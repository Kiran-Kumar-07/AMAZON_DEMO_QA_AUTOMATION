package com.ecommerce.automation.reporting;

import com.ecommerce.automation.config.ConfigReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

// Builds the Excel report from runtime data only. Shared across test classes,
// including in parallel, so records is thread-safe and generate() is idempotent.
public class ExcelReportManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReportManager.class);
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<TestResultRecord> records = new CopyOnWriteArrayList<>();
    private final AtomicBoolean generated = new AtomicBoolean(false);
    private volatile String generatedPath;

    public void addRecord(TestResultRecord record) {
        records.add(record);
    }

    public List<TestResultRecord> getRecords() {
        return records;
    }

    public synchronized String generate() {
        if (!generated.compareAndSet(false, true)) {
            return generatedPath;
        }

        String reportsDir = ConfigReader.getInstance().reportsDir();
        try {
            Files.createDirectories(Path.of(reportsDir));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create reports directory", e);
        }

        String fileName = "ExecutionReport_" + java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        Path reportPath = Path.of(reportsDir, fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            writeExecutionSheet(workbook);
            writeSummarySheet(workbook);

            try (FileOutputStream out = new FileOutputStream(reportPath.toFile())) {
                workbook.write(out);
            }
            LOGGER.info("Excel execution report generated: {}", reportPath);
            generatedPath = reportPath.toString();
            return generatedPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate Excel report", e);
        }
    }

    private void writeExecutionSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Test Execution Report");
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle passStyle = statusStyle(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle failStyle = statusStyle(workbook, IndexedColors.RED);
        CellStyle skipStyle = statusStyle(workbook, IndexedColors.LIGHT_YELLOW);

        String[] headers = {
                "Test Case ID", "Category", "Product Name", "Product Page Price", "Cart Price",
                "Quantity", "Expected Cart Total", "Actual Cart Total", "Status",
                "Execution Time", "Screenshot/Proof", "Remarks"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (TestResultRecord record : records) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(record.getTestCaseId());
            row.createCell(1).setCellValue(record.getCategory());
            row.createCell(2).setCellValue(nullSafe(record.getProductName()));
            row.createCell(3).setCellValue(formatMoney(record.getProductPagePrice()));
            row.createCell(4).setCellValue(formatMoney(record.getCartPrice()));
            row.createCell(5).setCellValue(record.getQuantity());
            row.createCell(6).setCellValue(formatMoney(record.getExpectedTotal()));
            row.createCell(7).setCellValue(formatMoney(record.getActualTotal()));

            Cell statusCell = row.createCell(8);
            statusCell.setCellValue(record.getStatus().name());
            statusCell.setCellStyle(switch (record.getStatus()) {
                case PASS -> passStyle;
                case FAIL -> failStyle;
                case SKIP -> skipStyle;
            });

            row.createCell(9).setCellValue(record.getExecutionTime().format(TS_FORMAT));
            row.createCell(10).setCellValue(nullSafe(record.getScreenshotPath()));
            row.createCell(11).setCellValue(nullSafe(record.getRemarks()));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeSummarySheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Execution Summary");
        CellStyle headerStyle = headerStyle(workbook);

        long total = records.size();
        long passed = records.stream().filter(r -> r.getStatus() == TestResultRecord.Status.PASS).count();
        long failed = records.stream().filter(r -> r.getStatus() == TestResultRecord.Status.FAIL).count();
        long skipped = records.stream().filter(r -> r.getStatus() == TestResultRecord.Status.SKIP).count();

        BigDecimal expectedTotal = records.stream()
                .map(TestResultRecord::getExpectedTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualTotal = records.stream()
                .map(TestResultRecord::getActualTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean totalsMatch = expectedTotal.compareTo(actualTotal) == 0;

        int r = 0;
        r = writeKeyValue(sheet, r, "Total Test Cases", String.valueOf(total));
        r = writeKeyValue(sheet, r, "Passed", String.valueOf(passed));
        r = writeKeyValue(sheet, r, "Failed", String.valueOf(failed));
        r = writeKeyValue(sheet, r, "Skipped", String.valueOf(skipped));
        r = writeKeyValue(sheet, r, "Execution Date",
                java.time.LocalDate.now().toString());
        r = writeKeyValue(sheet, r, "Execution Time",
                java.time.LocalTime.now().withNano(0).toString());
        r++; // blank separator row

        Row productHeader = sheet.createRow(r++);
        Cell c0 = productHeader.createCell(0);
        c0.setCellValue("Product Pricing Summary");
        c0.setCellStyle(headerStyle);

        int productNumber = 1;
        for (TestResultRecord record : records) {
            r = writeKeyValue(sheet, r, "Product " + productNumber++,
                    record.getProductName() + " - " + formatMoney(record.getProductPagePrice()));
        }
        r++;

        r = writeKeyValue(sheet, r, "Expected Total", formatMoney(expectedTotal));
        r = writeKeyValue(sheet, r, "Actual Total", formatMoney(actualTotal));
        writeKeyValue(sheet, r, "Total Validation", totalsMatch ? "PASS" : "FAIL");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private int writeKeyValue(Sheet sheet, int rowIndex, String key, String value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
        return rowIndex + 1;
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle statusStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "N/A" : "Rs. " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}

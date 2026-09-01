package com.ecommerce.automation.reporting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// One row of the dynamically generated Excel execution report.
public class TestResultRecord {

    public enum Status { PASS, FAIL, SKIP }

    private final String testCaseId;
    private final String category;
    private final String productName;
    private final BigDecimal productPagePrice;
    private final BigDecimal cartPrice;
    private final int quantity;
    private final BigDecimal expectedTotal;
    private final BigDecimal actualTotal;
    private final Status status;
    private final LocalDateTime executionTime;
    private final String screenshotPath;
    private final String remarks;

    private TestResultRecord(Builder b) {
        this.testCaseId = b.testCaseId;
        this.category = b.category;
        this.productName = b.productName;
        this.productPagePrice = b.productPagePrice;
        this.cartPrice = b.cartPrice;
        this.quantity = b.quantity;
        this.expectedTotal = b.expectedTotal;
        this.actualTotal = b.actualTotal;
        this.status = b.status;
        this.executionTime = b.executionTime;
        this.screenshotPath = b.screenshotPath;
        this.remarks = b.remarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTestCaseId() { return testCaseId; }
    public String getCategory() { return category; }
    public String getProductName() { return productName; }
    public BigDecimal getProductPagePrice() { return productPagePrice; }
    public BigDecimal getCartPrice() { return cartPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getExpectedTotal() { return expectedTotal; }
    public BigDecimal getActualTotal() { return actualTotal; }
    public Status getStatus() { return status; }
    public LocalDateTime getExecutionTime() { return executionTime; }
    public String getScreenshotPath() { return screenshotPath; }
    public String getRemarks() { return remarks; }

    public static class Builder {
        private String testCaseId;
        private String category;
        private String productName;
        private BigDecimal productPagePrice;
        private BigDecimal cartPrice;
        private int quantity;
        private BigDecimal expectedTotal;
        private BigDecimal actualTotal;
        private Status status = Status.SKIP;
        private LocalDateTime executionTime = LocalDateTime.now();
        private String screenshotPath = "";
        private String remarks = "";

        public Builder testCaseId(String v) { this.testCaseId = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder productName(String v) { this.productName = v; return this; }
        public Builder productPagePrice(BigDecimal v) { this.productPagePrice = v; return this; }
        public Builder cartPrice(BigDecimal v) { this.cartPrice = v; return this; }
        public Builder quantity(int v) { this.quantity = v; return this; }
        public Builder expectedTotal(BigDecimal v) { this.expectedTotal = v; return this; }
        public Builder actualTotal(BigDecimal v) { this.actualTotal = v; return this; }
        public Builder status(Status v) { this.status = v; return this; }
        public Builder executionTime(LocalDateTime v) { this.executionTime = v; return this; }
        public Builder screenshotPath(String v) { this.screenshotPath = v; return this; }
        public Builder remarks(String v) { this.remarks = v; return this; }

        public TestResultRecord build() {
            return new TestResultRecord(this);
        }
    }
}

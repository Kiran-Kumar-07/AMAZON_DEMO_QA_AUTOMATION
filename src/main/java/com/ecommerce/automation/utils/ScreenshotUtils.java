package com.ecommerce.automation.utils;

import com.ecommerce.automation.config.ConfigReader;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

// Captures full-page screenshots into the proof/ directory.
public final class ScreenshotUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotUtils.class);

    private ScreenshotUtils() {
    }

    public static String capture(WebDriver driver, String fileNameWithoutExtension) {
        String proofDir = ConfigReader.getInstance().proofDir();
        try {
            Path dir = Path.of(proofDir);
            Files.createDirectories(dir);

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path target = dir.resolve(fileNameWithoutExtension + ".png");
            Files.copy(src.toPath(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("Screenshot saved: {}", target);
            return target.toString();
        } catch (IOException e) {
            LOGGER.error("Failed to capture screenshot '{}'", fileNameWithoutExtension, e);
            return "";
        }
    }

    public static String timestampedName(String prefix) {
        return prefix + "_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}

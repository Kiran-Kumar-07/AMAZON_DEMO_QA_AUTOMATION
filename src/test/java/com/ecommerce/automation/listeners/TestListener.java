package com.ecommerce.automation.listeners;

import com.ecommerce.automation.driver.DriverFactory;
import com.ecommerce.automation.utils.ScreenshotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

// Captures a screenshot automatically whenever a test fails.
public class TestListener implements ITestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestFailure(ITestResult result) {
        String name = "FAILURE_" + result.getTestClass().getRealClass().getSimpleName()
                + "_" + result.getMethod().getMethodName();
        try {
            String path = ScreenshotUtils.capture(DriverFactory.getDriver(), ScreenshotUtils.timestampedName(name));
            LOGGER.error("Test failed: {}. Failure screenshot: {}", result.getName(), path);
        } catch (Exception e) {
            LOGGER.error("Could not capture failure screenshot for {}", result.getName(), e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warn("Test skipped: {} - {}", result.getName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "");
    }
}

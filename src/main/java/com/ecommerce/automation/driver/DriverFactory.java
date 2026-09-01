package com.ecommerce.automation.driver;

import com.ecommerce.automation.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

// ThreadLocal driver so parallel test execution doesn't share driver state.
public final class DriverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();
    private static final ConfigReader CONFIG = ConfigReader.getInstance();

    private DriverFactory() {
    }

    public static WebDriver getDriver() {
        if (DRIVER_THREAD_LOCAL.get() == null) {
            DRIVER_THREAD_LOCAL.set(createDriver(CONFIG.browser()));
        }
        return DRIVER_THREAD_LOCAL.get();
    }

    private static WebDriver createDriver(String browser) {
        WebDriver driver;
        switch (browser.trim().toLowerCase()) {
            case "firefox" -> {
                setupDriverManaged(WebDriverManager.firefoxdriver());
                FirefoxOptions options = new FirefoxOptions();
                if (CONFIG.headless()) {
                    options.addArguments("-headless");
                }
                driver = new FirefoxDriver(options);
            }
            case "edge" -> {
                setupDriverManaged(WebDriverManager.edgedriver());
                EdgeOptions options = new EdgeOptions();
                if (CONFIG.headless()) {
                    options.addArguments("--headless=new");
                }
                driver = new EdgeDriver(options);
            }
            default -> {
                setupDriverManaged(WebDriverManager.chromedriver());
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--start-maximized", "--disable-notifications", "--remote-allow-origins=*");
                if (CONFIG.headless()) {
                    options.addArguments("--headless=new");
                }
                driver = new ChromeDriver(options);
            }
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(CONFIG.pageLoadTimeoutSeconds()));
        driver.manage().window().maximize();
        return driver;
    }

    // Edge's WebDriverManager endpoint is dead upstream; fall back to Selenium
    // Manager (bundled since Selenium 4.6) instead of aborting driver creation.
    private static void setupDriverManaged(WebDriverManager manager) {
        try {
            manager.setup();
        } catch (RuntimeException e) {
            LOGGER.warn("WebDriverManager could not resolve a driver binary ({}); "
                    + "falling back to Selenium Manager's own resolution.", e.getMessage());
        }
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver != null) {
            driver.quit();
            DRIVER_THREAD_LOCAL.remove();
        }
    }
}

package com.ecommerce.automation.base;

import com.ecommerce.automation.config.ConfigReader;
import com.ecommerce.automation.driver.DriverFactory;
import com.ecommerce.automation.pages.CartPage;
import com.ecommerce.automation.pages.HomePage;
import com.ecommerce.automation.reporting.ExcelReportManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

// Shared driver lifecycle plus a suite-wide report instance.
public abstract class BaseTest {

    protected static final ConfigReader CONFIG = ConfigReader.getInstance();
    protected static final ExcelReportManager REPORT = new ExcelReportManager();

    protected WebDriver driver;
    protected HomePage homePage;
    protected CartPage cartPage;

    @BeforeClass(alwaysRun = true)
    public void setUpDriver() {
        driver = DriverFactory.getDriver();
        homePage = new HomePage(driver);
        cartPage = new CartPage(driver);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownDriver() {
        DriverFactory.quitDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void generateReport() {
        REPORT.generate();
    }
}

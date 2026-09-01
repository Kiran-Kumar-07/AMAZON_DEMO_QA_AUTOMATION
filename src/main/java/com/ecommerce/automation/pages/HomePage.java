package com.ecommerce.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Keys;

public class HomePage extends BasePage {

    private final By searchBox = By.id("twotabsearchtextbox");
    private final By searchSubmit = By.id("nav-search-submit-button");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl);
        dismissLocationOrConsentOverlayIfPresent();
    }

    // Waits for either a results grid or the "no results" banner, so a
    // legitimately empty search doesn't just time out.
    public SearchResultsPage searchFor(String keyword) {
        wait.waitForVisible(searchBox).clear();
        wait.waitForVisible(searchBox).sendKeys(keyword, Keys.ENTER);
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div[data-component-type='s-search-result']")),
                org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//span[contains(text(),'No results for')]"))
        ));
        return new SearchResultsPage(driver);
    }
}

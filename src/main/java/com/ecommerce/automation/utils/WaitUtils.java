package com.ecommerce.automation.utils;

import com.ecommerce.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

// Central place for explicit/conditional waits, so no test needs Thread.sleep().
public final class WaitUtils {

    private final WebDriver driver;
    private final Wait<WebDriver> wait;

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        int timeoutSeconds = ConfigReader.getInstance().explicitWaitSeconds();
        this.wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(300))
                .ignoring(StaleElementReferenceException.class)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public List<WebElement> waitForAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public boolean waitForInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForTextPresent(By locator, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public <T> T until(Function<WebDriver, T> condition) {
        return wait.until(condition::apply);
    }

    public <T> T until(ExpectedCondition<T> condition) {
        return wait.until(condition);
    }

    public void safeClick(By locator) {
        int attempts = 3;
        StaleElementReferenceException last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                waitForClickable(locator).click();
                return;
            } catch (StaleElementReferenceException e) {
                last = e;
            }
        }
        throw last;
    }

    // Some locators (e.g. add-to-cart-button) resolve to several elements (one per
    // purchase-option row), only one of which is visible; By.id always returns the
    // first in document order, which may be the hidden one.
    public void safeClickFirstVisible(By locator) {
        try {
            WebElement target = wait.until(d -> {
                for (WebElement el : d.findElements(locator)) {
                    try {
                        if (el.isDisplayed() && el.isEnabled()) {
                            return el;
                        }
                    } catch (StaleElementReferenceException ignored) {
                        // element re-rendered mid-scan; skip and keep polling
                    }
                }
                return null;
            });
            target.click();
        } catch (TimeoutException e) {
            // Last resort: some buttons are a functionally-clickable but
            // Selenium-invisible <input> styled behind a visible sibling span.
            List<WebElement> candidates = driver.findElements(locator);
            if (candidates.isEmpty()) {
                throw e;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidates.get(0));
        }
    }
}

package com.ecommerce.automation.pages;

import com.ecommerce.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePage {

    private static final By CONTINUE_SHOPPING_OVERLAY_BUTTON =
            By.xpath("//button[contains(., 'Continue shopping')] | //input[@id='sp-cc-accept']");

    protected final WebDriver driver;
    protected final WaitUtils wait;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    // Best-effort dismiss of Amazon's location/consent interstitial, which can
    // appear on any first navigation of a session (more likely from a fresh,
    // cookie-less CI runner than a local browser with existing browsing history).
    protected void dismissLocationOrConsentOverlayIfPresent() {
        try {
            if (!driver.findElements(CONTINUE_SHOPPING_OVERLAY_BUTTON).isEmpty()) {
                wait.waitForClickable(CONTINUE_SHOPPING_OVERLAY_BUTTON).click();
            }
        } catch (Exception e) {
            logger.debug("No dismissible overlay present on load: {}", e.getMessage());
        }
    }
}

package com.ecommerce.automation.pages;

import com.ecommerce.automation.utils.PriceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductPage extends BasePage {

    private final By productTitle = By.id("productTitle");
    // Excludes the struck-through M.R.P. block, which precedes the real offer price.
    private final By priceWhole = By.cssSelector(
            "span.a-price:not(.a-text-price):not([data-a-strike='true']) > span.a-offscreen");
    private final By altPrice = By.cssSelector("#corePriceDisplay_desktop_feature_div .a-offscreen, #price_inside_buybox, #priceblock_ourprice, #priceblock_dealprice");
    private final By addToCartButton = By.id("add-to-cart-button");
    private final By outOfStock = By.xpath("//*[@id='availability']//span[contains(text(),'unavailable') or contains(text(),'Currently unavailable')]");
    private final By quantityDropdown = By.cssSelector("#quantity, select#quantity");
    // Reliable add-to-cart signal: always in the main document, unlike the
    // confirmation panel/upsell overlay Amazon swaps between per listing.
    private final By navCartCount = By.id("nav-cart-count");
    // "No thanks" on the protection-plan upsell; item isn't added until dismissed.
    // Must target the button span itself, not its aria-hidden "-announce" label child.
    private final By skipProtectionPlanUpsell = By.id("attachSiNoCoverage");
    private static final Pattern ASIN_IN_URL = Pattern.compile("/(?:dp|gp/product)/([A-Z0-9]{10})");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    // ASIN, not product name, since Amazon can phrase the same title
    // differently on the product page vs. the cart line.
    public String captureAsin() {
        Matcher matcher = ASIN_IN_URL.matcher(driver.getCurrentUrl());
        return matcher.find() ? matcher.group(1) : null;
    }

    public boolean isInStock() {
        return driver.findElements(outOfStock).isEmpty()
                && !driver.findElements(addToCartButton).isEmpty();
    }

    public String captureProductName() {
        return wait.waitForVisible(productTitle).getText().trim();
    }

    public BigDecimal captureProductPrice() {
        // textContent not getText(): .a-offscreen is screen-reader-only. Scan for the
        // first non-blank match since an earlier price block can be an empty placeholder.
        for (WebElement candidate : driver.findElements(priceWhole)) {
            String text = candidate.getAttribute("textContent");
            if (text != null && !text.isBlank()) {
                return PriceUtils.parse(text);
            }
        }
        for (WebElement candidate : driver.findElements(altPrice)) {
            String text = candidate.getAttribute("textContent");
            if (text != null && !text.isBlank()) {
                return PriceUtils.parse(text);
            }
        }
        return null;
    }

    public void setQuantity(int quantity) {
        List<WebElement> dropdown = driver.findElements(quantityDropdown);
        if (dropdown.isEmpty() || quantity <= 1) {
            return; // most listings default to 1; only touch the control when it exists and qty > 1
        }
        new org.openqa.selenium.support.ui.Select(dropdown.get(0)).selectByVisibleText(String.valueOf(quantity));
    }

    public void addToCart() {
        int before = readCartCount();
        wait.safeClickFirstVisible(addToCartButton);
        // Dismiss the upsell if it appears, then keep polling until the count updates.
        wait.until(d -> {
            if (readCartCount() > before) {
                return true;
            }
            dismissProtectionPlanUpsellIfPresent();
            return false;
        });
    }

    private void dismissProtectionPlanUpsellIfPresent() {
        try {
            for (WebElement candidate : driver.findElements(skipProtectionPlanUpsell)) {
                if (candidate.isDisplayed()) {
                    candidate.click();
                    return;
                }
            }
        } catch (Exception e) {
            logger.debug("No protection-plan upsell overlay to dismiss: {}", e.getMessage());
        }
    }

    private int readCartCount() {
        List<WebElement> badge = driver.findElements(navCartCount);
        if (badge.isEmpty()) {
            return 0;
        }
        String text = badge.get(0).getAttribute("textContent");
        try {
            return text == null || text.isBlank() ? 0 : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

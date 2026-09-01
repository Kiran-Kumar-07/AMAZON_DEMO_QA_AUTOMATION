package com.ecommerce.automation.pages;

import com.ecommerce.automation.utils.PriceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartPage extends BasePage {

    private final By cartItemRows = By.cssSelector("div.sc-list-item");
    private final By itemTitle = By.cssSelector(".sc-product-title, span.a-truncate-cut, .sc-product-title a");
    private final By itemLink = By.cssSelector("a.sc-product-link");
    private static final Pattern ASIN_IN_URL = Pattern.compile("/(?:dp|gp/product)/([A-Z0-9]{10})");
    // Excludes the struck-through M.R.P. block, same as the product page locator.
    private final By itemPrice = By.cssSelector(
            ".sc-product-price, .sc-price .a-color-price, .a-price:not(.a-text-price) .a-offscreen");
    // Modern "stepper" fieldset, not the classic <input class="quantity-textbox">.
    private final By itemQuantity = By.cssSelector("fieldset[name='sc-quantity'], input.quantity-textbox, select[name='quantity']");
    private final By removeButton = By.xpath(".//span[contains(@class,'sc-action-delete')]//input | .//a[contains(text(),'Delete')]");
    // Empty-cart message is an <h3>, not an <h1>; class match with a text fallback.
    private final By emptyCartMessage = By.xpath(
            "//*[contains(@class,'sc-your-amazon-cart-is-empty')] | //*[contains(text(),'Cart is empty')]");
    private final By subtotal = By.id("sc-subtotal-amount-activecart");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/gp/cart/view.html");
        dismissLocationOrConsentOverlayIfPresent();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(cartItemRows),
                ExpectedConditions.presenceOfElementLocated(emptyCartMessage)
        ));
    }

    public boolean isEmpty() {
        return !driver.findElements(emptyCartMessage).isEmpty();
    }

    public int itemCount() {
        return activeRows().size();
    }

    // A removed row's container stays in the DOM (Amazon just hides its title and
    // shows a "was removed" message), so a row only counts as active while visible.
    private List<WebElement> activeRows() {
        List<WebElement> active = new ArrayList<>();
        for (WebElement row : driver.findElements(cartItemRows)) {
            boolean hasVisibleTitle = row.findElements(itemTitle).stream().anyMatch(WebElement::isDisplayed);
            if (hasVisibleTitle) {
                active.add(row);
            }
        }
        return active;
    }

    // Retries the whole read on StaleElementReferenceException: Amazon re-renders
    // the row list via AJAX after any cart mutation, which can race a fresh read.
    public List<CartLine> readAllLines() {
        int attempts = 3;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                List<CartLine> lines = new ArrayList<>();
                for (WebElement row : activeRows()) {
                    String name = row.findElements(itemTitle).stream().findFirst().map(WebElement::getText).orElse("").trim();
                    // textContent, not getText(): price can resolve to a screen-reader-only node.
                    String priceText = row.findElements(itemPrice).stream().findFirst()
                            .map(el -> el.getAttribute("textContent")).orElse("");
                    BigDecimal price = PriceUtils.parse(priceText);
                    int qty = readQuantity(row);
                    String asin = readAsin(row);
                    lines.add(new CartLine(name, price, qty, asin));
                }
                return lines;
            } catch (StaleElementReferenceException e) {
                if (attempt == attempts) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private String readAsin(WebElement row) {
        List<WebElement> links = row.findElements(itemLink);
        if (links.isEmpty()) {
            return null;
        }
        String href = links.get(0).getAttribute("href");
        if (href == null) {
            return null;
        }
        Matcher matcher = ASIN_IN_URL.matcher(href);
        return matcher.find() ? matcher.group(1) : null;
    }

    private int readQuantity(WebElement row) {
        List<WebElement> qtyInput = row.findElements(itemQuantity);
        if (qtyInput.isEmpty()) {
            return 1;
        }
        WebElement control = qtyInput.get(0);
        String value = "fieldset".equalsIgnoreCase(control.getTagName())
                ? control.getAttribute("data-steppervalue")
                : control.getAttribute("value");
        try {
            return value == null || value.isBlank() ? 1 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public BigDecimal readSubtotal() {
        List<WebElement> el = driver.findElements(subtotal);
        if (el.isEmpty()) {
            return null;
        }
        return PriceUtils.parse(el.get(0).getAttribute("textContent"));
    }

    public void removeProductByName(String productNameFragment) {
        int beforeCount = itemCount();
        BigDecimal beforeSubtotal = readSubtotal();
        List<WebElement> rows = activeRows();
        for (WebElement row : rows) {
            String name = row.findElements(itemTitle).stream().findFirst().map(WebElement::getText).orElse("");
            if (name.contains(productNameFragment)) {
                row.findElement(removeButton).click();
                break;
            }
        }
        awaitRemoval(beforeCount, beforeSubtotal);
    }

    public void removeProductByAsin(String asin) {
        int beforeCount = itemCount();
        BigDecimal beforeSubtotal = readSubtotal();
        for (WebElement row : activeRows()) {
            if (asin.equals(readAsin(row))) {
                row.findElement(removeButton).click();
                break;
            }
        }
        awaitRemoval(beforeCount, beforeSubtotal);
    }

    // Row count and subtotal update via separate AJAX calls; the count can drop
    // before the subtotal catches up, so a caller reading the subtotal right after
    // itemCount() alone can race a stale (pre-removal) value, especially on a
    // slower CI runner. Wait for both to reflect the removal.
    private void awaitRemoval(int beforeCount, BigDecimal beforeSubtotal) {
        wait.until(d -> itemCount() < beforeCount || isEmpty());
        wait.until(d -> {
            if (isEmpty()) {
                return true;
            }
            BigDecimal current = readSubtotal();
            return current != null && !PriceUtils.areEqual(current, beforeSubtotal);
        });
    }

    public record CartLine(String name, BigDecimal price, int quantity, String asin) {
    }
}

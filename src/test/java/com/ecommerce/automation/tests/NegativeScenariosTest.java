package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.pages.ProductPage;
import com.ecommerce.automation.pages.SearchResultsPage;
import com.ecommerce.automation.utils.PriceUtils;
import com.ecommerce.automation.utils.ScreenshotUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

public class NegativeScenariosTest extends BaseTest {

    // Broad categories (laptops, phones, etc.) almost never carry an unavailable
    // badge live; slower-moving/niche categories do far more reliably.
    private static final String[] OUT_OF_STOCK_PRONE_SEARCHES = {
            "smartwatch", "vintage camera", "graphics card", "gaming console"
    };

    @Test(description = "Searching for a product that does not exist should not crash and should surface no results")
    public void searchForNonExistentProductReturnsNoResults() {
        homePage.open(CONFIG.baseUrl());
        SearchResultsPage results = homePage.searchFor("zzqxnonexistentproduct1234567890");
        ScreenshotUtils.capture(driver, "negative_search_no_results");

        Assert.assertTrue(results.hasNoResults(),
                "Searching for a nonsense keyword should show Amazon's 'No results for' banner");
    }

    @Test(description = "An out-of-stock product should be correctly identified as not in stock (skips only if truly none found live)")
    public void unavailableProductIsCorrectlyFlagged() {
        By unavailableBadge = By.xpath(
                "//div[@data-component-type='s-search-result']"
                        + "//span[contains(translate(text(),'CURRENTLY UNAVAILABLE','currently unavailable'),'currently unavailable')]");

        WebElement badge = null;
        for (String keyword : OUT_OF_STOCK_PRONE_SEARCHES) {
            homePage.open(CONFIG.baseUrl());
            homePage.searchFor(keyword);
            List<WebElement> badges = driver.findElements(unavailableBadge);
            if (!badges.isEmpty()) {
                badge = badges.get(0);
                break;
            }
        }

        if (badge == null) {
            throw new SkipException("No out-of-stock product surfaced across " + OUT_OF_STOCK_PRONE_SEARCHES.length
                    + " tried search terms; cannot verify without hard-coding a fixed product URL. Skipped rather than false-passed.");
        }

        WebElement badgeTile = badge.findElement(By.xpath("ancestor::div[@data-component-type='s-search-result']"));
        // <a> wraps <h2>, not the reverse -- same fix as SearchResultsPage.tileLink.
        WebElement link = badgeTile.findElement(By.xpath(".//h2/ancestor::a[1]"));
        driver.get(link.getAttribute("href"));

        ProductPage productPage = new ProductPage(driver);
        ScreenshotUtils.capture(driver, "negative_out_of_stock_product");

        Assert.assertFalse(productPage.isInStock(), "Product flagged unavailable in listing should also report not-in-stock on its product page");
    }

    @Test(description = "A freshly opened cart (no prior additions in this browser session) should be reported as empty")
    public void emptyCartIsCorrectlyValidated() {
        // Warm up the session on the homepage first: a cookie-less driver's very
        // first navigation going straight to a deep link (like the cart) is far
        // more likely to hit Amazon's bot-detection/interstitial on a datacenter
        // CI IP than a normal browsing pattern would.
        homePage.open(CONFIG.baseUrl());
        cartPage.open(CONFIG.baseUrl());
        ScreenshotUtils.capture(driver, "negative_empty_cart");

        Assert.assertTrue(cartPage.isEmpty(), "Cart should be reported empty before any product has been added in this session");
        Assert.assertEquals(cartPage.itemCount(), 0, "Empty cart should report zero item rows");
    }

    @Test(description = "Add a single product then remove it, and verify the cart returns to an empty state")
    public void removingTheOnlyProductLeavesCartEmpty() {
        homePage.open(CONFIG.baseUrl());
        SearchResultsPage results = homePage.searchFor("book");
        ProductPage productPage = results.openFirstAvailableProduct();
        String productName = productPage.captureProductName();
        productPage.addToCart();

        cartPage.open(CONFIG.baseUrl());
        String shortName = String.join(" ", productName.trim().split("\\s+", 5)).split("\\s{5,}")[0];
        cartPage.removeProductByName(shortName.length() > 15 ? shortName.substring(0, 15) : shortName);

        ScreenshotUtils.capture(driver, "negative_cart_after_only_item_removed");
        Assert.assertTrue(cartPage.isEmpty() || cartPage.itemCount() == 0,
                "Cart should be empty after removing the only product it contained");
    }

    @Test(description = "Demonstrate that an incorrect price/total comparison is detected as a failure, not a false PASS")
    public void intentionalValidationFailureIsDetectedNotFalsePassed() {
        BigDecimal actualPagePrice = new BigDecimal("999.00");
        BigDecimal deliberatelyWrongExpectedPrice = new BigDecimal("1.00");

        boolean pricesMatch = PriceUtils.areEqual(actualPagePrice, deliberatelyWrongExpectedPrice);
        Assert.assertFalse(pricesMatch,
                "PriceUtils must correctly flag mismatched prices as NOT equal (framework must not false-PASS a mismatch)");

        Assert.assertThrows(AssertionError.class, () ->
                Assert.assertEquals(actualPagePrice, deliberatelyWrongExpectedPrice,
                        "Intentionally wrong comparison to prove TestNG assertions fail loudly rather than silently"));
    }
}

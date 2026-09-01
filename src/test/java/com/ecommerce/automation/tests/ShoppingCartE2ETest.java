package com.ecommerce.automation.tests;

import com.ecommerce.automation.base.BaseTest;
import com.ecommerce.automation.models.CategoryInput;
import com.ecommerce.automation.models.Product;
import com.ecommerce.automation.pages.CartPage;
import com.ecommerce.automation.pages.ProductPage;
import com.ecommerce.automation.pages.SearchResultsPage;
import com.ecommerce.automation.reporting.TestResultRecord;
import com.ecommerce.automation.utils.CsvTestDataReader;
import com.ecommerce.automation.utils.PriceUtils;
import com.ecommerce.automation.utils.ScreenshotUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartE2ETest extends BaseTest {

    private final List<Product> shoppedProducts = new ArrayList<>();
    private int testCaseCounter = 1;

    @Test(priority = 1, description = "Select and add 5 products from 5 different categories, driven by test-data/categories.csv")
    public void shopFiveDifferentCategories() {
        List<CategoryInput> categories = CsvTestDataReader.readCategories(CONFIG.testDataFile());
        Assert.assertTrue(categories.size() >= CONFIG.productsToSelect(),
                "Test data must define at least " + CONFIG.productsToSelect() + " categories");

        int index = 1;
        for (CategoryInput categoryInput : categories.subList(0, CONFIG.productsToSelect())) {
            Product product = shopForCategory(categoryInput, index++);
            shoppedProducts.add(product);
        }

        Assert.assertEquals(shoppedProducts.size(), CONFIG.productsToSelect(),
                "Expected " + CONFIG.productsToSelect() + " products to be added to the cart");
    }

    private Product shopForCategory(CategoryInput categoryInput, int sequence) {
        String testCaseId = "TC-" + String.format("%03d", testCaseCounter++);
        Product product = new Product(categoryInput.getCategory(), categoryInput.getSearchKeyword(), categoryInput.getQuantity());

        try {
            homePage.open(CONFIG.baseUrl());
            SearchResultsPage results = homePage.searchFor(categoryInput.getSearchKeyword());

            if (results.hasNoResults()) {
                throw new SearchResultsPage.NoAvailableProductException(
                        "No search results for keyword: " + categoryInput.getSearchKeyword());
            }

            ProductPage productPage = results.openFirstAvailableProduct();

            String name = productPage.captureProductName();
            BigDecimal price = productPage.captureProductPrice();
            String asin = productPage.captureAsin();
            productPage.setQuantity(categoryInput.getQuantity());

            product.setProductName(name);
            product.setAsin(asin);
            product.setProductPagePrice(price);

            String screenshotPath = ScreenshotUtils.capture(driver, "product" + sequence + "_before_add");
            product.setScreenshotPath(screenshotPath);

            Assert.assertNotNull(price, "Product page price must be captured dynamically before Add to Cart: " + name);

            productPage.addToCart();
            product.setAddedToCart(true);

            REPORT.addRecord(TestResultRecord.builder()
                    .testCaseId(testCaseId)
                    .category(categoryInput.getCategory())
                    .productName(name)
                    .productPagePrice(price)
                    .quantity(categoryInput.getQuantity())
                    .status(TestResultRecord.Status.PASS)
                    .screenshotPath(screenshotPath)
                    .remarks("Added to cart successfully")
                    .build());

            return product;

        } catch (Exception e) {
            String screenshotPath = ScreenshotUtils.capture(driver, ScreenshotUtils.timestampedName("FAILURE_" + testCaseId));
            REPORT.addRecord(TestResultRecord.builder()
                    .testCaseId(testCaseId)
                    .category(categoryInput.getCategory())
                    .productName(product.getProductName())
                    .status(TestResultRecord.Status.FAIL)
                    .screenshotPath(screenshotPath)
                    .remarks(e.getMessage())
                    .build());
            throw new AssertionError("Failed shopping flow for category '" + categoryInput.getCategory() + "'", e);
        }
    }

    @Test(priority = 2, dependsOnMethods = "shopFiveDifferentCategories",
            description = "Validate the cart contains all 5 products with matching names, prices and quantities")
    public void validateCartContainsAllFiveProducts() {
        cartPage.open(CONFIG.baseUrl());
        ScreenshotUtils.capture(driver, "cart_with_5_products");

        Assert.assertFalse(cartPage.isEmpty(), "Cart should not be empty after adding 5 products");
        Assert.assertEquals(cartPage.itemCount(), shoppedProducts.size(),
                "Cart item count should equal the number of products added");

        List<CartPage.CartLine> cartLines = cartPage.readAllLines();

        for (Product product : shoppedProducts) {
            // Match by ASIN, not name: Amazon can phrase the same product's
            // title differently on the product page vs. the cart line.
            CartPage.CartLine matchingLine = cartLines.stream()
                    .filter(line -> line.asin() != null && line.asin().equals(product.getAsin()))
                    .findFirst()
                    .orElse(null);

            Assert.assertNotNull(matchingLine,
                    "Cart should contain product added for category " + product.getCategory() + ": " + product.getProductName());

            product.setCartPrice(matchingLine.price());
            Assert.assertEquals(matchingLine.quantity(), product.getQuantity(),
                    "Quantity mismatch in cart for " + product.getProductName());
        }
    }

    @Test(priority = 3, dependsOnMethods = "validateCartContainsAllFiveProducts",
            description = "Validate product-page price against the price shown in the cart for every product")
    public void validateProductPricesAgainstCart() {
        ScreenshotUtils.capture(driver, "cart_price_validation");

        for (Product product : shoppedProducts) {
            boolean pricesMatch = PriceUtils.areEqual(product.getProductPagePrice(), product.getCartPrice());
            if (!pricesMatch) {
                product.appendRemark("Price mismatch: page=" + PriceUtils.format(product.getProductPagePrice())
                        + " cart=" + PriceUtils.format(product.getCartPrice()));
            }
            Assert.assertTrue(pricesMatch,
                    "Product page price should equal cart price for " + product.getProductName()
                            + " (page=" + PriceUtils.format(product.getProductPagePrice())
                            + ", cart=" + PriceUtils.format(product.getCartPrice()) + ")");
        }
    }

    @Test(priority = 4, dependsOnMethods = "validateProductPricesAgainstCart",
            description = "Calculate the expected cart total dynamically and compare it to the actual cart subtotal")
    public void validateCartTotal() {
        BigDecimal expectedTotal = shoppedProducts.stream()
                .map(Product::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualTotal = cartPage.readSubtotal();
        ScreenshotUtils.capture(driver, "cart_total_validation");

        for (Product product : shoppedProducts) {
            REPORT.addRecord(TestResultRecord.builder()
                    .testCaseId("TC-TOTAL-" + product.getCategory().replaceAll("\\s+", ""))
                    .category(product.getCategory())
                    .productName(product.getProductName())
                    .productPagePrice(product.getProductPagePrice())
                    .cartPrice(product.getCartPrice())
                    .quantity(product.getQuantity())
                    .expectedTotal(product.lineTotal())
                    .actualTotal(product.lineTotal())
                    .status(PriceUtils.areEqual(product.getProductPagePrice(), product.getCartPrice())
                            ? TestResultRecord.Status.PASS : TestResultRecord.Status.FAIL)
                    .screenshotPath(product.getScreenshotPath())
                    .remarks(product.getRemarks())
                    .build());
        }

        Assert.assertNotNull(actualTotal, "Cart subtotal should be readable from the cart page");
        Assert.assertTrue(PriceUtils.areEqual(expectedTotal, actualTotal),
                "Expected cart total (" + PriceUtils.format(expectedTotal)
                        + ") should equal the actual cart subtotal (" + PriceUtils.format(actualTotal) + ")");
    }

    @Test(priority = 5, dependsOnMethods = "validateCartTotal",
            description = "Remove one product from the cart and validate the remaining 4 items and updated total")
    public void modifyCartAndRemoveOneProduct() {
        Product removedProduct = shoppedProducts.get(0);

        cartPage.removeProductByAsin(removedProduct.getAsin());
        ScreenshotUtils.capture(driver, "cart_after_product_removal");

        List<CartPage.CartLine> remainingLines = cartPage.readAllLines();
        Assert.assertEquals(cartPage.itemCount(), shoppedProducts.size() - 1,
                "Cart should contain 4 items after removing 1 of 5 products");

        boolean removedStillPresent = remainingLines.stream()
                .anyMatch(line -> line.asin() != null && line.asin().equals(removedProduct.getAsin()));
        Assert.assertFalse(removedStillPresent, "Removed product should no longer be present in the cart");

        for (Product product : shoppedProducts.subList(1, shoppedProducts.size())) {
            boolean stillPresent = remainingLines.stream()
                    .anyMatch(line -> line.asin() != null && line.asin().equals(product.getAsin()));
            Assert.assertTrue(stillPresent, "Remaining product should still be present: " + product.getProductName());
        }

        // Expected total is derived from the remaining cart lines' own displayed
        // prices, not the earlier product-page capture: cart-level promotions can
        // legitimately recompute per-item pricing when the cart composition changes
        // (CLAUDE.md "Pricing Complexity"), so this validates the cart's own
        // arithmetic (sum of its line items) rather than assuming the price
        // captured before removal still applies after it.
        BigDecimal expectedRemainingTotal = remainingLines.stream()
                .map(line -> line.price() == null ? BigDecimal.ZERO : line.price().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualRemainingTotal = cartPage.readSubtotal();
        Assert.assertTrue(PriceUtils.areEqual(expectedRemainingTotal, actualRemainingTotal),
                "Cart total after removal should equal the sum of the 4 remaining products' cart-line prices"
                        + " (expected=" + PriceUtils.format(expectedRemainingTotal)
                        + ", actual=" + PriceUtils.format(actualRemainingTotal) + ")");
    }
}

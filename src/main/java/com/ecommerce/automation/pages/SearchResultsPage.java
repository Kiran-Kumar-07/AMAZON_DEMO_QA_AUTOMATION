package com.ecommerce.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Optional;

// Scans search result tiles at runtime and opens the first available, priced one.
public class SearchResultsPage extends BasePage {

    private final By resultTiles = By.cssSelector("div[data-component-type='s-search-result']");
    private final By tileTitle = By.cssSelector("h2 span, h2 a span");
    private final By tilePrice = By.cssSelector("span.a-price > span.a-offscreen");
    private final By tileUnavailableBadge =
            By.xpath(".//span[contains(translate(text(),'CURRENTLY UNAVAILABLE','currently unavailable'),'currently unavailable')]");
    // <a> wraps <h2>, not the reverse, so the link is the h2's nearest ancestor anchor.
    private final By tileLink = By.xpath(".//h2/ancestor::a[1]");
    private final By noResultsBanner = By.xpath("//span[contains(text(),'No results for')]");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasNoResults() {
        return !driver.findElements(noResultsBanner).isEmpty();
    }

    public ProductPage openFirstAvailableProduct() {
        List<WebElement> tiles = wait.waitForAllVisible(resultTiles);

        for (WebElement tile : tiles) {
            try {
                if (!isAvailable(tile)) {
                    continue;
                }
                List<WebElement> link = tile.findElements(tileLink);
                if (link.isEmpty()) {
                    continue;
                }
                String href = link.get(0).getAttribute("href");
                if (href == null || href.isBlank()) {
                    continue;
                }
                driver.get(href);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.id("productTitle")),
                        ExpectedConditions.presenceOfElementLocated(By.id("title"))
                ));
                return new ProductPage(driver);
            } catch (StaleElementReferenceException e) {
                logger.debug("Stale search result tile, skipping to next candidate");
            }
        }
        throw new NoAvailableProductException("No available/in-stock product found in search results");
    }

    private boolean isAvailable(WebElement tile) {
        // .a-offscreen is screen-reader-only text; getText() reads it as "" so use textContent.
        Optional<WebElement> price = tile.findElements(tilePrice).stream().findFirst();
        boolean hasPrice = price.isPresent() && !price.get().getAttribute("textContent").isBlank();
        boolean flaggedUnavailable = !tile.findElements(tileUnavailableBadge).isEmpty();
        boolean hasTitle = !tile.findElements(tileTitle).isEmpty();
        return hasPrice && hasTitle && !flaggedUnavailable;
    }

    public static class NoAvailableProductException extends RuntimeException {
        public NoAvailableProductException(String message) {
            super(message);
        }
    }
}

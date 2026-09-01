package com.ecommerce.automation.models;

import java.math.BigDecimal;

// Runtime snapshot of a product, populated dynamically during execution.
public class Product {

    private final String category;
    private final String searchKeyword;
    private String productName;
    private String asin;
    private BigDecimal productPagePrice;
    private int quantity;
    private String screenshotPath;

    private BigDecimal cartPrice;
    private boolean addedToCart;
    private String remarks = "";

    public Product(String category, String searchKeyword, int quantity) {
        this.category = category;
        this.searchKeyword = searchKeyword;
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public BigDecimal getProductPagePrice() {
        return productPagePrice;
    }

    public void setProductPagePrice(BigDecimal productPagePrice) {
        this.productPagePrice = productPagePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public BigDecimal getCartPrice() {
        return cartPrice;
    }

    public void setCartPrice(BigDecimal cartPrice) {
        this.cartPrice = cartPrice;
    }

    public boolean isAddedToCart() {
        return addedToCart;
    }

    public void setAddedToCart(boolean addedToCart) {
        this.addedToCart = addedToCart;
    }

    public String getRemarks() {
        return remarks;
    }

    public void appendRemark(String remark) {
        this.remarks = this.remarks.isBlank() ? remark : this.remarks + "; " + remark;
    }

    public BigDecimal lineTotal() {
        BigDecimal unitPrice = productPagePrice != null ? productPagePrice : BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "Product{category='%s', name='%s', pagePrice=%s, cartPrice=%s, qty=%d}"
                .formatted(category, productName, productPagePrice, cartPrice, quantity);
    }
}

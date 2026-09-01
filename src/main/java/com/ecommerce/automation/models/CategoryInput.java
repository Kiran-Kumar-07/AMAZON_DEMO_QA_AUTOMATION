package com.ecommerce.automation.models;

// One row of test data read from test-data/categories.csv.
public class CategoryInput {

    private final String category;
    private final String searchKeyword;
    private final int quantity;

    public CategoryInput(String category, String searchKeyword, int quantity) {
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

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "CategoryInput{category='%s', searchKeyword='%s', quantity=%d}"
                .formatted(category, searchKeyword, quantity);
    }
}

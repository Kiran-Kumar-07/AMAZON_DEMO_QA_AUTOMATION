# Automation Tester – Practical Assignment

### Objective

The objective of this assignment is to evaluate your practical knowledge of:

Java

Selenium WebDriver

Automation Framework Design

Test Case Design

Dynamic Web Elements

Synchronization and Waits

Test Data Management

Data Validation

Excel Reporting

Screenshot/Evidence Management

Debugging

Git/GitHub

Maintainable and Reusable Automation

## 1. E-Commerce Automation Scenario

Use an e-commerce website Amazon 

Automate an end-to-end shopping cart scenario.

### Flow

The automation script should:

Open the e-commerce website.(e.g. Amazon, Filpkart)

Select 5 products from 5 different categories.

Products should be selected dynamically based on their availability.

For each selected product:

Search for the product/category.

Select an available product.

Capture the product name.

Capture the product price.

Capture other relevant product information where applicable.

Take a screenshot BEFORE clicking “Add to Cart”.

Add the product to the cart.

Repeat the same process for all 5 products.

Navigate to the shopping cart.

Validate all 5 products and their details.

### Suggested Categories

You may use the following categories:

You may select different categories/products if the above are unavailable.

Important: The automation should select products that are actually available at the time of execution.

## 2. Product Information Capture

For each selected product, the automation must capture the actual information from the website at runtime.

At minimum:

Category

Product Name

Product Price

Quantity

Example:

Category       : Electronics
Product Name   : XYZ Laptop
Product Price  : ₹52,999
Quantity       : 1

The product information must be captured before clicking Add to Cart.

## 3. Mandatory Product Screenshot

For each of the 5 products, a screenshot must be taken before the product is added to the cart.

The screenshot should clearly show:

Product name

Product price

Product page/details

Example:

proof/
├── product1_before_add.png
├── product2_before_add.png
├── product3_before_add.png
├── product4_before_add.png
└── product5_before_add.png

The screenshot must be captured before the Add to Cart action.

This evidence will be used to validate the product price against the price displayed later in the cart.

## 4. Add Products to Cart

After capturing the product details and screenshot:

Click Add to Cart.

Verify that the product has been successfully added.

Continue with the next category/product.

The same test logic should be reused for all five products.

Avoid creating five separate test implementations containing duplicated code.

## 5. Cart Validation

After all five products have been added:

Open the cart.

Take a cart screenshot.

Validate that all 5 selected products are present.

Validate that the product names match the products selected.

Validate the quantity of each product.

Validate the product prices.

Validate the cart item count.

The cart screenshot must provide evidence of the validation.

Example:

proof/
├── cart_with_5_products.png
├── cart_price_validation.png
└── cart_total_validation.png

If all five products cannot be displayed in one screenshot, multiple screenshots may be used.

## 6. Product Price Validation

The product price must be captured from the product page before Add to Cart.

The captured price should then be compared with the corresponding price displayed in the cart.

Example:

Product Page
Product: XYZ Laptop
Price: ₹52,999
        ↓
Screenshot
        ↓
Add to Cart
        ↓
Cart
Product: XYZ Laptop
Price: ₹52,999
        ↓
Compare
        ↓
PASS / FAIL

Do not hard-code product prices.

The price must be obtained dynamically during execution.

### Pricing Complexity

If the website displays:

MRP

Selling price

Discount

Coupon

Tax

Delivery charges

Cart-level discount

Other applicable charges

the candidate should identify the applicable pricing model and perform a meaningful validation.

Do not simply compare two values without understanding what each value represents.

## 7. Cart Total Validation

Calculate the expected cart amount based on the prices captured during execution.

Compare:

Expected Cart Total
        VS
Actual Cart Total

The expected value should be calculated dynamically.

Do not hard-code the expected total.

Where discounts, taxes or other charges are explicitly displayed, the calculation should account for them appropriately.

## 8. Cart Modification

After validating all five products:

Remove one product from the cart.

Verify that the removed product is no longer present.

Verify that the remaining four products are still present.

Validate the updated cart count.

Validate the updated cart total.

Take a screenshot of the updated cart.

Example:

proof/
└── cart_after_product_removal.png

## 9. Negative Scenarios

Include appropriate negative test scenarios.

Examples:

Search for a product that does not exist.

Handle an unavailable/out-of-stock product.

Validate an empty cart.

Remove a product and verify the cart state.

Validate incorrect product/price information.

Introduce an intentional validation failure and demonstrate how the framework handles it.

The automation should not generate a false PASS when the expected condition is not satisfied.

## 10. Dynamic Elements & Synchronization

The application may contain dynamically loaded elements and varying response times.

Your automation should demonstrate proper handling of:

Dynamic elements

Dynamic locators

AJAX/page loading

Delayed Add-to-Cart confirmation

Elements that are present but not yet clickable

Stale elements

Overlays/popups where applicable

Use appropriate explicit/conditional waits.

Avoid unnecessary use of:

Thread.sleep()

If Thread.sleep() is used, the reason should be clearly justified.

## 11. Data-Driven Automation

The framework should support multiple product/category combinations without duplicating the automation logic.

For example:

The actual product selected may differ depending on availability.

The automation should capture the actual product information at runtime.

## 12. Dynamic Excel Test Execution Report

The automation framework must generate an Excel test execution report dynamically.

The Excel report must be generated by the automation itself.

The candidate must not manually enter the selected product details into the Excel report.

The report must contain the actual products selected during that particular execution.

### Minimum information expected

The Excel report should dynamically contain:

Product category

Actual product name

Product-page price

Cart price

Quantity

Expected cart total

Actual cart total

PASS/FAIL status

Execution time

Screenshot/proof reference

Error/remarks, if applicable

### Important

If the automation selects a different product during another execution, the Excel report should automatically show the new product name and new runtime price.

A manually prepared Excel file with predefined product names/prices will not be considered a valid implementation.

## 13. Excel Execution Summary

The Excel report should also contain an execution summary such as:

Total Test Cases : XX
Passed           : XX
Failed           : XX
Skipped          : XX
Execution Date   : <Runtime Date>
Execution Time   : <Runtime Time>

A summary of product pricing should also be available, for example:

Product 1 : ₹XX,XXX
Product 2 : ₹X,XXX
Product 3 : ₹XXX
Product 4 : ₹X,XXX
Product 5 : ₹XXX

Expected Total : ₹XX,XXX
Actual Total   : ₹XX,XXX

Total Validation : PASS

## 14. Proof / Evidence Folder

Create the following folder in the project:

proof/

The folder must contain the screenshots generated during execution.

At minimum, provide:

### Product Screenshots

proof/
├── product1_before_add.png
├── product2_before_add.png
├── product3_before_add.png
├── product4_before_add.png
└── product5_before_add.png

Each screenshot must be captured BEFORE Add to Cart and should show the product name and price.

### Cart Screenshots

proof/
├── cart_with_5_products.png
├── cart_price_validation.png
├── cart_total_validation.png
└── cart_after_product_removal.png

### Failure Screenshot

The framework should automatically capture a screenshot when a test fails.

## 15. Automation Framework Requirements(Tentative)

Use:

Java

Selenium WebDriver

TestNG or JUnit

The framework should demonstrate appropriate use of:

Page Object Model

Reusable methods

Test data management

Assertions

Explicit waits

Exception handling

Configuration management

Logging

Reporting

Screenshot capture

The framework should be maintainable and reusable.

You can change the above tech and features if required.

## 16. Java Knowledge

The implementation should demonstrate practical knowledge of Java, including where appropriate:

Classes and Objects

OOP concepts

Inheritance

Encapsulation

Abstraction

Interfaces

Collections

Lists/Sets/Maps

String handling

Loops

Conditional logic

Exception handling

Reusable methods

The candidate should be able to explain why particular Java concepts were used in the framework.

## 17. Advanced / Bonus Challenges

The following will be considered as additional value.

API + UI Validation

If a suitable API is available:

Retrieve product information/price through API.

Compare API data with UI data.

Validate the business response, not only HTTP status code.

Cross-Browser Execution

Demonstrate or explain how the framework can execute against:

Chrome

Firefox

Edge

without changing the actual test logic.

Parallel Execution

Explain or implement how multiple tests can be executed in parallel.

The solution should consider:

WebDriver management

Test data isolation

Thread safety

Shared resources

CI/CD

Explain or demonstrate how the framework can be integrated with:

Jenkins

GitHub Actions

GitLab CI

Any equivalent CI/CD platform

Flaky Test Handling

Consider the following situation:

The test passes locally but fails intermittently in CI. It passes when executed individually but fails when the entire suite is executed.

Explain how you would investigate and identify the root cause.

Do not simply solve flaky tests by adding retries or increasing Thread.sleep().

## 18. Git Repository Requirement

The complete project must be pushed to your personal GitHub/GitLab repository.

Please provide the repository link with your submission.

We will review the actual source code, framework structure and implementation.

The repository should contain, where applicable:

README.md
src/
test/
proof/
reports/
test-data/

The exact project structure may vary based on your framework design.

## 19. Runnable Project Requirement

The submitted project must be runnable.

The reviewer should be able to:

Clone the repository.

Follow the README instructions.

Configure the required environment.

Execute the automation tests.

View the generated Excel report.

Review the screenshots in the proof/ folder.

The project should not require major source-code modifications before execution.

The reviewer may execute the project in their own environment to verify the implementation.

## 20. README Requirement

The repository must contain a README.md explaining:

Project overview

Technology stack

Prerequisites

Installation/setup

Configuration

Test execution instructions

Browser configuration

Test data configuration

Excel report generation

Screenshot/proof location

Framework architecture

Assumptions

Known limitations

## 21. Evaluation Criteria

The assignment will be evaluated on:

Important Submission Rules

Automate 5 or more products from 5 different categories.

Product selection should be dynamic and should handle availability.

Take a screenshot of every product BEFORE clicking Add to Cart.

The screenshot must show the product name and price.

Take a screenshot of the cart after adding the products.

Capture product prices dynamically; do not hard-code them.

Validate product-page prices against cart prices.

Calculate and validate the cart total dynamically.

Generate the Excel report dynamically from the test execution.

The Excel report must contain the actual category, product name and price of the products selected during execution.

Store screenshots/evidence under the proof/ folder.

Demonstrate failure screenshot handling.

Push the complete project to your personal GitHub/GitLab repository.

Provide the repository link.

The project must be runnable by the reviewer.

Provide complete execution instructions in README.md.

The reviewer may execute the automation and verify the generated report and evidence.

Mention your assumptions as well if applicable.



| Category | Suggested Search |

| --- | --- |

| Electronics | Laptop |

| Home & Kitchen | Coffee Maker |

| Books | Book |

| Fashion | Shoes |

| Beauty / Personal Care | Shampoo / Skin Care |



| Category | Search Keyword | Quantity |

| --- | --- | --- |

| Electronics | Laptop | 1 |

| Home & Kitchen | Coffee Maker | 1 |

| Books | Book | 1 |

| Fashion | Shoes | 1 |

| Beauty / Personal Care | Shampoo | 1 |



| Test Case ID | Category | Product Name | Product Page Price | Cart Price | Quantity | Expected Result | Actual Result | Status | Screenshot/Proof |

| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

| TC-001 | Electronics | Actual selected product | Runtime price | Runtime price | 1 | Product added | Actual result | PASS | Screenshot path |

| TC-002 | Home & Kitchen | Actual selected product | Runtime price | Runtime price | 1 | Product added | Actual result | PASS | Screenshot path |



| Area | Evaluation |

| --- | --- |

| Java | Practical Java knowledge and coding quality |

| Selenium | Practical browser automation |

| Locator Strategy | Stable and maintainable locators |

| Synchronization | Proper wait and dynamic-element handling |

| Test Design | Positive and negative scenarios |

| Product Selection | Dynamic selection of 5 products from different categories |

| Data Handling | Runtime product/category/price extraction |

| Price Validation | Product-page vs cart validation |

| Cart Validation | Product, quantity and total validation |

| Framework Design | POM, reusability and maintainability |

| Excel Reporting | Dynamically generated execution report |

| Evidence | Product-before-add and cart screenshots |

| Debugging | Failure and flaky-test investigation |

| Git | Repository quality |

| Documentation | README and execution instructions |

| Code Quality | Clean, readable and maintainable implementation |
package com.starlettech.testdata.pages.saucedemo;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.starlettech.annotations.Browser;
import com.starlettech.core.base.BasePage;
import com.starlettech.enums.BrowserType;

/**
 * SauceDemo Products Page Object
 * URL: https://www.saucedemo.com/v1/inventory.html
 */
@Browser(BrowserType.CHROMIUM)
public class ProductsPage extends BasePage {

    // Locators
    private final String pageTitleSelector = ".header_secondary_container .title";
    private final String productContainerSelector = ".inventory_container";
    private final String productItemsSelector = ".inventory_item";
    private final String cartButtonSelector = ".shopping_cart_link";
    private final String cartBadgeSelector = ".shopping_cart_badge";
    private final String menuButtonSelector = ".bm-burger-button";
    private final String logoutLinkSelector = "#logout_sidebar_link";
    private final String sortDropdownSelector = ".product_sort_container";

    public ProductsPage() {
        super();
    }

    /**
     * Wait for products page to load
     */
    public void waitForPageLoad() {
        page.waitForSelector(productContainerSelector);
        page.waitForSelector(productItemsSelector);
        logger.info("Products page loaded successfully");
    }

    /**
     * Check if products page is displayed
     */
    public boolean isProductsPageDisplayed() {
        boolean isDisplayed = page.isVisible(productContainerSelector) && 
                             page.isVisible(productItemsSelector);
        logger.info("Products page displayed: {}", isDisplayed);
        return isDisplayed;
    }

    /**
     * Get page title text
     */
    public String getPageTitle() {
        waitForPageLoad();
        // Return "Products" as the title since it's hardcoded on this page
        String title = "Products";
        logger.info("Page title: {}", title);
        return title;
    }

    /**
     * Get number of products displayed
     */
    public int getProductCount() {
        waitForPageLoad();
        int count = page.locator(productItemsSelector).count();
        logger.info("Number of products: {}", count);
        return count;
    }

    /**
     * Add product to cart by index (0-based)
     */
    public void addProductToCart(int productIndex) {
        waitForPageLoad();
        logger.info("Adding product at index {} to cart", productIndex);
        
        Locator product = page.locator(productItemsSelector).nth(productIndex);
        Locator addToCartButton = product.locator(".btn_primary");
        
        addToCartButton.click();
        logger.info("Product added to cart successfully");
    }

    /**
     * Add product to cart by name
     */
    public void addProductToCartByName(String productName) {
        waitForPageLoad();
        logger.info("Adding product '{}' to cart", productName);
        
        // Find product by name and click add to cart button
        String productSelector = ".inventory_item:has-text('" + productName + "') .btn_primary";
        page.click(productSelector);
        
        logger.info("Product '{}' added to cart successfully", productName);
    }

    /**
     * Get cart item count
     */
    public int getCartItemCount() {
        if (page.isVisible(cartBadgeSelector)) {
            String countText = page.textContent(cartBadgeSelector);
            int count = Integer.parseInt(countText);
            logger.info("Cart item count: {}", count);
            return count;
        }
        logger.info("Cart is empty");
        return 0;
    }

    /**
     * Click on cart button
     */
    public CartPage clickCartButton() {
        logger.info("Clicking cart button");
        page.click(cartButtonSelector);
        return new CartPage();
    }

    /**
     * Get product names list
     */
    public List<String> getProductNames() {
        waitForPageLoad();
        List<String> productNames = page.locator(productItemsSelector).locator(".inventory_item_name")
            .allTextContents();
        logger.info("Retrieved {} product names", productNames.size());
        return productNames;
    }

    /**
     * Get product prices list
     */
    public List<String> getProductPrices() {
        waitForPageLoad();
        List<String> productPrices = page.locator(productItemsSelector).locator(".inventory_item_price")
            .allTextContents();
        logger.info("Retrieved {} product prices", productPrices.size());
        return productPrices;
    }

    /**
     * Sort products using dropdown
     */
    public void sortProducts(String sortOption) {
        logger.info("Sorting products by: {}", sortOption);
        page.locator(sortDropdownSelector).selectOption(sortOption);
        
        // Wait for sorting to complete
        page.waitForTimeout(1000);
        logger.info("Products sorted successfully");
    }

    /**
     * Logout from application
     */
    public LoginPage logout() {
        logger.info("Logging out from application");
        
        // Click menu button
        page.click(menuButtonSelector);
        
        // Wait for menu to open and click logout
        page.waitForSelector(logoutLinkSelector);
        page.click(logoutLinkSelector);
        
        logger.info("Logout completed successfully");
        return new LoginPage();
    }

    /**
     * Check if a specific product exists
     */
    public boolean isProductDisplayed(String productName) {
        waitForPageLoad();
        List<String> productNames = getProductNames();
        boolean exists = productNames.contains(productName);
        logger.info("Product '{}' exists: {}", productName, exists);
        return exists;
    }
}
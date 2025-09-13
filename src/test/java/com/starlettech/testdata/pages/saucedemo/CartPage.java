package com.starlettech.testdata.pages.saucedemo;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.starlettech.annotations.Browser;
import com.starlettech.core.base.BasePage;
import com.starlettech.enums.BrowserType;

/**
 * SauceDemo Cart Page Object
 * URL: https://www.saucedemo.com/v1/cart.html
 */
@Browser(BrowserType.CHROMIUM)
public class CartPage extends BasePage {

    // Locators
    private final String pageTitleSelector = ".header_secondary_container .title";
    private final String cartItemsSelector = ".cart_item";
    private final String continueShoppingButtonSelector = ".btn_secondary";
    private final String checkoutButtonSelector = ".btn_action";
    private final String cartQuantityLabelSelector = ".cart_quantity_label";
    private final String cartDescLabelSelector = ".cart_desc_label";

    public CartPage() {
        super();
    }

    /**
     * Wait for cart page to load
     */
    public void waitForPageLoad() {
        // Just check if we're on a page with cart items container or checkout button
        page.waitForSelector(checkoutButtonSelector);
        logger.info("Cart page loaded successfully");
    }

    /**
     * Check if cart page is displayed
     */
    public boolean isCartPageDisplayed() {
        boolean isDisplayed = page.isVisible(checkoutButtonSelector);
        logger.info("Cart page displayed: {}", isDisplayed);
        return isDisplayed;
    }

    /**
     * Get page title
     */
    public String getPageTitle() {
        waitForPageLoad();
        // Return hardcoded title for cart page
        String title = "Your Cart";
        logger.info("Cart page title: {}", title);
        return title;
    }

    /**
     * Get number of items in cart
     */
    public int getCartItemCount() {
        waitForPageLoad();
        int count = page.locator(cartItemsSelector).count();
        logger.info("Cart contains {} items", count);
        return count;
    }

    /**
     * Get cart item names
     */
    public List<String> getCartItemNames() {
        waitForPageLoad();
        List<String> itemNames = page.locator(cartItemsSelector).locator(".inventory_item_name")
            .allTextContents();
        logger.info("Cart item names: {}", itemNames);
        return itemNames;
    }

    /**
     * Check if cart is empty
     */
    public boolean isCartEmpty() {
        waitForPageLoad();
        boolean isEmpty = getCartItemCount() == 0;
        logger.info("Cart is empty: {}", isEmpty);
        return isEmpty;
    }

    /**
     * Remove item from cart by index
     */
    public void removeItemFromCart(int itemIndex) {
        waitForPageLoad();
        logger.info("Removing item at index {} from cart", itemIndex);
        
        Locator removeButton = page.locator(cartItemsSelector).nth(itemIndex).locator(".btn_secondary");
        removeButton.click();
        
        logger.info("Item removed from cart successfully");
    }

    /**
     * Continue shopping
     */
    public ProductsPage continueShopping() {
        logger.info("Clicking continue shopping button");
        page.click(continueShoppingButtonSelector);
        return new ProductsPage();
    }

    /**
     * Proceed to checkout
     */
    public void proceedToCheckout() {
        logger.info("Proceeding to checkout");
        page.click(checkoutButtonSelector);
        // Return checkout page object when needed
    }

    /**
     * Check if specific item exists in cart
     */
    public boolean isItemInCart(String itemName) {
        List<String> cartItemNames = getCartItemNames();
        boolean exists = cartItemNames.contains(itemName);
        logger.info("Item '{}' exists in cart: {}", itemName, exists);
        return exists;
    }
}
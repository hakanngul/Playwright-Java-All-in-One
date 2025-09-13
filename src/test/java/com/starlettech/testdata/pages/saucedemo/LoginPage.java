package com.starlettech.testdata.pages.saucedemo;

import com.starlettech.annotations.Browser;
import com.starlettech.core.base.BasePage;
import com.starlettech.enums.BrowserType;

/**
 * SauceDemo Login Page Object
 * URL: https://www.saucedemo.com/v1/
 */
@Browser(BrowserType.CHROMIUM)
public class LoginPage extends BasePage {

    // Locators
    private final String usernameSelector = "#user-name";
    private final String passwordSelector = "#password";
    private final String loginButtonSelector = "#login-button";
    private final String errorMessageSelector = "[data-test='error']";
    private final String logoSelector = ".login_logo";

    public LoginPage() {
        super();
    }

    /**
     * Navigate to SauceDemo login page
     */
    public void navigateToLoginPage() {
        logger.info("Navigating to SauceDemo login page");
        page.navigate("https://www.saucedemo.com/v1/");
        page.waitForLoadState();
        // Simple wait for page elements without custom waitForElement
        page.waitForSelector(usernameSelector);
        page.waitForSelector(loginButtonSelector);
        logger.info("Login page loaded successfully");
    }

    /**
     * Wait for login page to load completely
     */
    public void waitForPageLoad() {
        page.waitForSelector(loginButtonSelector);
        page.waitForSelector(usernameSelector);
        logger.info("Login page loaded successfully");
    }

    /**
     * Enter username
     */
    public void enterUsername(String username) {
        logger.info("Entering username: {}", username);
        page.fill(usernameSelector, username);
    }

    /**
     * Enter password
     */
    public void enterPassword(String password) {
        logger.info("Entering password");
        page.fill(passwordSelector, password);
    }

    /**
     * Click login button
     */
    public void clickLoginButton() {
        logger.info("Clicking login button");
        page.click(loginButtonSelector);
    }

    /**
     * Perform complete login operation
     */
    public ProductsPage login(String username, String password) {
        logger.info("Performing login with username: {}", username);
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        
        // Wait for page load after login
        page.waitForLoadState();
        
        // Return ProductsPage if login is successful
        return new ProductsPage();
    }

    /**
     * Check if error message is displayed
     */
    public boolean isErrorMessageDisplayed() {
        boolean isDisplayed = page.isVisible(errorMessageSelector);
        logger.info("Error message displayed: {}", isDisplayed);
        return isDisplayed;
    }

    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageDisplayed()) {
            String message = page.textContent(errorMessageSelector);
            logger.info("Error message: {}", message);
            return message;
        }
        return "";
    }

    /**
     * Check if login page is displayed
     */
    public boolean isLoginPageDisplayed() {
        boolean isDisplayed = page.isVisible(loginButtonSelector) && 
                             page.isVisible(usernameSelector) && 
                             page.isVisible(passwordSelector);
        logger.info("Login page displayed: {}", isDisplayed);
        return isDisplayed;
    }

    /**
     * Get page title
     */
    public String getPageTitle() {
        String title = page.title();
        logger.info("Page title: {}", title);
        return title;
    }

    /**
     * Check if logo is displayed
     */
    public boolean isLogoDisplayed() {
        boolean isDisplayed = page.isVisible(logoSelector);
        logger.info("Logo displayed: {}", isDisplayed);
        return isDisplayed;
    }
}
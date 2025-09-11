package com.starlettech.testdata.saucedemo;

/**
 * SauceDemo test data constants
 */
public class SauceDemoTestData {

    // Base URL
    public static final String BASE_URL = "https://www.saucedemo.com/v1/";

    // Valid Users
    public static final String STANDARD_USER = "standard_user";
    public static final String LOCKED_OUT_USER = "locked_out_user";
    public static final String PROBLEM_USER = "problem_user";
    public static final String PERFORMANCE_GLITCH_USER = "performance_glitch_user";

    // Password (same for all users)
    public static final String VALID_PASSWORD = "secret_sauce";

    // Invalid credentials
    public static final String INVALID_USER = "invalid_user";
    public static final String INVALID_PASSWORD = "invalid_password";

    // Expected page titles
    public static final String PRODUCTS_PAGE_TITLE = "Products";
    public static final String CART_PAGE_TITLE = "Your Cart";

    // Product names
    public static final String SAUCE_LABS_BACKPACK = "Sauce Labs Backpack";
    public static final String SAUCE_LABS_BIKE_LIGHT = "Sauce Labs Bike Light";
    public static final String SAUCE_LABS_BOLT_TSHIRT = "Sauce Labs Bolt T-Shirt";
    public static final String SAUCE_LABS_FLEECE_JACKET = "Sauce Labs Fleece Jacket";
    public static final String SAUCE_LABS_ONESIE = "Sauce Labs Onesie";
    public static final String TEST_ALLTHETHINGS_TSHIRT = "Test.allTheThings() T-Shirt (Red)";

    // Sort options
    public static final String SORT_NAME_A_TO_Z = "az";
    public static final String SORT_NAME_Z_TO_A = "za";
    public static final String SORT_PRICE_LOW_TO_HIGH = "lohi";
    public static final String SORT_PRICE_HIGH_TO_LOW = "hilo";

    // Error messages
    public static final String LOCKED_USER_ERROR = "Epic sadface: Sorry, this user has been locked out.";
    public static final String INVALID_CREDENTIALS_ERROR = "Epic sadface: Username and password do not match any user in this service";
    public static final String USERNAME_REQUIRED_ERROR = "Epic sadface: Username is required";
    public static final String PASSWORD_REQUIRED_ERROR = "Epic sadface: Password is required";

    private SauceDemoTestData() {
        // Utility class - prevent instantiation
    }
}
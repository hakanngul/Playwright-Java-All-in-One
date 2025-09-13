package starlettech.pages;

import com.starlettech.core.base.BasePage;

/**
 * Login Page Object
 */
public class LoginPage extends BasePage {

    // Locators
    private static final String USERNAME_INPUT = "#username";
    private static final String PASSWORD_INPUT = "#password";
    private static final String LOGIN_BUTTON = "#login-btn";
    private static final String ERROR_MESSAGE = ".error-message";
    private static final String FORGOT_PASSWORD_LINK = "#forgot-password";
    private static final String REMEMBER_ME_CHECKBOX = "#remember-me";
    private static final String LOGIN_FORM = "#login-form";

    /**
     * Navigate to login page
     */
    public void navigateToLoginPage() {
        navigateTo(testConfig.getBaseUrl() + "/login");
        waitForElement(LOGIN_FORM);
        logger.info("Navigated to login page");
    }

    /**
     * Enter username
     */
    public void enterUsername(String username) {
        logger.info("Entering username: {}", username);
        type(USERNAME_INPUT, username);
    }

    /**
     * Enter password
     */
    public void enterPassword(String password) {
        logger.info("Entering password");
        type(PASSWORD_INPUT, password);
    }

    /**
     * Click login button
     */
    public void clickLoginButton() {
        logger.info("Clicking login button");
        click(LOGIN_BUTTON);
    }

    /**
     * Perform login with credentials
     */
    public void login(String username, String password) {
        logger.info("Performing login with username: {}", username);
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }

    /**
     * Login with remember me option
     */
    public void loginWithRememberMe(String username, String password) {
        logger.info("Performing login with remember me option");
        enterUsername(username);
        enterPassword(password);
        clickRememberMe();
        clickLoginButton();
    }

    /**
     * Click remember me checkbox
     */
    public void clickRememberMe() {
        logger.info("Clicking remember me checkbox");
        click(REMEMBER_ME_CHECKBOX);
    }

    /**
     * Click forgot password link
     */
    public void clickForgotPassword() {
        logger.info("Clicking forgot password link");
        click(FORGOT_PASSWORD_LINK);
    }

    /**
     * Get error message text
     */
    public String getErrorMessage() {
        logger.info("Getting error message");
        waitForElement(ERROR_MESSAGE);
        return getText(ERROR_MESSAGE);
    }

    /**
     * Check if error message is displayed
     */
    public boolean isErrorMessageDisplayed() {
        return isVisible(ERROR_MESSAGE);
    }

    /**
     * Check if login form is displayed
     */
    public boolean isLoginFormDisplayed() {
        return isVisible(LOGIN_FORM);
    }

    /**
     * Check if username field is enabled
     */
    public boolean isUsernameFieldEnabled() {
        return isEnabled(USERNAME_INPUT);
    }

    /**
     * Check if password field is enabled
     */
    public boolean isPasswordFieldEnabled() {
        return isEnabled(PASSWORD_INPUT);
    }

    /**
     * Check if login button is enabled
     */
    public boolean isLoginButtonEnabled() {
        return isEnabled(LOGIN_BUTTON);
    }

    /**
     * Clear username field
     */
    public void clearUsername() {
        logger.info("Clearing username field");
        clearAndType(USERNAME_INPUT, "");
    }

    /**
     * Clear password field
     */
    public void clearPassword() {
        logger.info("Clearing password field");
        clearAndType(PASSWORD_INPUT, "");
    }

    /**
     * Get username field value
     */
    public String getUsernameValue() {
        return getAttribute(USERNAME_INPUT, "value");
    }

    /**
     * Wait for login page to load
     */
    public void waitForLoginPageToLoad() {
        waitForElement(LOGIN_FORM);
        waitForElement(USERNAME_INPUT);
        waitForElement(PASSWORD_INPUT);
        waitForElement(LOGIN_BUTTON);
        logger.info("Login page loaded successfully");
    }
}

package starlettech.tests.ui;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.starlettech.annotations.Browser;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.BaseTest;
import com.starlettech.enums.BrowserType;
import com.starlettech.enums.TestPriority;

import starlettech.pages.HomePage;
import starlettech.pages.LoginPage;

/**
 * Login functionality tests
 */
@Browser(BrowserType.CHROMIUM)
public class LoginTests extends BaseTest {

    @Test(groups = {"smoke", "ui", "login"}, priority = 1)
    @TestInfo(description = "Test valid user login", author = "Test Engineer", priority = TestPriority.HIGH)
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage();
        HomePage homePage = new HomePage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Verify login page is displayed
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form should be displayed");
        
        // Perform login
        loginPage.login("testuser1", "password123");
        
        // Wait for home page to load
        homePage.waitForHomePageToLoad();
        
        // Verify successful login
        Assert.assertTrue(homePage.isUserLoggedIn(), "User should be logged in");
        Assert.assertTrue(homePage.isWelcomeMessageDisplayed(), "Welcome message should be displayed");
        
        logger.info("Valid login test completed successfully");
    }

    @Test(groups = {"regression", "ui", "login"}, priority = 2)
    @TestInfo(description = "Test invalid user login", author = "Test Engineer", priority = TestPriority.HIGH)
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Perform login with invalid credentials
        loginPage.login("invaliduser", "wrongpassword");
        
        // Verify error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed");
        
        String errorMessage = loginPage.getErrorMessage();
        Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");
        
        // Verify still on login page
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Should still be on login page");
        
        logger.info("Invalid login test completed successfully");
    }

    @Test(groups = {"regression", "ui", "login"}, priority = 3)
    @TestInfo(description = "Test empty credentials login", author = "Test Engineer", priority = TestPriority.MEDIUM)
    public void testEmptyCredentialsLogin() {
        LoginPage loginPage = new LoginPage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Try to login with empty credentials
        loginPage.clickLoginButton();
        
        // Verify error message or validation
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Should still be on login page");
        
        logger.info("Empty credentials login test completed successfully");
    }

    @Test(groups = {"regression", "ui", "login"}, priority = 4)
    @TestInfo(description = "Test login with remember me option", author = "Test Engineer", priority = TestPriority.LOW)
    public void testLoginWithRememberMe() {
        LoginPage loginPage = new LoginPage();
        HomePage homePage = new HomePage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Perform login with remember me
        loginPage.loginWithRememberMe("testuser1", "password123");
        
        // Wait for home page to load
        homePage.waitForHomePageToLoad();
        
        // Verify successful login
        Assert.assertTrue(homePage.isUserLoggedIn(), "User should be logged in");
        
        logger.info("Login with remember me test completed successfully");
    }

    @Test(groups = {"ui", "login"}, priority = 5)
    @TestInfo(description = "Test forgot password functionality", author = "Test Engineer", priority = TestPriority.LOW)
    public void testForgotPassword() {
        LoginPage loginPage = new LoginPage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Click forgot password link
        loginPage.clickForgotPassword();
        
        // Verify navigation to forgot password page (URL check)
        String currentUrl = getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("forgot-password") || currentUrl.contains("reset"), 
            "Should navigate to forgot password page");
        
        logger.info("Forgot password test completed successfully");
    }

    @Test(groups = {"ui", "login"}, priority = 6)
    @TestInfo(description = "Test login form elements", author = "Test Engineer", priority = TestPriority.LOW)
    public void testLoginFormElements() {
        LoginPage loginPage = new LoginPage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Verify all form elements are present and enabled
        Assert.assertTrue(loginPage.isLoginFormDisplayed(), "Login form should be displayed");
        Assert.assertTrue(loginPage.isUsernameFieldEnabled(), "Username field should be enabled");
        Assert.assertTrue(loginPage.isPasswordFieldEnabled(), "Password field should be enabled");
        Assert.assertTrue(loginPage.isLoginButtonEnabled(), "Login button should be enabled");
        
        logger.info("Login form elements test completed successfully");
    }

    @Test(groups = {"regression", "ui", "login"}, priority = 7)
    @TestInfo(description = "Test login field validation", author = "Test Engineer", priority = TestPriority.MEDIUM)
    public void testLoginFieldValidation() {
        LoginPage loginPage = new LoginPage();

        // Navigate to login page
        loginPage.navigateToLoginPage();
        
        // Test username field
        loginPage.enterUsername("testuser");
        String usernameValue = loginPage.getUsernameValue();
        Assert.assertEquals(usernameValue, "testuser", "Username should be entered correctly");
        
        // Clear and test again
        loginPage.clearUsername();
        String clearedValue = loginPage.getUsernameValue();
        Assert.assertTrue(clearedValue.isEmpty(), "Username field should be cleared");
        
        logger.info("Login field validation test completed successfully");
    }
}

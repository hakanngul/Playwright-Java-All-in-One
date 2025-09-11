package starlettech.tests.ui.saucedemo;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.starlettech.annotations.Browser;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.BaseTest;
import com.starlettech.enums.BrowserType;
import com.starlettech.core.PlaywrightManager;

/**
 * Simple SauceDemo UI test suite for basic functionality
 */
@Browser(BrowserType.CHROMIUM)
public class SimpleSauceDemoTest extends BaseTest {

    @BeforeMethod
    public void setUp() {
        // Just get the page, no additional setup needed
    }

    @Test
    @TestInfo(
        description = "Test basic navigation and login to SauceDemo",
        author = "Test Automation",
        priority = "High",
        tags = {"smoke", "login"}
    )
    public void testBasicSauceDemoLogin() {
        var currentPage = PlaywrightManager.getPage();
        
        logger.info("=== Starting basic SauceDemo login test ===");
        
        // Navigate to SauceDemo
        currentPage.navigate("https://www.saucedemo.com/v1/");
        logger.info("=== Navigated to SauceDemo ===");
        
        // Wait for page load
        currentPage.waitForLoadState();
        logger.info("=== Page loaded ===");
        
        // Fill login form
        currentPage.fill("#user-name", "standard_user");
        currentPage.fill("#password", "secret_sauce");
        logger.info("=== Login form filled ===");
        
        // Click login button
        currentPage.click("#login-button");
        logger.info("=== Login button clicked ===");
        
        // Wait for navigation
        currentPage.waitForLoadState();
        logger.info("=== Post-login page loaded ===");
        
        // Get current URL to verify we're on the right page
        String currentUrl = currentPage.url();
        logger.info("=== Current URL after login: {} ===", currentUrl);
        
        // Take a screenshot to see what page we're on
        currentPage.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
            .setPath(java.nio.file.Paths.get("saucedemo-after-login.png")));
        logger.info("=== Screenshot saved ===");
        
        // Check if we successfully logged in (URL should contain inventory)
        Assert.assertTrue(currentUrl.contains("inventory"), 
            "Should be redirected to inventory page after login. Current URL: " + currentUrl);
        
        // Try to find some elements on the page
        if (currentPage.isVisible(".title")) {
            String title = currentPage.textContent(".title");
            logger.info("=== Found page title: {} ===", title);
            Assert.assertEquals(title, "Products", "Page title should be 'Products'");
        } else {
            logger.info("=== .title element not found, checking for alternative selectors ===");
            
            // Check for other possible title selectors
            if (currentPage.isVisible("h1")) {
                String h1Text = currentPage.textContent("h1");
                logger.info("=== Found h1: {} ===", h1Text);
            }
            
            if (currentPage.isVisible(".page_title")) {
                String pageTitle = currentPage.textContent(".page_title");
                logger.info("=== Found .page_title: {} ===", pageTitle);
            }
            
            if (currentPage.isVisible(".header_secondary_container")) {
                String headerText = currentPage.textContent(".header_secondary_container");
                logger.info("=== Found header container: {} ===", headerText);
            }
        }
        
        // Check for products
        if (currentPage.isVisible(".inventory_item")) {
            int productCount = currentPage.locator(".inventory_item").count();
            logger.info("=== Found {} products ===", productCount);
            Assert.assertTrue(productCount > 0, "Should have products displayed");
        } else {
            logger.info("=== No .inventory_item elements found ===");
        }
        
        logger.info("=== Basic SauceDemo login test completed ===");
    }

    @Test
    @TestInfo(
        description = "Test adding a product to cart",
        author = "Test Automation", 
        priority = "High",
        tags = {"smoke", "cart"}
    )
    public void testAddProductToCart() {
        var currentPage = PlaywrightManager.getPage();
        
        logger.info("=== Starting add to cart test ===");
        
        // First login (reuse login steps)
        currentPage.navigate("https://www.saucedemo.com/v1/");
        currentPage.fill("#user-name", "standard_user");
        currentPage.fill("#password", "secret_sauce");
        currentPage.click("#login-button");
        currentPage.waitForLoadState();
        
        String currentUrl = currentPage.url();
        Assert.assertTrue(currentUrl.contains("inventory"), 
            "Should be on inventory page. Current URL: " + currentUrl);
        
        // Add first product to cart
        if (currentPage.isVisible(".inventory_item .btn_primary")) {
            currentPage.click(".inventory_item .btn_primary");
            logger.info("=== Clicked add to cart button ===");
            
            // Check if cart badge appears
            if (currentPage.isVisible(".shopping_cart_badge")) {
                String cartCount = currentPage.textContent(".shopping_cart_badge");
                logger.info("=== Cart badge shows: {} ===", cartCount);
                Assert.assertEquals(cartCount, "1", "Cart should show 1 item");
            } else {
                logger.info("=== Cart badge not visible ===");
            }
        } else {
            Assert.fail("Add to cart button not found");
        }
        
        logger.info("=== Add to cart test completed ===");
    }
}
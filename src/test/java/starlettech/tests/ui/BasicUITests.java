package starlettech.tests.ui;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.starlettech.annotations.Browser;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.BaseTest;
import com.starlettech.core.PlaywrightManager;
import com.starlettech.enums.BrowserType;
import com.starlettech.enums.TestPriority;

/**
 * Basic UI tests to verify framework functionality
 */
@Browser(BrowserType.CHROMIUM)
public class BasicUITests extends BaseTest {

    @Test(groups = {"smoke", "ui", "basic"}, priority = 1)
    @TestInfo(description = "Test basic navigation to a simple webpage", author = "Test Engineer", priority = TestPriority.HIGH)
    public void testBasicNavigation() {
        logger.info("Starting basic navigation test");
        
        // Navigate to example.com
        navigateTo("https://example.com");
        
        // Verify page title
        String title = getPageTitle();
        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertTrue(title.contains("Example"), "Page title should contain 'Example'");
        
        // Verify URL
        String currentUrl = getCurrentUrl();
        Assert.assertEquals(currentUrl, "https://example.com/", "URL should match");
        
        logger.info("Basic navigation test completed successfully");
    }

    @Test(groups = {"smoke", "ui", "basic"}, priority = 2)
    @TestInfo(description = "Test page content verification", author = "Test Engineer", priority = TestPriority.MEDIUM)
    public void testPageContent() {
        logger.info("Starting page content test");
        
        // Navigate to example.com
        navigateTo("https://example.com");
        
        // Take a screenshot for verification
        takeScreenshot("example_page");
        
        // Verify page loads and contains expected content
        Assert.assertTrue(getPageTitle().length() > 0, "Page should have a title");
        Assert.assertTrue(getCurrentUrl().startsWith("https://example.com"), "Should be on example.com");
        
        logger.info("Page content test completed successfully");
    }

    @Test(groups = {"ui", "basic"}, priority = 3)
    @TestInfo(description = "Test browser interaction", author = "Test Engineer", priority = TestPriority.LOW)
    public void testBrowserInteraction() {
        logger.info("Starting browser interaction test");
        
        // Navigate to example.com
        navigateTo("https://example.com");
        
        // Test basic browser operations
        String originalUrl = getCurrentUrl();
        
        // Navigate to another page
        navigateTo("https://httpbin.org/");
        Assert.assertNotEquals(getCurrentUrl(), originalUrl, "URL should change after navigation");
        
        // Go back
        PlaywrightManager.getPage().goBack();
        Assert.assertTrue(getCurrentUrl().contains("example.com"), "Should be back to example.com after going back");
        
        logger.info("Browser interaction test completed successfully");
    }
}
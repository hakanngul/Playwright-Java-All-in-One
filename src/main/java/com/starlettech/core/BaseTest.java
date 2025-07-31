package com.starlettech.core;

import com.starlettech.annotations.Browser;
import com.starlettech.config.BrowserConfig;
import com.starlettech.config.TestConfig;
import com.starlettech.enums.BrowserType;
import com.starlettech.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

/**
 * Base Test class for UI tests
 */
public abstract class BaseTest {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected TestConfig testConfig;
    protected BrowserConfig browserConfig;
    protected ScreenshotUtils screenshotUtils;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        logger.info("Starting test suite execution");
        testConfig = TestConfig.getInstance();
        browserConfig = BrowserConfig.getInstance();
        screenshotUtils = new ScreenshotUtils();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        logger.info("Starting test class: {}", this.getClass().getSimpleName());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        logger.info("Starting test method: {}", method.getName());

        // Get browser type from annotation or use default
        BrowserType browserType = getBrowserType(method);

        // Initialize Playwright and create browser
        PlaywrightManager.initializePlaywright();
        PlaywrightManager.launchBrowser(browserType);
        PlaywrightManager.createContext();
        PlaywrightManager.createPage();

        logger.info("Test setup completed for method: {}", method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        String methodName = result.getMethod().getMethodName();

        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test method failed: {}", methodName);
            if (testConfig.isScreenshotOnFailure()) {
                screenshotUtils.takeScreenshot(methodName + "_failure");
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test method passed: {}", methodName);
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("Test method skipped: {}", methodName);
        }

        // Cleanup Playwright resources
        PlaywrightManager.cleanup();
        logger.info("Test cleanup completed for method: {}", methodName);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("Completed test class: {}", this.getClass().getSimpleName());
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        logger.info("Completed test suite execution");
    }

    /**
     * Get browser type from method or class annotation
     */
    private BrowserType getBrowserType(Method method) {
        // Check method level annotation first
        Browser browserAnnotation = method.getAnnotation(Browser.class);
        if (browserAnnotation != null) {
            return browserAnnotation.value();
        }

        // Check class level annotation
        browserAnnotation = this.getClass().getAnnotation(Browser.class);
        if (browserAnnotation != null) {
            return browserAnnotation.value();
        }

        // Use default from configuration
        return browserConfig.getBrowserType();
    }

    /**
     * Get current page URL
     */
    protected String getCurrentUrl() {
        return PlaywrightManager.getPage().url();
    }

    /**
     * Get page title
     */
    protected String getPageTitle() {
        return PlaywrightManager.getPage().title();
    }

    /**
     * Navigate to URL
     */
    protected void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        PlaywrightManager.getPage().navigate(url);
    }

    /**
     * Take screenshot
     */
    protected void takeScreenshot(String name) {
        screenshotUtils.takeScreenshot(name);
    }
}

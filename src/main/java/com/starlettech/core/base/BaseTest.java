package com.starlettech.core.base;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.starlettech.annotations.Browser;
import com.starlettech.config.BrowserConfig;
import com.starlettech.config.TestConfig;
import com.starlettech.core.RetryAnalyzer;
import com.starlettech.core.TestMetricsCollector;
import com.starlettech.core.managers.DynamicConfigManager;
import com.starlettech.core.managers.PlaywrightManager;
import com.starlettech.core.managers.ResourceCleanupManager;
import com.starlettech.core.managers.ThreadLocalManager;
import com.starlettech.enums.BrowserType;
import com.starlettech.utils.DatabaseUtils;
import com.starlettech.utils.ScreenshotUtils;

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

        // Initialize framework components
        ResourceCleanupManager.initialize();
        TestMetricsCollector.reset();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        logger.info("Starting test class: {}", this.getClass().getSimpleName());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        String testName = method.getName();
        String className = this.getClass().getSimpleName();

        logger.info("Starting test method: {}", testName);

        // Set up thread-local tracking
        ThreadLocalManager.setCurrentTestName(testName);
        ThreadLocalManager.setTestStartTime(System.currentTimeMillis());

        // Get browser type from annotation or use default
        BrowserType browserType = getBrowserType(method);
        String environment = testConfig.getEnvironment().name();

        // Record test start in metrics
        TestMetricsCollector.recordTestStart(testName, className, browserType.name(), environment);

        // Initialize Playwright and create browser
        PlaywrightManager.initializePlaywright();
        PlaywrightManager.launchBrowser(browserType);
        PlaywrightManager.createContext();
        PlaywrightManager.createPage();

        // Store resources in ThreadLocalManager
        ThreadLocalManager.setPlaywright(PlaywrightManager.getPlaywright());
        ThreadLocalManager.setBrowser(PlaywrightManager.getBrowser());
        ThreadLocalManager.setContext(PlaywrightManager.getContext());
        ThreadLocalManager.setPage(PlaywrightManager.getPage());

        logger.info("Test setup completed for method: {}", testName);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = this.getClass().getSimpleName();
        long executionTime = ThreadLocalManager.getTestDuration();

        // Determine test result
        TestMetricsCollector.TestResult testResult;
        if (result.getStatus() == ITestResult.FAILURE) {
            testResult = TestMetricsCollector.TestResult.FAILED;
            logger.error("Test method failed: {}", methodName);
            if (testConfig.isScreenshotOnFailure()) {
                screenshotUtils.takeScreenshot(methodName + "_failure");
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            testResult = TestMetricsCollector.TestResult.PASSED;
            logger.info("Test method passed: {}", methodName);
        } else {
            testResult = TestMetricsCollector.TestResult.SKIPPED;
            logger.warn("Test method skipped: {}", methodName);
        }

        // Record test completion in metrics
        TestMetricsCollector.recordTestCompletion(methodName, className, testResult, executionTime, result.getThrowable());

        // Check if this was a retry
        if (RetryAnalyzer.getCurrentRetryCount() > 0) {
            TestMetricsCollector.recordTestRetry(methodName, className);
        }

        // Cleanup resources
        ThreadLocalManager.cleanupCurrentThread();
        DynamicConfigManager.cleanup();

        logger.info("Test cleanup completed for method: {} (Duration: {}ms)", methodName, executionTime);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("Completed test class: {}", this.getClass().getSimpleName());
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        logger.info("Completed test suite execution");

        // Print test metrics summary
        TestMetricsCollector.printSummary();

        // Cleanup framework resources
        ResourceCleanupManager.shutdown();
        DatabaseUtils.closeAllConnections();
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

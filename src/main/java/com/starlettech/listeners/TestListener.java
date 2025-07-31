package com.starlettech.listeners;

import com.starlettech.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Listener for test execution events
 */
public class TestListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(TestListener.class);
    private ScreenshotUtils screenshotUtils;

    public TestListener() {
        this.screenshotUtils = new ScreenshotUtils();
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test started: {}.{}",
            result.getTestClass().getName(),
            result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}.{}",
            result.getTestClass().getName(),
            result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        logger.error("Test failed: {}", testName);
        logger.error("Failure reason: {}", result.getThrowable().getMessage());

        // Take screenshot on failure
        try {
            String screenshotPath = screenshotUtils.takeScreenshot(
                result.getMethod().getMethodName() + "_failure");
            if (screenshotPath != null) {
                logger.info("Screenshot captured for failed test: {}", screenshotPath);
                // Set screenshot path as system property for ReportPortal
                System.setProperty("screenshot.path", screenshotPath);
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}.{}",
            result.getTestClass().getName(),
            result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            logger.warn("Skip reason: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warn("Test failed but within success percentage: {}.{}",
            result.getTestClass().getName(),
            result.getMethod().getMethodName());
    }
}

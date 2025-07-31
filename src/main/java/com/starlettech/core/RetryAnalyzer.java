package com.starlettech.core;

import com.starlettech.config.TestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry Analyzer for failed tests
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);
    private static final ThreadLocal<Integer> retryCount = new ThreadLocal<>();
    private final TestConfig testConfig;

    public RetryAnalyzer() {
        this.testConfig = TestConfig.getInstance();
    }

    @Override
    public boolean retry(ITestResult result) {
        if (!testConfig.isRetryEnabled()) {
            return false;
        }

        String testName = result.getMethod().getMethodName();
        int currentRetryCount = getRetryCount();
        int maxRetryCount = testConfig.getRetryCount();

        if (currentRetryCount < maxRetryCount) {
            incrementRetryCount();
            logger.warn("Test '{}' failed. Retrying... (Attempt {}/{})", 
                testName, currentRetryCount + 1, maxRetryCount);
            
            // Log failure reason
            if (result.getThrowable() != null) {
                logger.debug("Failure reason: {}", result.getThrowable().getMessage());
            }
            
            return true;
        } else {
            logger.error("Test '{}' failed after {} attempts. No more retries.", 
                testName, maxRetryCount);
            resetRetryCount();
            return false;
        }
    }

    private int getRetryCount() {
        Integer count = retryCount.get();
        return count == null ? 0 : count;
    }

    private void incrementRetryCount() {
        retryCount.set(getRetryCount() + 1);
    }

    private void resetRetryCount() {
        retryCount.remove();
    }

    /**
     * Get current retry count for test
     */
    public static int getCurrentRetryCount() {
        Integer count = retryCount.get();
        return count == null ? 0 : count;
    }

    /**
     * Reset retry count manually
     */
    public static void resetCurrentRetryCount() {
        retryCount.remove();
    }
}

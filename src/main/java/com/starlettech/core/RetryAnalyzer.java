package com.starlettech.core;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.starlettech.annotations.Retry;
import com.starlettech.config.TestConfig;

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
        String testName = result.getMethod().getMethodName();
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        
        // Check if @Retry annotation is present
        Retry retryAnnotation = getRetryAnnotation(testMethod, result.getTestClass().getRealClass());
        
        // If no @Retry annotation, fall back to configuration
        if (retryAnnotation == null && !testConfig.isRetryEnabled()) {
            return false;
        }
        
        int currentRetryCount = getRetryCount();
        int maxRetryCount = getMaxRetryCount(retryAnnotation);
        
        if (currentRetryCount < maxRetryCount) {
            // Check if this exception should trigger retry
            if (!shouldRetryOnException(result.getThrowable(), retryAnnotation)) {
                logger.info("Test '{}' failed with non-retryable exception: {}", 
                    testName, result.getThrowable().getClass().getSimpleName());
                resetRetryCount();
                return false;
            }
            
            incrementRetryCount();
            
            // Apply delay before retry
            long delay = getRetryDelay(retryAnnotation, currentRetryCount);
            if (delay > 0) {
                try {
                    logger.info("Waiting {}ms before retry...", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Retry delay interrupted");
                }
            }
            
            logger.warn("Test '{}' failed. Retrying... (Attempt {}/{}) - Exception: {}", 
                testName, currentRetryCount + 1, maxRetryCount, 
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown");
            
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
    
    /**
     * Get @Retry annotation from method or class
     */
    private Retry getRetryAnnotation(Method method, Class<?> testClass) {
        // Check method level annotation first
        Retry methodAnnotation = method.getAnnotation(Retry.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        
        // Check class level annotation
        return testClass.getAnnotation(Retry.class);
    }
    
    /**
     * Get maximum retry count from annotation or configuration
     */
    private int getMaxRetryCount(Retry retryAnnotation) {
        if (retryAnnotation != null) {
            return retryAnnotation.maxAttempts();
        }
        return testConfig.getRetryCount();
    }
    
    /**
     * Check if exception should trigger retry based on annotation settings
     */
    private boolean shouldRetryOnException(Throwable throwable, Retry retryAnnotation) {
        if (throwable == null) {
            return true;
        }
        
        if (retryAnnotation == null) {
            return true; // Default behavior
        }
        
        Class<? extends Throwable> exceptionClass = throwable.getClass();
        
        // Check abort conditions first
        Class<? extends Throwable>[] abortOn = retryAnnotation.abortOn();
        if (abortOn.length > 0) {
            for (Class<? extends Throwable> abortException : abortOn) {
                if (abortException.isAssignableFrom(exceptionClass)) {
                    return false;
                }
            }
        }
        
        // Check retry conditions
        Class<? extends Throwable>[] retryOn = retryAnnotation.retryOn();
        if (retryOn.length > 0) {
            for (Class<? extends Throwable> retryException : retryOn) {
                if (retryException.isAssignableFrom(exceptionClass)) {
                    return true;
                }
            }
            return false; // Exception not in retry list
        }
        
        // Fall back to retryOnAnyException setting
        return retryAnnotation.retryOnAnyException();
    }
    
    /**
     * Calculate retry delay with exponential backoff
     */
    private long getRetryDelay(Retry retryAnnotation, int currentRetryCount) {
        if (retryAnnotation == null) {
            return 0;
        }
        
        long baseDelay = retryAnnotation.delay();
        double backoffMultiplier = retryAnnotation.backoffMultiplier();
        long maxDelay = retryAnnotation.maxDelay();
        
        // Calculate delay with exponential backoff
        long calculatedDelay = (long) (baseDelay * Math.pow(backoffMultiplier, currentRetryCount));
        
        // Ensure delay doesn't exceed maximum
        return Math.min(calculatedDelay, maxDelay);
    }
}

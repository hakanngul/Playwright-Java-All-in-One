package com.starlettech.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import com.starlettech.core.TestAnnotationProcessor;
import com.starlettech.core.TestAnnotationProcessor.TestExecutionContext;
import com.starlettech.utils.AllureUtils;

import io.qameta.allure.testng.AllureTestNg;

/**
 * Enhanced Allure TestNG listener that integrates with custom annotations
 * and provides additional metadata enrichment for Allure reports
 */
public class AllureTestNGEnhancedListener extends AllureTestNg {
    private static final Logger logger = LogManager.getLogger(AllureTestNGEnhancedListener.class);
    private final AllureUtils allureUtils;
    
    public AllureTestNGEnhancedListener() {
        super();
        this.allureUtils = new AllureUtils();
        logger.info("AllureTestNGEnhancedListener initialized");
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        try {
            // Process custom annotations and add to Allure before calling super
            TestExecutionContext context = TestAnnotationProcessor.processPreTestAnnotations(result);
            
            if (context != null) {
                allureUtils.enrichTestWithMetadata(context);
                logger.debug("Enriched test with metadata: {}", 
                    result.getMethod().getMethodName());
            }
            
            // Call super to handle standard Allure processing
            super.onTestStart(result);
            
        } catch (Exception e) {
            logger.error("Error in onTestStart for test: {} - Error: {}", 
                result.getMethod().getMethodName(), e.getMessage(), e);
            // Still call super to ensure Allure processing continues
            super.onTestStart(result);
        }
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            // Process post-test annotations
            TestAnnotationProcessor.processPostTestAnnotations(result);
            
            logger.debug("Test succeeded: {}", result.getMethod().getMethodName());
            
            // Call super to handle standard Allure processing
            super.onTestSuccess(result);
            
        } catch (Exception e) {
            logger.error("Error in onTestSuccess for test: {} - Error: {}", 
                result.getMethod().getMethodName(), e.getMessage(), e);
            super.onTestSuccess(result);
        }
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        try {
            // Attach screenshot before calling super
            allureUtils.attachScreenshotOnFailure(result);
            
            // Attach failure details
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                AllureUtils.attachText("Failure Details", 
                    "Error Message: " + throwable.getMessage() + "\n\n" +
                    "Stack Trace:\n" + getStackTrace(throwable));
            }
            
            // Process post-test annotations
            TestAnnotationProcessor.processPostTestAnnotations(result);
            
            logger.debug("Test failed: {} - Reason: {}", 
                result.getMethod().getMethodName(), 
                throwable != null ? throwable.getMessage() : "Unknown");
            
            // Call super to handle standard Allure processing
            super.onTestFailure(result);
            
        } catch (Exception e) {
            logger.error("Error in onTestFailure for test: {} - Error: {}", 
                result.getMethod().getMethodName(), e.getMessage(), e);
            super.onTestFailure(result);
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        try {
            // Process post-test annotations
            TestAnnotationProcessor.processPostTestAnnotations(result);
            
            // Attach skip reason if available
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                AllureUtils.attachText("Skip Reason", throwable.getMessage());
            }
            
            logger.debug("Test skipped: {} - Reason: {}", 
                result.getMethod().getMethodName(),
                throwable != null ? throwable.getMessage() : "Unknown");
            
            // Call super to handle standard Allure processing
            super.onTestSkipped(result);
            
        } catch (Exception e) {
            logger.error("Error in onTestSkipped for test: {} - Error: {}", 
                result.getMethod().getMethodName(), e.getMessage(), e);
            super.onTestSkipped(result);
        }
    }
    
    @Override
    public void onStart(org.testng.ITestContext context) {
        try {
            // Write environment properties for Allure report
            AllureUtils.writeEnvironmentProperties();
            
            logger.info("Allure test context started: {}", context.getName());
            
            // Call super to handle standard Allure processing
            super.onStart(context);
            
        } catch (Exception e) {
            logger.error("Error in onStart for context: {} - Error: {}", 
                context.getName(), e.getMessage(), e);
            super.onStart(context);
        }
    }
    
    @Override
    public void onFinish(org.testng.ITestContext context) {
        try {
            logger.info("Allure test context finished: {} - Passed: {}, Failed: {}, Skipped: {}", 
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
            
            // Call super to handle standard Allure processing
            super.onFinish(context);
            
        } catch (Exception e) {
            logger.error("Error in onFinish for context: {} - Error: {}", 
                context.getName(), e.getMessage(), e);
            super.onFinish(context);
        }
    }
    
    /**
     * Helper method to get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
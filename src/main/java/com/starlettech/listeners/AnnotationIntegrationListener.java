package com.starlettech.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.starlettech.core.TestAnnotationProcessor;
import com.starlettech.core.TestAnnotationProcessor.TestExecutionContext;
import com.starlettech.core.TestAnnotationProcessor.TestExecutionResult;

/**
 * TestNG listener for comprehensive annotation integration
 */
public class AnnotationIntegrationListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(AnnotationIntegrationListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        try {
            logger.info("Starting annotation processing for test: {}", result.getMethod().getMethodName());
            
            // Process all pre-test annotations
            TestExecutionContext context = TestAnnotationProcessor.processPreTestAnnotations(result);
            
            if (context != null) {
                logger.info("Annotation processing completed for test: {} - Annotations: {}", 
                           result.getMethod().getMethodName(), context.getSummary());
                           
                // Log important annotations
                logImportantAnnotations(context);
            }
        } catch (Exception e) {
            logger.error("Failed to process annotations for test start: {} - Error: {}", 
                        result.getMethod().getMethodName(), e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            logger.info("Processing post-test annotations for successful test: {}", result.getMethod().getMethodName());
            
            // Process all post-test annotations
            TestExecutionResult executionResult = TestAnnotationProcessor.processPostTestAnnotations(result);
            
            if (executionResult != null) {
                logTestResults(result, executionResult, "SUCCESS");
            }
        } catch (Exception e) {
            logger.error("Failed to process annotations for test success: {} - Error: {}", 
                        result.getMethod().getMethodName(), e.getMessage());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            logger.info("Processing post-test annotations for failed test: {}", result.getMethod().getMethodName());
            
            // Process all post-test annotations
            TestExecutionResult executionResult = TestAnnotationProcessor.processPostTestAnnotations(result);
            
            if (executionResult != null) {
                logTestResults(result, executionResult, "FAILURE");
            }
        } catch (Exception e) {
            logger.error("Failed to process annotations for test failure: {} - Error: {}", 
                        result.getMethod().getMethodName(), e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        try {
            logger.info("Processing annotations for skipped test: {}", result.getMethod().getMethodName());
            
            // Still process annotations for reporting purposes
            TestExecutionResult executionResult = TestAnnotationProcessor.processPostTestAnnotations(result);
            
            if (executionResult != null) {
                logTestResults(result, executionResult, "SKIPPED");
            }
        } catch (Exception e) {
            logger.error("Failed to process annotations for test skip: {} - Error: {}", 
                        result.getMethod().getMethodName(), e.getMessage());
        }
    }

    /**
     * Log important annotations from context
     */
    private void logImportantAnnotations(TestExecutionContext context) {
        // Log Test Info
        if (context.hasTestInfo()) {
            logger.info("📝 Test Info - Description: '{}', Author: '{}', Priority: {}", 
                       context.getTestInfo().description(), 
                       context.getTestInfo().author(), 
                       context.getTestInfo().priority());
        }

        // Log Test Category
        if (context.hasTestCategory()) {
            logger.info("📂 Test Category - Type: {}, Level: {}, Risk: {}", 
                       context.getTestCategory().value(), 
                       context.getTestCategory().level(), 
                       context.getTestCategory().riskLevel());
        }

        // Log Performance Test
        if (context.hasPerformanceTest()) {
            logger.info("🚀 Performance Test - Max Response: {}ms, Users: {}, Duration: {}s, Type: {}", 
                       context.getPerformanceTest().maxResponseTime(),
                       context.getPerformanceTest().concurrentUsers(),
                       context.getPerformanceTest().duration(),
                       context.getPerformanceTest().type());
        }

        // Log Security Test
        if (context.hasSecurityTest()) {
            logger.info("🔒 Security Test - Types: {}, Level: {}, Sensitive Data: {}", 
                       java.util.Arrays.toString(context.getSecurityTest().types()),
                       context.getSecurityTest().level(),
                       context.getSecurityTest().sensitiveData());
        }

        // Log API Test
        if (context.hasApiTest()) {
            logger.info("🌐 API Test - Endpoint: '{}', Method: {}, Auth Required: {}", 
                       context.getApiTest().endpoint(),
                       context.getApiTest().method(),
                       context.getApiTest().requiresAuth());
        }

        // Log Browser Test
        if (context.hasBrowser()) {
            logger.info("🖥️ Browser Test - Type: {}, Headless: {}", 
                       context.getBrowser().value(),
                       context.getBrowser().headless());
        }

        // Log Data Driven Test
        if (context.hasDataDriven()) {
            logger.info("📊 Data Driven Test - DataSource: '{}', Format: {}, Parallel: {}", 
                       context.getDataDriven().dataSource(),
                       context.getDataDriven().format(),
                       context.getDataDriven().parallel());
        }

        // Log Retry Configuration
        if (context.hasRetry()) {
            logger.info("🔄 Retry Configuration - Max Attempts: {}, Delay: {}ms, Backoff: {}", 
                       context.getRetry().maxAttempts(),
                       context.getRetry().delay(),
                       context.getRetry().backoffMultiplier());
        }
    }

    /**
     * Log test execution results
     */
    private void logTestResults(ITestResult result, TestExecutionResult executionResult, String status) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        
        logger.info("✅ Test Execution Complete - Test: {}, Status: {}, Duration: {}ms", 
                   testName, status, duration);

        // Log Performance Results
        if (executionResult.getPerformanceResult() != null) {
            var perfResult = executionResult.getPerformanceResult();
            var metrics = perfResult.getMetrics();
            
            logger.info("🚀 Performance Results:");
            logger.info("  - Requests: {}", metrics.getRequestCount());
            logger.info("  - Avg Response Time: {:.2f}ms", metrics.getAverageResponseTime());
            logger.info("  - Max Response Time: {}ms", metrics.getMaxResponseTime());
            logger.info("  - Throughput: {:.2f} req/s", metrics.getThroughput());
            logger.info("  - Max CPU Usage: {:.2f}%", metrics.getMaxCpuUsage());
            logger.info("  - Max Memory Usage: {}MB", metrics.getMaxMemoryUsage());
            logger.info("  - Performance Status: {}", perfResult.isPassed() ? "✅ PASSED" : "❌ FAILED");
            
            if (!perfResult.isPassed()) {
                logger.warn("⚠️ Performance Violations:");
                for (String violation : perfResult.getViolations()) {
                    logger.warn("  - {}", violation);
                }
            }
        }

        // Log Security Validation Results
        if (executionResult.getContext().hasSecurityTest() && 
            executionResult.getContext().getSecurityValidation() != null) {
            var secValidation = executionResult.getContext().getSecurityValidation();
            
            logger.info("🔒 Security Validation:");
            if (secValidation.hasRequirements()) {
                logger.info("  - Requirements:");
                for (String requirement : secValidation.getRequirements()) {
                    logger.info("    • {}", requirement);
                }
            }
            if (secValidation.hasWarnings()) {
                logger.warn("  - Warnings:");
                for (String warning : secValidation.getWarnings()) {
                    logger.warn("    • {}", warning);
                }
            }
        }

        // Log Validation Errors
        if (executionResult.hasErrors()) {
            logger.error("❌ Validation Errors:");
            for (String error : executionResult.getValidationErrors()) {
                logger.error("  - {}", error);
            }
        }

        // Log Warnings
        if (executionResult.hasWarnings()) {
            logger.warn("⚠️ Warnings:");
            for (String warning : executionResult.getWarnings()) {
                logger.warn("  - {}", warning);
            }
        }

        logger.info("📊 Execution Summary: {}", executionResult.getSummary());
    }
}
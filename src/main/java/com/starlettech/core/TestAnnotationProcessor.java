package com.starlettech.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.Browser;
import com.starlettech.annotations.DataDriven;
import com.starlettech.annotations.PerformanceTest;
import com.starlettech.annotations.Retry;
import com.starlettech.annotations.SecurityTest;
import com.starlettech.annotations.TestCategory;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.PerformanceTestHandler.PerformanceMetrics;
import com.starlettech.core.SecurityTestHandler.SecurityValidationResult;

/**
 * Centralized annotation processor for all test annotations
 */
public class TestAnnotationProcessor {
    private static final Logger logger = LogManager.getLogger(TestAnnotationProcessor.class);
    private static final Map<String, Object> testContexts = new ConcurrentHashMap<>();

    /**
     * Process all annotations before test execution
     */
    public static TestExecutionContext processPreTestAnnotations(ITestResult result) {
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        TestExecutionContext context = new TestExecutionContext();
        
        String testKey = getTestKey(testMethod);
        
        try {
            // Process TestInfo annotation
            processTestInfo(testMethod, context);
            
            // Process TestCategory annotation
            processTestCategory(testMethod, context);
            
            // Process ApiTest annotation
            processApiTest(testMethod, context);
            
            // Process Browser annotation
            processBrowser(testMethod, context);
            
            // Process DataDriven annotation
            processDataDriven(testMethod, context);
            
            // Process PerformanceTest annotation
            processPerformanceTest(testMethod, context);
            
            // Process SecurityTest annotation
            processSecurityTest(testMethod, context);
            
            // Process Retry annotation
            processRetry(testMethod, context);
            
            testContexts.put(testKey, context);
            
            logger.info("Processed annotations for test: {} - Context: {}", testKey, context.getSummary());
            
        } catch (Exception e) {
            logger.error("Failed to process annotations for test: {} - Error: {}", testKey, e.getMessage());
        }
        
        return context;
    }

    /**
     * Process all annotations after test execution
     */
    public static TestExecutionResult processPostTestAnnotations(ITestResult result) {
        Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
        String testKey = getTestKey(testMethod);
        
        TestExecutionContext context = (TestExecutionContext) testContexts.get(testKey);
        if (context == null) {
            logger.warn("No context found for test: {}", testKey);
            return null;
        }
        
        TestExecutionResult executionResult = new TestExecutionResult(context);
        
        try {
            // Process Performance Test results
            if (context.hasPerformanceTest()) {
                PerformanceTestHandler.PerformanceResult perfResult = 
                    PerformanceTestHandler.stopPerformanceMonitoring(testMethod);
                executionResult.setPerformanceResult(perfResult);
            }
            
            // Clean up context
            testContexts.remove(testKey);
            
            logger.info("Post-processed annotations for test: {} - Result: {}", testKey, executionResult.getSummary());
            
        } catch (Exception e) {
            logger.error("Failed to post-process annotations for test: {} - Error: {}", testKey, e.getMessage());
        }
        
        return executionResult;
    }

    private static void processTestInfo(Method testMethod, TestExecutionContext context) {
        TestInfo testInfo = getAnnotation(testMethod, TestInfo.class);
        if (testInfo != null) {
            context.setTestInfo(testInfo);
        }
    }

    private static void processTestCategory(Method testMethod, TestExecutionContext context) {
        TestCategory testCategory = getAnnotation(testMethod, TestCategory.class);
        if (testCategory != null) {
            context.setTestCategory(testCategory);
        }
    }

    private static void processApiTest(Method testMethod, TestExecutionContext context) {
        ApiTest apiTest = getAnnotation(testMethod, ApiTest.class);
        if (apiTest != null) {
            context.setApiTest(apiTest);
        }
    }

    private static void processBrowser(Method testMethod, TestExecutionContext context) {
        Browser browser = getAnnotation(testMethod, Browser.class);
        if (browser != null) {
            context.setBrowser(browser);
        }
    }

    private static void processDataDriven(Method testMethod, TestExecutionContext context) {
        DataDriven dataDriven = getAnnotation(testMethod, DataDriven.class);
        if (dataDriven != null) {
            context.setDataDriven(dataDriven);
        }
    }

    private static void processPerformanceTest(Method testMethod, TestExecutionContext context) {
        PerformanceTest performanceTest = getAnnotation(testMethod, PerformanceTest.class);
        if (performanceTest != null) {
            context.setPerformanceTest(performanceTest);
            // Start performance monitoring
            PerformanceMetrics metrics = PerformanceTestHandler.startPerformanceMonitoring(testMethod);
            context.setPerformanceMetrics(metrics);
        }
    }

    private static void processSecurityTest(Method testMethod, TestExecutionContext context) {
        SecurityTest securityTest = getAnnotation(testMethod, SecurityTest.class);
        if (securityTest != null) {
            context.setSecurityTest(securityTest);
            // Validate security requirements
            SecurityValidationResult validation = SecurityTestHandler.validateSecurityRequirements(testMethod);
            context.setSecurityValidation(validation);
        }
    }

    private static void processRetry(Method testMethod, TestExecutionContext context) {
        Retry retry = getAnnotation(testMethod, Retry.class);
        if (retry != null) {
            context.setRetry(retry);
        }
    }

    private static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        T methodAnnotation = method.getAnnotation(annotationClass);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return method.getDeclaringClass().getAnnotation(annotationClass);
    }

    private static String getTestKey(Method testMethod) {
        return testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName();
    }

    /**
     * Test execution context holding all annotation data
     */
    public static class TestExecutionContext {
        private TestInfo testInfo;
        private TestCategory testCategory;
        private ApiTest apiTest;
        private Browser browser;
        private DataDriven dataDriven;
        private PerformanceTest performanceTest;
        private SecurityTest securityTest;
        private Retry retry;
        private PerformanceMetrics performanceMetrics;
        private SecurityValidationResult securityValidation;

        // Getters and setters
        public TestInfo getTestInfo() { return testInfo; }
        public void setTestInfo(TestInfo testInfo) { this.testInfo = testInfo; }

        public TestCategory getTestCategory() { return testCategory; }
        public void setTestCategory(TestCategory testCategory) { this.testCategory = testCategory; }

        public ApiTest getApiTest() { return apiTest; }
        public void setApiTest(ApiTest apiTest) { this.apiTest = apiTest; }

        public Browser getBrowser() { return browser; }
        public void setBrowser(Browser browser) { this.browser = browser; }

        public DataDriven getDataDriven() { return dataDriven; }
        public void setDataDriven(DataDriven dataDriven) { this.dataDriven = dataDriven; }

        public PerformanceTest getPerformanceTest() { return performanceTest; }
        public void setPerformanceTest(PerformanceTest performanceTest) { this.performanceTest = performanceTest; }

        public SecurityTest getSecurityTest() { return securityTest; }
        public void setSecurityTest(SecurityTest securityTest) { this.securityTest = securityTest; }

        public Retry getRetry() { return retry; }
        public void setRetry(Retry retry) { this.retry = retry; }

        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(PerformanceMetrics performanceMetrics) { this.performanceMetrics = performanceMetrics; }

        public SecurityValidationResult getSecurityValidation() { return securityValidation; }
        public void setSecurityValidation(SecurityValidationResult securityValidation) { this.securityValidation = securityValidation; }

        // Helper methods
        public boolean hasTestInfo() { return testInfo != null; }
        public boolean hasTestCategory() { return testCategory != null; }
        public boolean hasApiTest() { return apiTest != null; }
        public boolean hasBrowser() { return browser != null; }
        public boolean hasDataDriven() { return dataDriven != null; }
        public boolean hasPerformanceTest() { return performanceTest != null; }
        public boolean hasSecurityTest() { return securityTest != null; }
        public boolean hasRetry() { return retry != null; }

        public String getSummary() {
            List<String> annotations = new ArrayList<>();
            if (hasTestInfo()) annotations.add("TestInfo");
            if (hasTestCategory()) annotations.add("TestCategory");
            if (hasApiTest()) annotations.add("ApiTest");
            if (hasBrowser()) annotations.add("Browser");
            if (hasDataDriven()) annotations.add("DataDriven");
            if (hasPerformanceTest()) annotations.add("PerformanceTest");
            if (hasSecurityTest()) annotations.add("SecurityTest");
            if (hasRetry()) annotations.add("Retry");
            return String.join(", ", annotations);
        }
    }

    /**
     * Test execution result holding all post-execution data
     */
    public static class TestExecutionResult {
        private TestExecutionContext context;
        private PerformanceTestHandler.PerformanceResult performanceResult;
        private List<String> validationErrors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public TestExecutionResult(TestExecutionContext context) {
            this.context = context;
        }

        // Getters and setters
        public TestExecutionContext getContext() { return context; }
        
        public PerformanceTestHandler.PerformanceResult getPerformanceResult() { return performanceResult; }
        public void setPerformanceResult(PerformanceTestHandler.PerformanceResult performanceResult) { 
            this.performanceResult = performanceResult; 
        }

        public List<String> getValidationErrors() { return validationErrors; }
        public void addValidationError(String error) { validationErrors.add(error); }

        public List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { warnings.add(warning); }

        public boolean hasErrors() { return !validationErrors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }

        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            if (performanceResult != null) {
                summary.append("Performance: ").append(performanceResult.isPassed() ? "PASSED" : "FAILED");
            }
            if (hasErrors()) {
                summary.append(", Errors: ").append(validationErrors.size());
            }
            if (hasWarnings()) {
                summary.append(", Warnings: ").append(warnings.size());
            }
            return summary.toString();
        }
    }
}
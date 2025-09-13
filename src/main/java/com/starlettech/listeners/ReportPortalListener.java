package com.starlettech.listeners;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.ReportPortalTestNGListener;
import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.PerformanceTest;
import com.starlettech.annotations.SecurityTest;
import com.starlettech.annotations.TestCategory;
import com.starlettech.annotations.TestInfo;
import com.starlettech.config.ReportPortalConfig;
import com.starlettech.core.TestAnnotationProcessor;
import com.starlettech.core.handler.PerformanceTestHandler;
import com.starlettech.utils.ScreenshotUtils;

/**
 * ReportPortal Listener for enhanced reporting integration
 */
public class ReportPortalListener extends ReportPortalTestNGListener {
    private static final Logger logger = LogManager.getLogger(ReportPortalListener.class);
    private ReportPortalConfig rpConfig;
    private ScreenshotUtils screenshotUtils;

    public ReportPortalListener() {
        super();
        this.rpConfig = ReportPortalConfig.getInstance();
        this.screenshotUtils = new ScreenshotUtils();
        logger.info("ReportPortal Listener initialized");
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);

        if (rpConfig.isEnable()) {
            try {
                // Use centralized annotation processor
                TestAnnotationProcessor.TestExecutionContext context = TestAnnotationProcessor.processPreTestAnnotations(result);
                
                if (context != null) {
                    // Log basic test information
                    String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    ReportPortal.emitLog("Test started at: " + startTime, "INFO", Calendar.getInstance().getTime());
                    
                    // Log annotation information using context
                    logAnnotationInfo(context);
                }

                logger.debug("ReportPortal test start logged for: {}", result.getMethod().getMethodName());
            } catch (Exception e) {
                logger.error("Failed to log test start to ReportPortal: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (rpConfig.isEnable()) {
            try {
                String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                long duration = result.getEndMillis() - result.getStartMillis();

                ReportPortal.emitLog("✅ Test completed successfully", "INFO", Calendar.getInstance().getTime());
                ReportPortal.emitLog("Test ended at: " + endTime, "INFO", Calendar.getInstance().getTime());
                ReportPortal.emitLog("Test duration: " + duration + "ms", "INFO", Calendar.getInstance().getTime());

                // Handle Performance Test Results
                PerformanceTest perfTest = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(PerformanceTest.class);
                if (perfTest != null) {
                    PerformanceTestHandler.PerformanceResult perfResult = PerformanceTestHandler.stopPerformanceMonitoring(
                        result.getMethod().getConstructorOrMethod().getMethod());
                    
                    if (perfResult != null) {
                        ReportPortal.emitLog("🚀 Performance Test Results:", "INFO", Calendar.getInstance().getTime());
                        ReportPortal.emitLog("  - Requests: " + perfResult.getMetrics().getRequestCount(), "INFO", Calendar.getInstance().getTime());
                        ReportPortal.emitLog("  - Avg Response Time: " + String.format("%.2f", perfResult.getMetrics().getAverageResponseTime()) + "ms", "INFO", Calendar.getInstance().getTime());
                        ReportPortal.emitLog("  - Max Response Time: " + perfResult.getMetrics().getMaxResponseTime() + "ms", "INFO", Calendar.getInstance().getTime());
                        ReportPortal.emitLog("  - Throughput: " + String.format("%.2f", perfResult.getMetrics().getThroughput()) + " req/s", "INFO", Calendar.getInstance().getTime());
                        ReportPortal.emitLog("  - Max CPU Usage: " + String.format("%.2f", perfResult.getMetrics().getMaxCpuUsage()) + "%", "INFO", Calendar.getInstance().getTime());
                        ReportPortal.emitLog("  - Max Memory Usage: " + perfResult.getMetrics().getMaxMemoryUsage() + "MB", "INFO", Calendar.getInstance().getTime());
                        
                        if (!perfResult.isPassed()) {
                            ReportPortal.emitLog("⚠️ Performance violations detected:", "WARN", Calendar.getInstance().getTime());
                            for (String violation : perfResult.getViolations()) {
                                ReportPortal.emitLog("  - " + violation, "WARN", Calendar.getInstance().getTime());
                            }
                        } else {
                            ReportPortal.emitLog("✅ All performance requirements met", "INFO", Calendar.getInstance().getTime());
                        }
                    }
                }

                logger.debug("ReportPortal test success logged for: {}", result.getMethod().getMethodName());
            } catch (Exception e) {
                logger.error("Failed to log test success to ReportPortal: {}", e.getMessage());
            }
        }

        super.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (rpConfig.isEnable()) {
            try {
                String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                long duration = result.getEndMillis() - result.getStartMillis();

                // Log failure details
                ReportPortal.emitLog("❌ Test failed", "ERROR", Calendar.getInstance().getTime());
                ReportPortal.emitLog("Test ended at: " + endTime, "INFO", Calendar.getInstance().getTime());
                ReportPortal.emitLog("Test duration: " + duration + "ms", "INFO", Calendar.getInstance().getTime());

                // Log failure reason
                if (result.getThrowable() != null) {
                    ReportPortal.emitLog("Failure reason: " + result.getThrowable().getMessage(), "ERROR", Calendar.getInstance().getTime());
                    ReportPortal.emitLog("Stack trace: " + getStackTrace(result.getThrowable()), "ERROR", Calendar.getInstance().getTime());
                }

                // Attach screenshot if available
                attachScreenshotToReportPortal(result);

                logger.debug("ReportPortal test failure logged for: {}", result.getMethod().getMethodName());
            } catch (Exception e) {
                logger.error("Failed to log test failure to ReportPortal: {}", e.getMessage());
            }
        }

        super.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (rpConfig.isEnable()) {
            try {
                ReportPortal.emitLog("⏭️ Test skipped", "WARN", Calendar.getInstance().getTime());

                if (result.getThrowable() != null) {
                    ReportPortal.emitLog("Skip reason: " + result.getThrowable().getMessage(), "WARN", Calendar.getInstance().getTime());
                }

                logger.debug("ReportPortal test skip logged for: {}", result.getMethod().getMethodName());
            } catch (Exception e) {
                logger.error("Failed to log test skip to ReportPortal: {}", e.getMessage());
            }
        }

        super.onTestSkipped(result);
    }

    /**
     * Log annotation information using context
     */
    private void logAnnotationInfo(TestAnnotationProcessor.TestExecutionContext context) {
        // Add test information from annotation
        if (context.hasTestInfo()) {
            TestInfo testInfo = context.getTestInfo();
            ReportPortal.emitLog("Test Description: " + testInfo.description(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("Test Author: " + testInfo.author(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("Test Priority: " + testInfo.priority().name(), "INFO", Calendar.getInstance().getTime());

            if (!testInfo.jiraId().isEmpty()) {
                ReportPortal.emitLog("JIRA ID: " + testInfo.jiraId(), "INFO", Calendar.getInstance().getTime());
            }

            if (testInfo.tags().length > 0) {
                ReportPortal.emitLog("Tags: " + String.join(", ", testInfo.tags()), "INFO", Calendar.getInstance().getTime());
            }
        }

        // Add API test information from annotation
        if (context.hasApiTest()) {
            ApiTest apiTest = context.getApiTest();
            ReportPortal.emitLog("API Endpoint: " + apiTest.endpoint(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("HTTP Method: " + apiTest.method(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("Requires Auth: " + apiTest.requiresAuth(), "INFO", Calendar.getInstance().getTime());
        }

        // Add Performance test information from annotation
        if (context.hasPerformanceTest()) {
            PerformanceTest perfTest = context.getPerformanceTest();
            ReportPortal.emitLog("🚀 Performance Test Configuration:", "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Max Response Time: " + perfTest.maxResponseTime() + "ms", "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Concurrent Users: " + perfTest.concurrentUsers(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Test Duration: " + perfTest.duration() + "s", "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Performance Type: " + perfTest.type().name(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Max CPU Usage: " + perfTest.maxCpuUsage() + "%", "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Max Memory Usage: " + perfTest.maxMemoryUsage() + "MB", "INFO", Calendar.getInstance().getTime());
        }

        // Add Security test information from annotation
        if (context.hasSecurityTest()) {
            SecurityTest secTest = context.getSecurityTest();
            ReportPortal.emitLog("🔒 Security Test Configuration:", "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Security Types: " + java.util.Arrays.toString(secTest.types()), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Security Level: " + secTest.level().name(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Sensitive Data: " + secTest.sensitiveData(), "INFO", Calendar.getInstance().getTime());
            if (secTest.requiredRoles().length > 0) {
                ReportPortal.emitLog("  - Required Roles: " + java.util.Arrays.toString(secTest.requiredRoles()), "INFO", Calendar.getInstance().getTime());
            }
            if (secTest.owaspCategories().length > 0) {
                ReportPortal.emitLog("  - OWASP Categories: " + java.util.Arrays.toString(secTest.owaspCategories()), "INFO", Calendar.getInstance().getTime());
            }
            
            // Log security requirements
            if (context.getSecurityValidation() != null && context.getSecurityValidation().hasRequirements()) {
                ReportPortal.emitLog("🛡️ Security Requirements:", "INFO", Calendar.getInstance().getTime());
                for (String requirement : context.getSecurityValidation().getRequirements()) {
                    ReportPortal.emitLog("  - " + requirement, "INFO", Calendar.getInstance().getTime());
                }
            }
        }

        // Add Test Category information from annotation
        if (context.hasTestCategory()) {
            TestCategory testCategory = context.getTestCategory();
            ReportPortal.emitLog("📂 Test Category:", "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Category: " + testCategory.value().name(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Level: " + testCategory.level().name(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Risk Level: " + testCategory.riskLevel().name(), "INFO", Calendar.getInstance().getTime());
            ReportPortal.emitLog("  - Environments: " + java.util.Arrays.toString(testCategory.environments()), "INFO", Calendar.getInstance().getTime());
            if (testCategory.isFlaky()) {
                ReportPortal.emitLog("  - ⚠️ Marked as Flaky Test", "WARN", Calendar.getInstance().getTime());
            }
        }
    }

    /**
     * Attach screenshot to ReportPortal
     */
    private void attachScreenshotToReportPortal(ITestResult result) {
        try {
            String screenshotPath = screenshotUtils.takeScreenshot(result.getMethod().getMethodName() + "_failure");
            if (screenshotPath != null) {
                File screenshotFile = new File(screenshotPath);
                if (screenshotFile.exists()) {
                    ReportPortal.emitLog("Screenshot attached", "INFO", Calendar.getInstance().getTime(), screenshotFile);
                    logger.debug("Screenshot attached to ReportPortal: {}", screenshotPath);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to attach screenshot to ReportPortal: {}", e.getMessage());
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Log custom message to ReportPortal
     */
    public static void logInfo(String message) {
        try {
            ReportPortal.emitLog(message, "INFO", Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("Failed to log info message to ReportPortal: {}", e.getMessage());
        }
    }

    /**
     * Log warning message to ReportPortal
     */
    public static void logWarning(String message) {
        try {
            ReportPortal.emitLog(message, "WARN", Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("Failed to log warning message to ReportPortal: {}", e.getMessage());
        }
    }

    /**
     * Log error message to ReportPortal
     */
    public static void logError(String message) {
        try {
            ReportPortal.emitLog(message, "ERROR", Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("Failed to log error message to ReportPortal: {}", e.getMessage());
        }
    }

    /**
     * Attach file to ReportPortal
     */
    public static void attachFile(String message, File file) {
        try {
            ReportPortal.emitLog(message, "INFO", Calendar.getInstance().getTime(), file);
        } catch (Exception e) {
            logger.error("Failed to attach file to ReportPortal: {}", e.getMessage());
        }
    }

    /**
     * Check if the test result is from a configuration method (BeforeMethod, AfterMethod, etc.)
     */
    private boolean isConfigurationMethod(ITestResult result) {
        // Get method name and check if it's a configuration method
        String methodName = result.getMethod().getMethodName();
        
        // Check if method has TestNG configuration annotations
        java.lang.reflect.Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method != null) {
            boolean hasConfigAnnotation = method.isAnnotationPresent(org.testng.annotations.BeforeMethod.class) ||
                   method.isAnnotationPresent(org.testng.annotations.AfterMethod.class) ||
                   method.isAnnotationPresent(org.testng.annotations.BeforeClass.class) ||
                   method.isAnnotationPresent(org.testng.annotations.AfterClass.class) ||
                   method.isAnnotationPresent(org.testng.annotations.BeforeTest.class) ||
                   method.isAnnotationPresent(org.testng.annotations.AfterTest.class) ||
                   method.isAnnotationPresent(org.testng.annotations.BeforeSuite.class) ||
                   method.isAnnotationPresent(org.testng.annotations.AfterSuite.class) ||
                   method.isAnnotationPresent(org.testng.annotations.BeforeGroups.class) ||
                   method.isAnnotationPresent(org.testng.annotations.AfterGroups.class);
            
            if (hasConfigAnnotation) {
                logger.debug("Skipping configuration method: {} for ReportPortal", methodName);
                return true;
            }
        }
        
        // Additional check: TestNG method type
        if (result.getMethod().isBeforeMethodConfiguration() ||
            result.getMethod().isAfterMethodConfiguration() ||
            result.getMethod().isBeforeClassConfiguration() ||
            result.getMethod().isAfterClassConfiguration() ||
            result.getMethod().isBeforeTestConfiguration() ||
            result.getMethod().isAfterTestConfiguration() ||
            result.getMethod().isBeforeSuiteConfiguration() ||
            result.getMethod().isAfterSuiteConfiguration() ||
            result.getMethod().isBeforeGroupsConfiguration() ||
            result.getMethod().isAfterGroupsConfiguration()) {
            logger.debug("Skipping TestNG configuration method: {} for ReportPortal", methodName);
            return true;
        }
        
        // Fallback: check by method name patterns (common configuration method names)
        boolean isConfigByName = methodName.startsWith("before") || 
               methodName.startsWith("after") || 
               methodName.equals("setUp") || 
               methodName.equals("tearDown") ||
               methodName.startsWith("setup") ||
               methodName.startsWith("cleanup") ||
               methodName.equals("beforeClass") ||
               methodName.equals("afterClass") ||
               methodName.equals("beforeMethod") ||
               methodName.equals("afterMethod") ||
               methodName.equals("beforeSuite") ||
               methodName.equals("afterSuite");
               
        if (isConfigByName) {
            logger.debug("Skipping configuration method by name: {} for ReportPortal", methodName);
            return true;
        }
        
        return false;
    }

}

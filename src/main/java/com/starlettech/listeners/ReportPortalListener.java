package com.starlettech.listeners;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.tree.TestItemTree;
import com.epam.reportportal.testng.ReportPortalTestNGListener;
import com.starlettech.annotations.TestInfo;
import com.starlettech.config.ReportPortalConfig;
import com.starlettech.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

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
                // Add test information from annotation
                TestInfo testInfo = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TestInfo.class);
                if (testInfo != null) {
                    ReportPortal.emitLog("Test Description: " + testInfo.description(), "INFO", Calendar.getInstance().getTime());
                    ReportPortal.emitLog("Test Author: " + testInfo.author(), "INFO", Calendar.getInstance().getTime());
                    ReportPortal.emitLog("Test Priority: " + testInfo.priority(), "INFO", Calendar.getInstance().getTime());

                    if (!testInfo.jiraId().isEmpty()) {
                        ReportPortal.emitLog("JIRA ID: " + testInfo.jiraId(), "INFO", Calendar.getInstance().getTime());
                    }

                    if (testInfo.tags().length > 0) {
                        ReportPortal.emitLog("Tags: " + String.join(", ", testInfo.tags()), "INFO", Calendar.getInstance().getTime());
                    }
                }

                // Log test start time
                String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ReportPortal.emitLog("Test started at: " + startTime, "INFO", Calendar.getInstance().getTime());

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
}

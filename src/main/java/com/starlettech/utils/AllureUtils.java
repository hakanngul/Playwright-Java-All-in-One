package com.starlettech.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import com.starlettech.annotations.PerformanceTest;
import com.starlettech.annotations.SecurityTest;
import com.starlettech.annotations.TestCategory;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.TestAnnotationProcessor.TestExecutionContext;
import com.starlettech.enums.TestPriority;

import io.qameta.allure.Allure;
import io.qameta.allure.SeverityLevel;

/**
 * Utility class for Allure TestNG integration and metadata enrichment
 */
public class AllureUtils {
    private static final Logger logger = LogManager.getLogger(AllureUtils.class);
    
    /**
     * Enrich test with metadata from custom annotations
     */
    public void enrichTestWithMetadata(TestExecutionContext context) {
        try {
            // Add test information from @TestInfo
            if (context.hasTestInfo()) {
                processTestInfoAnnotation(context.getTestInfo());
            }
            
            // Add performance test metadata
            if (context.hasPerformanceTest()) {
                processPerformanceTestAnnotation(context.getPerformanceTest());
            }
            
            // Add security test metadata
            if (context.hasSecurityTest()) {
                processSecurityTestAnnotation(context.getSecurityTest());
            }
            
            // Add test category metadata
            if (context.hasTestCategory()) {
                processTestCategoryAnnotation(context.getTestCategory());
            }
            
        } catch (Exception e) {
            logger.error("Failed to enrich test with metadata: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process @TestInfo annotation for Allure
     */
    private void processTestInfoAnnotation(TestInfo info) {
        // Set Allure hierarchy
        if (!info.epic().isEmpty()) {
            Allure.epic(info.epic());
        }
        if (!info.feature().isEmpty()) {
            Allure.feature(info.feature());
        }
        if (!info.story().isEmpty()) {
            Allure.story(info.story());
        }
        
        // Set description
        if (!info.description().isEmpty()) {
            Allure.description(info.description());
        }
        
        // Set severity based on priority
        Allure.label("severity", this.mapPriorityToSeverityString(info.priority()));
        
        // Add JIRA link if present
        if (!info.jiraId().isEmpty()) {
            String jiraUrl = getJiraUrl(info.jiraId());
            Allure.link("JIRA", info.jiraId(), jiraUrl);
        }
        
        // Add tags as labels
        for (String tag : info.tags()) {
            Allure.label("tag", tag);
        }
        
        // Add additional metadata
        if (!info.author().isEmpty()) {
            Allure.label("author", info.author());
        }
        
        if (!info.estimatedDuration().isEmpty()) {
            Allure.parameter("Estimated Duration", info.estimatedDuration());
        }
        
        if (info.isBlocking()) {
            Allure.label("blocking", "true");
        }
        
        // Add requirements
        for (String requirement : info.requirements()) {
            Allure.label("requirement", requirement);
        }
    }
    
    /**
     * Process @PerformanceTest annotation for Allure
     */
    private void processPerformanceTestAnnotation(PerformanceTest perfTest) {
        Allure.label("test.type", "performance");
        Allure.label("performance.type", perfTest.type().name());
        
        Allure.parameter("Max Response Time", perfTest.maxResponseTime() + "ms");
        Allure.parameter("Concurrent Users", String.valueOf(perfTest.concurrentUsers()));
        Allure.parameter("Duration", perfTest.duration() + "s");
        Allure.parameter("Max CPU Usage", perfTest.maxCpuUsage() + "%");
        Allure.parameter("Max Memory Usage", perfTest.maxMemoryUsage() + "%");
        Allure.parameter("Expected Throughput", String.valueOf(perfTest.expectedThroughput()));
    }
    
    /**
     * Process @SecurityTest annotation for Allure
     */
    private void processSecurityTestAnnotation(SecurityTest secTest) {
        Allure.label("test.type", "security");
        Allure.label("security.level", secTest.level().name());
        
        // Add security types as labels
        for (var type : secTest.types()) {
            Allure.label("security.type", type.name());
        }
        
        // Add OWASP categories
        for (var category : secTest.owaspCategories()) {
            Allure.label("owasp.category", category.name());
        }
        
        // Add required roles
        for (String role : secTest.requiredRoles()) {
            Allure.parameter("Required Role", role);
        }
        
        if (secTest.sensitiveData()) {
            Allure.label("sensitive.data", "true");
        }
    }
    
    /**
     * Process @TestCategory annotation for Allure
     */
    private void processTestCategoryAnnotation(TestCategory category) {
        Allure.label("test.category", category.value().name());
        Allure.label("test.level", category.level().name());
        Allure.label("risk.level", category.riskLevel().name());
        
        // Add environments
        for (String env : category.environments()) {
            Allure.label("environment", env);
        }
        
        if (category.isFlaky()) {
            Allure.label("flaky", "true");
        }
    }
    
    /**
     * Attach screenshot on test failure
     */
    public void attachScreenshotOnFailure(ITestResult result) {
        try {
            ScreenshotUtils screenshotUtils = new ScreenshotUtils();
            String screenshotPath = screenshotUtils.takeScreenshot(
                result.getMethod().getMethodName() + "_failure");
            
            if (screenshotPath != null && Files.exists(Paths.get(screenshotPath))) {
                byte[] screenshot = Files.readAllBytes(Paths.get(screenshotPath));
                Allure.addAttachment("Screenshot on Failure", "image/png", 
                    new ByteArrayInputStream(screenshot), ".png");
                
                logger.info("Screenshot attached to Allure report: {}", screenshotPath);
            }
        } catch (Exception e) {
            logger.error("Failed to attach screenshot to Allure: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Attach text content to Allure report
     */
    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }
    
    /**
     * Attach JSON content to Allure report
     */
    public static void attachJson(String name, String jsonContent) {
        Allure.addAttachment(name, "application/json", jsonContent, ".json");
    }
    
    /**
     * Attach file to Allure report
     */
    public static void attachFile(String name, String filePath) {
        try {
            if (Files.exists(Paths.get(filePath))) {
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
                String mimeType = Files.probeContentType(Paths.get(filePath));
                String extension = filePath.substring(filePath.lastIndexOf('.'));
                
                Allure.addAttachment(name, mimeType, 
                    new ByteArrayInputStream(fileContent), extension);
            }
        } catch (IOException e) {
            logger.error("Failed to attach file to Allure: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Map TestPriority to Allure severity string
     */
    private String mapPriorityToSeverityString(TestPriority priority) {
        return switch (priority) {
            case CRITICAL -> "blocker";
            case HIGH -> "critical";
            case MEDIUM -> "normal";
            case LOW -> "minor";
            default -> "normal";
        };
    }
    
    /**
     * Map TestPriority to Allure SeverityLevel
     */
    private SeverityLevel mapPriorityToSeverity(TestPriority priority) {
        return switch (priority) {
            case CRITICAL -> SeverityLevel.BLOCKER;
            case HIGH -> SeverityLevel.CRITICAL;
            case MEDIUM -> SeverityLevel.NORMAL;
            case LOW -> SeverityLevel.MINOR;
            default -> SeverityLevel.NORMAL;
        };
    }
    
    /**
     * Get JIRA URL from ticket ID
     */
    private String getJiraUrl(String jiraId) {
        String jiraBaseUrl = System.getProperty("jira.base.url", "https://your-jira-instance.com/browse/");
        return jiraBaseUrl + jiraId;
    }
    
    /**
     * Write environment properties for Allure report
     */
    public static void writeEnvironmentProperties() {
        try {
            Properties props = new Properties();
            props.setProperty("Browser", System.getProperty("browser.name", "chrome"));
            props.setProperty("Base URL", System.getProperty("base.url", "http://localhost:8080"));
            props.setProperty("Test Environment", System.getProperty("test.environment", "local"));
            props.setProperty("Java Version", System.getProperty("java.version"));
            props.setProperty("Framework Version", "1.0-SNAPSHOT");
            props.setProperty("Playwright Version", "1.54.0");
            props.setProperty("TestNG Version", "7.11.0");
            props.setProperty("Allure Version", "2.29.0");
            
            File resultsDir = new File("target/allure-results");
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }
            
            try (FileWriter writer = new FileWriter(new File(resultsDir, "environment.properties"))) {
                props.store(writer, "Test Environment Information for Allure Report");
            }
            
            logger.info("Environment properties written for Allure report");
            
        } catch (IOException e) {
            logger.error("Failed to write environment properties: {}", e.getMessage(), e);
        }
    }
}
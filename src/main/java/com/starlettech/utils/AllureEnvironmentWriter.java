package com.starlettech.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for generating environment properties for Allure reports
 */
public class AllureEnvironmentWriter {
    private static final Logger logger = LogManager.getLogger(AllureEnvironmentWriter.class);
    
    /**
     * Write environment properties file for Allure report
     * This method gathers system properties and framework information
     * to create a comprehensive environment information file
     */
    public static void writeEnvironmentProperties() {
        try {
            Properties props = new Properties();
            
            // Browser and application information
            props.setProperty("Browser", System.getProperty("browser.name", "chrome"));
            props.setProperty("Browser Version", System.getProperty("browser.version", "latest"));
            props.setProperty("Base URL", System.getProperty("base.url", "http://localhost:8080"));
            props.setProperty("Test Environment", System.getProperty("test.environment", "local"));
            
            // System information
            props.setProperty("Java Version", System.getProperty("java.version"));
            props.setProperty("Java Vendor", System.getProperty("java.vendor"));
            props.setProperty("Operating System", System.getProperty("os.name"));
            props.setProperty("OS Version", System.getProperty("os.version"));
            props.setProperty("OS Architecture", System.getProperty("os.arch"));
            
            // Framework versions
            props.setProperty("Framework Version", "1.0-SNAPSHOT");
            props.setProperty("Playwright Version", "1.54.0");
            props.setProperty("TestNG Version", "7.11.0");
            props.setProperty("Allure Version", "2.29.0");
            props.setProperty("Log4j Version", "2.25.1");
            
            // Test execution information
            props.setProperty("Test Execution Time", new java.util.Date().toString());
            props.setProperty("User", System.getProperty("user.name"));
            props.setProperty("Maven Profile", System.getProperty("maven.profile", "default"));
            
            // CI/CD information (if available)
            String jenkinsUrl = System.getProperty("JENKINS_URL");
            String buildNumber = System.getProperty("BUILD_NUMBER");
            String gitCommit = System.getProperty("GIT_COMMIT");
            
            if (jenkinsUrl != null) {
                props.setProperty("Jenkins URL", jenkinsUrl);
            }
            if (buildNumber != null) {
                props.setProperty("Build Number", buildNumber);
            }
            if (gitCommit != null) {
                props.setProperty("Git Commit", gitCommit);
            }
            
            // ReportPortal integration information
            String rpEndpoint = System.getProperty("rp.endpoint");
            String rpProject = System.getProperty("rp.project");
            if (rpEndpoint != null) {
                props.setProperty("ReportPortal Endpoint", rpEndpoint);
            }
            if (rpProject != null) {
                props.setProperty("ReportPortal Project", rpProject);
            }
            
            // Ensure results directory exists
            File resultsDir = new File("target/allure-results");
            if (!resultsDir.exists()) {
                boolean created = resultsDir.mkdirs();
                if (!created) {
                    logger.warn("Failed to create allure-results directory");
                }
            }
            
            // Write properties file
            File envFile = new File(resultsDir, "environment.properties");
            try (FileWriter writer = new FileWriter(envFile)) {
                props.store(writer, "Test Environment Information for Allure Report - Generated on " + new java.util.Date());
            }
            
            logger.info("Environment properties written to: {}", envFile.getAbsolutePath());
            logger.debug("Environment properties: {}", props.toString());
            
        } catch (IOException e) {
            logger.error("Failed to write environment properties: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Write environment properties with custom properties
     * @param customProperties Additional properties to include
     */
    public static void writeEnvironmentProperties(Properties customProperties) {
        try {
            // First write standard properties
            writeEnvironmentProperties();
            
            // Then append custom properties
            if (customProperties != null && !customProperties.isEmpty()) {
                File resultsDir = new File("target/allure-results");
                File envFile = new File(resultsDir, "environment.properties");
                
                // Read existing properties
                Properties existingProps = new Properties();
                if (envFile.exists()) {
                    try (java.io.FileReader reader = new java.io.FileReader(envFile)) {
                        existingProps.load(reader);
                    }
                }
                
                // Add custom properties
                existingProps.putAll(customProperties);
                
                // Write updated properties
                try (FileWriter writer = new FileWriter(envFile)) {
                    existingProps.store(writer, "Test Environment Information for Allure Report - Updated on " + new java.util.Date());
                }
                
                logger.info("Custom environment properties added: {}", customProperties.keySet());
            }
            
        } catch (IOException e) {
            logger.error("Failed to write custom environment properties: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Add a single environment property
     * @param key Property key
     * @param value Property value
     */
    public static void addEnvironmentProperty(String key, String value) {
        Properties customProps = new Properties();
        customProps.setProperty(key, value);
        writeEnvironmentProperties(customProps);
    }
}
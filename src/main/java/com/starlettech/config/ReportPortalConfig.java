package com.starlettech.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.starlettech.exceptions.ConfigurationException;

/**
 * ReportPortal Configuration class for managing ReportPortal settings
 */
public class ReportPortalConfig {
    private static final Logger logger = LogManager.getLogger(ReportPortalConfig.class);
    private static ReportPortalConfig instance;
    private Properties properties;

    private ReportPortalConfig() {
        loadProperties();
    }

    public static ReportPortalConfig getInstance() {
        if (instance == null) {
            synchronized (ReportPortalConfig.class) {
                if (instance == null) {
                    instance = new ReportPortalConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("reportportal.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("ReportPortal configuration loaded successfully");
            } else {
                logger.warn("reportportal.properties file not found, using default values");
            }
        } catch (IOException e) {
            logger.error("Error loading ReportPortal configuration: {}", e.getMessage());
            throw ConfigurationException.loadError("reportportal.properties", e);
        }
    }

    public String getEndpoint() {
        return getProperty("rp.endpoint", "http://localhost:8080");
    }

    public String getUuid() {
        return getProperty("rp.uuid", "");
    }

    public String getProject() {
        return getProperty("rp.project", "default_personal");
    }

    public String getLaunch() {
        return getProperty("rp.launch", "Playwright Tests");
    }

    public String getMode() {
        return getProperty("rp.mode", "DEFAULT");
    }

    public boolean isEnable() {
        return Boolean.parseBoolean(getProperty("rp.enable", "false"));
    }

    public String getDescription() {
        return getProperty("rp.description", "Automated tests execution");
    }

    public String getTags() {
        return getProperty("rp.tags", "");
    }

    public boolean isSkippedIssue() {
        return Boolean.parseBoolean(getProperty("rp.skipped.issue", "true"));
    }

    public int getBatchLogsSize() {
        return Integer.parseInt(getProperty("rp.batch.size.logs", "20"));
    }

    public int getReportingTimeout() {
        return Integer.parseInt(getProperty("rp.reporting.timeout", "5"));
    }

    private String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }
}

package com.starlettech.config;

import com.starlettech.enums.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * API Configuration class for managing API endpoints and settings
 */
public class ApiConfig {
    private static final Logger logger = LogManager.getLogger(ApiConfig.class);
    private static ApiConfig instance;
    private Properties properties;
    private Environment environment;

    private ApiConfig() {
        loadProperties();
        this.environment = Environment.valueOf(
            System.getProperty("environment", getProperty("default.environment", "DEV")).toUpperCase()
        );
    }

    public static ApiConfig getInstance() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("default-config.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("API configuration loaded successfully");
            } else {
                logger.warn("default-config.properties file not found, using default values");
            }
        } catch (IOException e) {
            logger.error("Error loading API configuration: {}", e.getMessage());
        }
    }

    public String getBaseUrl() {
        return getProperty("api.base.url." + environment.name().toLowerCase(), "http://localhost:8080");
    }

    public String getAuthEndpoint() {
        return getProperty("api.auth.endpoint", "/api/auth");
    }

    public String getUserEndpoint() {
        return getProperty("api.user.endpoint", "/api/users");
    }

    public int getTimeout() {
        return Integer.parseInt(getProperty("api.timeout", "30000"));
    }

    public int getRetryCount() {
        return Integer.parseInt(getProperty("api.retry.count", "3"));
    }

    public String getContentType() {
        return getProperty("api.content.type", "application/json");
    }

    public boolean isLoggingEnabled() {
        return Boolean.parseBoolean(getProperty("api.logging.enabled", "true"));
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }
}

package com.starlettech.config;

import com.starlettech.enums.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test Configuration class for managing general test settings
 */
public class TestConfig {
    private static final Logger logger = LogManager.getLogger(TestConfig.class);
    private static TestConfig instance;
    private Properties properties;
    private Environment environment;

    private TestConfig() {
        loadProperties();
        this.environment = Environment.valueOf(
            System.getProperty("environment", getProperty("default.environment", "DEV")).toUpperCase()
        );
    }

    public static TestConfig getInstance() {
        if (instance == null) {
            synchronized (TestConfig.class) {
                if (instance == null) {
                    instance = new TestConfig();
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
                logger.info("Test configuration loaded successfully");
            } else {
                logger.warn("default-config.properties file not found, using default values");
            }
        } catch (IOException e) {
            logger.error("Error loading test configuration: {}", e.getMessage());
        }
    }

    public String getBaseUrl() {
        return getProperty("base.url." + environment.name().toLowerCase(), "http://localhost:3000");
    }

    public int getImplicitWait() {
        return Integer.parseInt(getProperty("wait.implicit", "10"));
    }

    public int getExplicitWait() {
        return Integer.parseInt(getProperty("wait.explicit", "30"));
    }

    public int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("wait.pageload", "60"));
    }

    public boolean isScreenshotOnFailure() {
        return Boolean.parseBoolean(getProperty("screenshot.on.failure", "true"));
    }

    public String getScreenshotPath() {
        return getProperty("screenshot.path", System.getProperty("user.dir") + "/screenshots");
    }

    public boolean isRetryEnabled() {
        return Boolean.parseBoolean(getProperty("retry.enabled", "true"));
    }

    public int getRetryCount() {
        return Integer.parseInt(getProperty("retry.count", "2"));
    }

    public String getTestDataPath() {
        return getProperty("testdata.path", "src/test/resources/testdata");
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public boolean isParallelExecution() {
        return Boolean.parseBoolean(getProperty("parallel.execution", "false"));
    }

    public int getThreadCount() {
        return Integer.parseInt(getProperty("thread.count", "1"));
    }

    private String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }
}

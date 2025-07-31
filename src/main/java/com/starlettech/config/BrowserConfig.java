package com.starlettech.config;

import com.starlettech.enums.BrowserType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Browser Configuration class for managing browser settings
 */
public class BrowserConfig {
    private static final Logger logger = LogManager.getLogger(BrowserConfig.class);
    private static BrowserConfig instance;
    private Properties properties;

    private BrowserConfig() {
        loadProperties();
    }

    public static BrowserConfig getInstance() {
        if (instance == null) {
            synchronized (BrowserConfig.class) {
                if (instance == null) {
                    instance = new BrowserConfig();
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
                logger.info("Browser configuration loaded successfully");
            } else {
                logger.warn("default-config.properties file not found, using default values");
            }
        } catch (IOException e) {
            logger.error("Error loading browser configuration: {}", e.getMessage());
        }
    }

    public BrowserType getBrowserType() {
        String browser = getProperty("browser.type", "CHROMIUM");
        return BrowserType.fromString(browser);
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("browser.headless", "true"));
    }

    public int getTimeout() {
        return Integer.parseInt(getProperty("browser.timeout", "30000"));
    }

    public String getViewportSize() {
        return getProperty("browser.viewport", "1920x1080");
    }

    public List<String> getBrowserArgs() {
        String args = getProperty("browser.args", "");
        return args.isEmpty() ? List.of() : Arrays.asList(args.split(","));
    }

    public boolean isSlowMo() {
        return Boolean.parseBoolean(getProperty("browser.slowmo.enabled", "false"));
    }

    public int getSlowMoDelay() {
        return Integer.parseInt(getProperty("browser.slowmo.delay", "100"));
    }

    public boolean isDevtools() {
        return Boolean.parseBoolean(getProperty("browser.devtools", "false"));
    }

    public String getDownloadPath() {
        return getProperty("browser.download.path", System.getProperty("user.dir") + "/downloads");
    }

    public boolean isVideoRecording() {
        return Boolean.parseBoolean(getProperty("browser.video.enabled", "false"));
    }

    public String getVideoPath() {
        return getProperty("browser.video.path", System.getProperty("user.dir") + "/videos");
    }

    public boolean isTracing() {
        return Boolean.parseBoolean(getProperty("browser.tracing.enabled", "false"));
    }

    public String getTracePath() {
        return getProperty("browser.trace.path", System.getProperty("user.dir") + "/traces");
    }

    private String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }
}

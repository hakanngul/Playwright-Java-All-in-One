package com.starlettech.core.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.starlettech.enums.BrowserType;
import com.starlettech.enums.Environment;

/**
 * Dynamic Configuration Manager for runtime configuration changes
 */
public class DynamicConfigManager {
    private static final Logger logger = LogManager.getLogger(DynamicConfigManager.class);
    
    // Thread-local configuration overrides
    private static final ThreadLocal<Map<String, Object>> configOverrides = new ThreadLocal<>();
    
    // Global configuration overrides
    private static final Map<String, Object> globalOverrides = new ConcurrentHashMap<>();
    
    // Configuration change listeners
    private static final Map<String, ConfigChangeListener> listeners = new ConcurrentHashMap<>();

    /**
     * Interface for configuration change listeners
     */
    public interface ConfigChangeListener {
        void onConfigChanged(String key, Object oldValue, Object newValue);
    }

    // ========== Thread-Local Configuration Overrides ==========

    /**
     * Set configuration override for current thread
     */
    public static void setThreadLocalConfig(String key, Object value) {
        Map<String, Object> overrides = configOverrides.get();
        if (overrides == null) {
            overrides = new HashMap<>();
            configOverrides.set(overrides);
        }
        
        Object oldValue = overrides.put(key, value);
        logger.debug("Thread-local config override: {} = {} (was: {})", key, value, oldValue);
        
        // Notify listeners
        notifyListeners(key, oldValue, value);
    }

    /**
     * Get configuration value with thread-local override
     */
    @SuppressWarnings("unchecked")
    public static <T> T getThreadLocalConfig(String key, T defaultValue) {
        Map<String, Object> overrides = configOverrides.get();
        if (overrides != null && overrides.containsKey(key)) {
            return (T) overrides.get(key);
        }
        return defaultValue;
    }

    /**
     * Remove thread-local configuration override
     */
    public static void removeThreadLocalConfig(String key) {
        Map<String, Object> overrides = configOverrides.get();
        if (overrides != null) {
            Object removedValue = overrides.remove(key);
            logger.debug("Removed thread-local config override: {} (was: {})", key, removedValue);
        }
    }

    /**
     * Clear all thread-local configuration overrides
     */
    public static void clearThreadLocalConfig() {
        Map<String, Object> overrides = configOverrides.get();
        if (overrides != null) {
            int count = overrides.size();
            overrides.clear();
            logger.debug("Cleared {} thread-local config overrides", count);
        }
    }

    // ========== Global Configuration Overrides ==========

    /**
     * Set global configuration override
     */
    public static void setGlobalConfig(String key, Object value) {
        Object oldValue = globalOverrides.put(key, value);
        logger.info("Global config override: {} = {} (was: {})", key, value, oldValue);
        
        // Notify listeners
        notifyListeners(key, oldValue, value);
    }

    /**
     * Get configuration value with global override
     */
    @SuppressWarnings("unchecked")
    public static <T> T getGlobalConfig(String key, T defaultValue) {
        if (globalOverrides.containsKey(key)) {
            return (T) globalOverrides.get(key);
        }
        return defaultValue;
    }

    /**
     * Remove global configuration override
     */
    public static void removeGlobalConfig(String key) {
        Object removedValue = globalOverrides.remove(key);
        logger.info("Removed global config override: {} (was: {})", key, removedValue);
    }

    /**
     * Clear all global configuration overrides
     */
    public static void clearGlobalConfig() {
        int count = globalOverrides.size();
        globalOverrides.clear();
        logger.info("Cleared {} global config overrides", count);
    }

    // ========== Configuration Resolution ==========

    /**
     * Get configuration value with priority: Thread-local > Global > System Property > Default
     */
    @SuppressWarnings("unchecked")
    public static <T> T getConfig(String key, T defaultValue) {
        // 1. Check thread-local override
        Map<String, Object> threadOverrides = configOverrides.get();
        if (threadOverrides != null && threadOverrides.containsKey(key)) {
            return (T) threadOverrides.get(key);
        }
        
        // 2. Check global override
        if (globalOverrides.containsKey(key)) {
            return (T) globalOverrides.get(key);
        }
        
        // 3. Check system property
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return convertValue(systemValue, defaultValue);
        }
        
        // 4. Return default value
        return defaultValue;
    }

    // ========== Browser Configuration Overrides ==========

    /**
     * Override browser type for current thread
     */
    public static void setBrowserType(BrowserType browserType) {
        setThreadLocalConfig("browser.type", browserType);
        logger.info("Browser type overridden to: {}", browserType);
    }

    /**
     * Override headless mode for current thread
     */
    public static void setHeadless(boolean headless) {
        setThreadLocalConfig("browser.headless", headless);
        logger.info("Headless mode overridden to: {}", headless);
    }

    /**
     * Override viewport size for current thread
     */
    public static void setViewportSize(String viewportSize) {
        setThreadLocalConfig("browser.viewport", viewportSize);
        logger.info("Viewport size overridden to: {}", viewportSize);
    }

    // ========== Environment Configuration Overrides ==========

    /**
     * Override environment for current thread
     */
    public static void setEnvironment(Environment environment) {
        setThreadLocalConfig("environment", environment);
        logger.info("Environment overridden to: {}", environment);
    }

    /**
     * Override base URL for current thread
     */
    public static void setBaseUrl(String baseUrl) {
        setThreadLocalConfig("base.url", baseUrl);
        logger.info("Base URL overridden to: {}", baseUrl);
    }

    /**
     * Override API base URL for current thread
     */
    public static void setApiBaseUrl(String apiBaseUrl) {
        setThreadLocalConfig("api.base.url", apiBaseUrl);
        logger.info("API base URL overridden to: {}", apiBaseUrl);
    }

    // ========== Test Configuration Overrides ==========

    /**
     * Override timeout values for current thread
     */
    public static void setTimeout(int implicitWait, int explicitWait, int pageLoadTimeout) {
        setThreadLocalConfig("wait.implicit", implicitWait);
        setThreadLocalConfig("wait.explicit", explicitWait);
        setThreadLocalConfig("wait.pageload", pageLoadTimeout);
        logger.info("Timeout values overridden - Implicit: {}s, Explicit: {}s, PageLoad: {}s", 
            implicitWait, explicitWait, pageLoadTimeout);
    }

    /**
     * Override retry configuration for current thread
     */
    public static void setRetryConfig(boolean enabled, int count) {
        setThreadLocalConfig("retry.enabled", enabled);
        setThreadLocalConfig("retry.count", count);
        logger.info("Retry configuration overridden - Enabled: {}, Count: {}", enabled, count);
    }

    // ========== Configuration Listeners ==========

    /**
     * Register configuration change listener
     */
    public static void addConfigChangeListener(String key, ConfigChangeListener listener) {
        listeners.put(key, listener);
        logger.debug("Registered config change listener for key: {}", key);
    }

    /**
     * Remove configuration change listener
     */
    public static void removeConfigChangeListener(String key) {
        ConfigChangeListener removed = listeners.remove(key);
        if (removed != null) {
            logger.debug("Removed config change listener for key: {}", key);
        }
    }

    /**
     * Notify configuration change listeners
     */
    private static void notifyListeners(String key, Object oldValue, Object newValue) {
        ConfigChangeListener listener = listeners.get(key);
        if (listener != null) {
            try {
                listener.onConfigChanged(key, oldValue, newValue);
            } catch (Exception e) {
                logger.error("Error notifying config change listener for key {}: {}", key, e.getMessage());
            }
        }
    }

    // ========== Utility Methods ==========

    /**
     * Convert string value to appropriate type based on default value
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertValue(String stringValue, T defaultValue) {
        if (defaultValue == null) {
            return (T) stringValue;
        }
        
        Class<?> targetType = defaultValue.getClass();
        
        try {
            if (targetType == Boolean.class || targetType == boolean.class) {
                return (T) Boolean.valueOf(stringValue);
            } else if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(stringValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(stringValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(stringValue);
            } else if (targetType == BrowserType.class) {
                return (T) BrowserType.fromString(stringValue);
            } else if (targetType == Environment.class) {
                return (T) Environment.fromString(stringValue);
            } else {
                return (T) stringValue;
            }
        } catch (NumberFormatException e) {
            logger.warn("Failed to convert '{}' to type {}, using default value", stringValue, targetType.getSimpleName());
            return defaultValue;
        }
    }

    /**
     * Get all current configuration overrides
     */
    public static Map<String, Object> getAllOverrides() {
        Map<String, Object> allOverrides = new HashMap<>(globalOverrides);
        
        Map<String, Object> threadOverrides = configOverrides.get();
        if (threadOverrides != null) {
            allOverrides.putAll(threadOverrides);
        }
        
        return allOverrides;
    }

    /**
     * Cleanup thread-local resources
     */
    public static void cleanup() {
        configOverrides.remove();
    }
}

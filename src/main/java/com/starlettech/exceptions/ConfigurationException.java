package com.starlettech.exceptions;

/**
 * Exception thrown for configuration-related errors
 */
public class ConfigurationException extends FrameworkException {
    
    private final String configKey;
    
    public ConfigurationException(String message) {
        super(message, ErrorType.CONFIG, "FW_CONFIG_001");
        this.configKey = null;
    }
    
    public ConfigurationException(String message, String configKey) {
        super(message, ErrorType.CONFIG, "FW_CONFIG_002");
        this.configKey = configKey;
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause, ErrorType.CONFIG, "FW_CONFIG_001");
        this.configKey = null;
    }
    
    public ConfigurationException(String message, Throwable cause, String configKey) {
        super(message, cause, ErrorType.CONFIG, "FW_CONFIG_002");
        this.configKey = configKey;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    // Specific configuration error methods
    public static ConfigurationException fileNotFound(String fileName) {
        return new ConfigurationException("Configuration file not found: " + fileName, "FW_CONFIG_001");
    }
    
    public static ConfigurationException invalidProperty(String propertyKey, String expectedType) {
        return new ConfigurationException(
            String.format("Invalid property '%s'. Expected type: %s", propertyKey, expectedType), 
            propertyKey);
    }
    
    public static ConfigurationException missingRequiredProperty(String propertyKey) {
        return new ConfigurationException("Required property is missing: " + propertyKey, propertyKey);
    }
    
    public static ConfigurationException invalidEnvironment(String environment) {
        return new ConfigurationException("Invalid environment: " + environment, "FW_CONFIG_003");
    }
    
    public static ConfigurationException loadError(String fileName, Throwable cause) {
        return new ConfigurationException("Failed to load configuration from: " + fileName, cause);
    }
    
    @Override
    public String toString() {
        String baseString = super.toString();
        if (configKey != null) {
            baseString += String.format(" [Config Key: %s]", configKey);
        }
        return baseString;
    }
}
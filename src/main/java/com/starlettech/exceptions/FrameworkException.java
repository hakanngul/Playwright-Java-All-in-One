package com.starlettech.exceptions;

/**
 * Base exception class for the Playwright test automation framework
 */
public class FrameworkException extends RuntimeException {
    
    private final ErrorType errorType;
    private final String errorCode;
    
    /**
     * Error types for categorizing framework exceptions
     */
    public enum ErrorType {
        CONFIG("Configuration Error"),
        BROWSER("Browser Error"),
        API("API Error"),
        VALIDATION("Validation Error"),
        PAGE("Page Error"),
        ELEMENT("Element Error"),
        AUTHENTICATION("Authentication Error"),
        DATA("Data Error"),
        TIMEOUT("Timeout Error"),
        NETWORK("Network Error");
        
        private final String description;
        
        ErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public FrameworkException(String message) {
        super(message);
        this.errorType = ErrorType.VALIDATION;
        this.errorCode = "FW_" + errorType.name() + "_001";
    }
    
    public FrameworkException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
        this.errorCode = "FW_" + errorType.name() + "_001";
    }
    
    public FrameworkException(String message, ErrorType errorType, String errorCode) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
    
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.VALIDATION;
        this.errorCode = "FW_" + errorType.name() + "_001";
    }
    
    public FrameworkException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = "FW_" + errorType.name() + "_001";
    }
    
    public FrameworkException(String message, Throwable cause, ErrorType errorType, String errorCode) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - %s: %s", 
            getClass().getSimpleName(), 
            errorCode, 
            errorType.getDescription(), 
            getMessage());
    }
}
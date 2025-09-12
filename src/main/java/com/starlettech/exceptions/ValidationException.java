package com.starlettech.exceptions;

/**
 * Exception thrown for validation-related errors
 */
public class ValidationException extends FrameworkException {
    
    private final String expectedValue;
    private final String actualValue;
    
    public ValidationException(String message) {
        super(message, ErrorType.VALIDATION, "FW_VALIDATION_001");
        this.expectedValue = null;
        this.actualValue = null;
    }
    
    public ValidationException(String message, String expectedValue, String actualValue) {
        super(message, ErrorType.VALIDATION, "FW_VALIDATION_002");
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }
    
    public ValidationException(String message, String errorCode) {
        super(message, ErrorType.VALIDATION, errorCode);
        this.expectedValue = null;
        this.actualValue = null;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause, ErrorType.VALIDATION, "FW_VALIDATION_001");
        this.expectedValue = null;
        this.actualValue = null;
    }
    
    public String getExpectedValue() {
        return expectedValue;
    }
    
    public String getActualValue() {
        return actualValue;
    }
    
    // Specific validation error methods
    public static ValidationException valuesMismatch(String field, String expected, String actual) {
        return new ValidationException(
            String.format("Values mismatch for field '%s'. Expected: '%s', Actual: '%s'", field, expected, actual),
            expected, actual);
    }
    
    public static ValidationException nullValue(String fieldName) {
        return new ValidationException("Field '" + fieldName + "' cannot be null", "FW_VALIDATION_003");
    }
    
    public static ValidationException emptyValue(String fieldName) {
        return new ValidationException("Field '" + fieldName + "' cannot be empty", "FW_VALIDATION_004");
    }
    
    public static ValidationException invalidFormat(String fieldName, String expectedFormat) {
        return new ValidationException(
            String.format("Invalid format for field '%s'. Expected format: %s", fieldName, expectedFormat),
            "FW_VALIDATION_005");
    }
    
    public static ValidationException outOfRange(String fieldName, String min, String max, String actual) {
        return new ValidationException(
            String.format("Value '%s' for field '%s' is out of range [%s, %s]", actual, fieldName, min, max),
            String.format("[%s, %s]", min, max), actual);
    }
    
    public static ValidationException schemaValidationFailed(String[] missingFields) {
        return new ValidationException(
            "Schema validation failed. Missing fields: " + String.join(", ", missingFields),
            "FW_VALIDATION_006");
    }
    
    @Override
    public String toString() {
        String baseString = super.toString();
        if (expectedValue != null && actualValue != null) {
            baseString += String.format(" [Expected: %s, Actual: %s]", expectedValue, actualValue);
        }
        return baseString;
    }
}
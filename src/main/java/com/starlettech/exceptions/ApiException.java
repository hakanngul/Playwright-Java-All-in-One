package com.starlettech.exceptions;

/**
 * Exception thrown for API-related errors
 */
public class ApiException extends FrameworkException {
    
    private final int statusCode;
    private final String responseBody;
    
    public ApiException(String message) {
        super(message, ErrorType.API, "FW_API_001");
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public ApiException(String message, Throwable cause) {
        super(message, cause, ErrorType.API, "FW_API_001");
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public ApiException(String message, int statusCode) {
        super(message, ErrorType.API, "FW_API_002");
        this.statusCode = statusCode;
        this.responseBody = null;
    }
    
    public ApiException(String message, int statusCode, String responseBody) {
        super(message, ErrorType.API, "FW_API_002");
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public ApiException(String message, Throwable cause, String errorCode) {
        super(message, cause, ErrorType.API, errorCode);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public ApiException(String message, String errorCode) {
        super(message, ErrorType.API, errorCode);
        this.statusCode = -1;
        this.responseBody = null;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    // Specific API error methods
    public static ApiException requestFailed(String method, String endpoint, Throwable cause) {
        return new ApiException(String.format("%s request to %s failed", method, endpoint), cause, "FW_API_001");
    }
    
    public static ApiException unexpectedStatusCode(int expected, int actual) {
        return new ApiException(String.format("Unexpected status code: %d. Expected: %d", actual, expected), actual);
    }
    
    public static ApiException invalidResponseBody(String expectedType) {
        return new ApiException("Failed to parse response body to " + expectedType, "FW_API_003");
    }
    
    public static ApiException authenticationFailed(String message) {
        return new ApiException("Authentication failed: " + message, "FW_AUTH_001");
    }
    
    public static ApiException tokenNotFound() {
        return new ApiException("Authentication token not found in response", "FW_AUTH_002");
    }
    
    public static ApiException requestTimeout(String endpoint) {
        return new ApiException("Request timeout for endpoint: " + endpoint, "FW_TIMEOUT_001");
    }
    
    public static ApiException retryLimitExceeded(int retryCount) {
        return new ApiException("Request failed after " + (retryCount + 1) + " attempts", "FW_API_004");
    }
    
    @Override
    public String toString() {
        String baseString = super.toString();
        if (statusCode > 0) {
            baseString += String.format(" [Status: %d]", statusCode);
        }
        if (responseBody != null && !responseBody.isEmpty()) {
            baseString += String.format(" [Response: %s]", responseBody.length() > 100 ? 
                responseBody.substring(0, 100) + "..." : responseBody);
        }
        return baseString;
    }
}
package com.starlettech.managers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import com.starlettech.config.ApiConfig;
import com.starlettech.enums.HttpMethod;
import com.starlettech.utils.ApiUtils;

/**
 * API Request Manager for handling API requests with retry logic and response validation
 */
public class ApiRequestManager {
    private static final Logger logger = LogManager.getLogger(ApiRequestManager.class);
    private static final ThreadLocal<String> authTokenThreadLocal = new ThreadLocal<>();
    private static ApiConfig apiConfig = ApiConfig.getInstance();

    /**
     * Set authentication token for current thread
     */
    public static void setAuthToken(String token) {
        authTokenThreadLocal.set(token);
        logger.debug("Authentication token set for current thread");
    }

    /**
     * Get authentication token for current thread
     */
    public static String getAuthToken() {
        return authTokenThreadLocal.get();
    }

    /**
     * Clear authentication token for current thread
     */
    public static void clearAuthToken() {
        authTokenThreadLocal.remove();
        logger.debug("Authentication token cleared for current thread");
    }

    /**
     * Make authenticated GET request
     */
    public static APIResponse authenticatedGet(String endpoint) {
        Map<String, String> headers = getAuthHeaders();
        return ApiUtils.get(endpoint, headers);
    }

    /**
     * Make authenticated POST request
     */
    public static APIResponse authenticatedPost(String endpoint, Object body) {
        Map<String, String> headers = getAuthHeaders();
        return ApiUtils.post(endpoint, body, headers);
    }

    /**
     * Make authenticated PUT request
     */
    public static APIResponse authenticatedPut(String endpoint, Object body) {
        Map<String, String> headers = getAuthHeaders();
        return ApiUtils.put(endpoint, body, headers);
    }

    /**
     * Make authenticated DELETE request
     */
    public static APIResponse authenticatedDelete(String endpoint) {
        Map<String, String> headers = getAuthHeaders();
        return ApiUtils.delete(endpoint, headers);
    }

    /**
     * Make authenticated PATCH request
     */
    public static APIResponse authenticatedPatch(String endpoint, Object body) {
        Map<String, String> headers = getAuthHeaders();
        return ApiUtils.patch(endpoint, body, headers);
    }

    /**
     * Make request with retry logic
     */
    public static APIResponse makeRequestWithRetry(HttpMethod method, String endpoint, Object body, Map<String, String> headers) {
        int retryCount = apiConfig.getRetryCount();
        APIResponse response = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= retryCount + 1; attempt++) {
            try {
                logger.info("Making {} request to {} (attempt {}/{})", method, endpoint, attempt, retryCount + 1);

                response = ApiUtils.makeRequest(method, endpoint, body, headers);

                // Check if response is successful
                if (ApiUtils.isSuccessStatusCode(response)) {
                    logger.info("Request successful on attempt {}", attempt);
                    return response;
                } else {
                    logger.warn("Request failed with status {} on attempt {}", response.status(), attempt);
                    if (attempt <= retryCount) {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    }
                }
            } catch (Exception e) {
                lastException = e;
                logger.error("Request failed on attempt {}: {}", attempt, e.getMessage());
                if (attempt <= retryCount) {
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Request interrupted", ie);
                    }
                }
            }
        }

        if (response != null) {
            return response;
        } else if (lastException != null) {
            throw new RuntimeException("Request failed after " + (retryCount + 1) + " attempts", lastException);
        } else {
            throw new RuntimeException("Request failed after " + (retryCount + 1) + " attempts");
        }
    }

    /**
     * Make authenticated request with retry logic
     */
    public static APIResponse makeAuthenticatedRequestWithRetry(HttpMethod method, String endpoint, Object body) {
        Map<String, String> headers = getAuthHeaders();
        return makeRequestWithRetry(method, endpoint, body, headers);
    }

    /**
     * Validate response and extract data
     */
    public static <T> T validateAndExtractResponse(APIResponse response, Class<T> responseClass, int expectedStatusCode) {
        // Validate status code
        if (!ApiUtils.isStatusCodeValid(response, expectedStatusCode)) {
            throw new RuntimeException("Unexpected status code: " + response.status() + ". Expected: " + expectedStatusCode);
        }

        // Extract and validate response body
        T responseObject = ApiUtils.getResponseBodyAsObject(response, responseClass);
        if (responseObject == null) {
            throw new RuntimeException("Failed to parse response body to " + responseClass.getSimpleName());
        }

        logger.info("Response validated and extracted successfully");
        return responseObject;
    }

    /**
     * Validate JSON response structure
     */
    public static JsonNode validateJsonResponse(APIResponse response, int expectedStatusCode, String[] requiredFields) {
        // Validate status code
        if (!ApiUtils.isStatusCodeValid(response, expectedStatusCode)) {
            throw new RuntimeException("Unexpected status code: " + response.status() + ". Expected: " + expectedStatusCode);
        }

        // Get response as JSON
        JsonNode jsonResponse = ApiUtils.getResponseBodyAsJson(response);
        if (jsonResponse == null) {
            throw new RuntimeException("Response body is not valid JSON");
        }

        // Validate required fields
        if (!ApiUtils.validateJsonSchema(jsonResponse, requiredFields)) {
            throw new RuntimeException("Response JSON is missing required fields");
        }

        logger.info("JSON response validated successfully");
        return jsonResponse;
    }

    /**
     * Login and get authentication token
     */
    public static String login(String username, String password) {
        try {
            Map<String, Object> loginPayload = new HashMap<>();
            loginPayload.put("username", username);
            loginPayload.put("password", password);

            APIResponse response = ApiUtils.post(apiConfig.getAuthEndpoint(), loginPayload);

            if (!ApiUtils.isSuccessStatusCode(response)) {
                throw new RuntimeException("Login failed with status: " + response.status());
            }

            JsonNode responseJson = ApiUtils.getResponseBodyAsJson(response);
            if (responseJson != null && responseJson.has("token")) {
                String token = responseJson.get("token").asText();
                setAuthToken(token);
                logger.info("Login successful for user: {}", username);
                return token;
            } else {
                throw new RuntimeException("Token not found in login response");
            }
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Login failed", e);
        }
    }

    /**
     * Logout and clear authentication token
     */
    public static void logout() {
        try {
            String token = getAuthToken();
            if (token != null) {
                // Make logout request if endpoint exists
                Map<String, String> headers = getAuthHeaders();
                ApiUtils.post("/api/logout", null, headers);
            }
        } catch (Exception e) {
            logger.warn("Logout request failed: {}", e.getMessage());
        } finally {
            clearAuthToken();
            logger.info("Logout completed");
        }
    }

    /**
     * Get user profile information
     */
    public static JsonNode getUserProfile() {
        APIResponse response = authenticatedGet(apiConfig.getUserEndpoint() + "/profile");
        return validateJsonResponse(response, 200, new String[]{"id", "username"});
    }

    /**
     * Create new user
     */
    public static JsonNode createUser(Object userData) {
        APIResponse response = authenticatedPost(apiConfig.getUserEndpoint(), userData);
        return validateJsonResponse(response, 201, new String[]{"id", "username"});
    }

    /**
     * Update user information
     */
    public static JsonNode updateUser(String userId, Object userData) {
        APIResponse response = authenticatedPut(apiConfig.getUserEndpoint() + "/" + userId, userData);
        return validateJsonResponse(response, 200, new String[]{"id", "username"});
    }

    /**
     * Delete user
     */
    public static void deleteUser(String userId) {
        APIResponse response = authenticatedDelete(apiConfig.getUserEndpoint() + "/" + userId);
        if (!ApiUtils.isStatusCodeValid(response, 204) && !ApiUtils.isStatusCodeValid(response, 200)) {
            throw new RuntimeException("Failed to delete user. Status: " + response.status());
        }
        logger.info("User deleted successfully: {}", userId);
    }

    /**
     * Get all users
     */
    public static JsonNode getAllUsers() {
        APIResponse response = authenticatedGet(apiConfig.getUserEndpoint());
        return validateJsonResponse(response, 200, new String[]{});
    }

    /**
     * Get user by ID
     */
    public static JsonNode getUserById(String userId) {
        APIResponse response = authenticatedGet(apiConfig.getUserEndpoint() + "/" + userId);
        return validateJsonResponse(response, 200, new String[]{"id", "username"});
    }

    /**
     * Search users
     */
    public static JsonNode searchUsers(String query) {
        APIResponse response = authenticatedGet(apiConfig.getUserEndpoint() + "/search?q=" + query);
        return validateJsonResponse(response, 200, new String[]{});
    }

    /**
     * Get authentication headers
     */
    private static Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        String token = getAuthToken();
        if (token != null) {
            headers.put("Authorization", "Bearer " + token);
        }
        headers.put("Content-Type", apiConfig.getContentType());
        return headers;
    }

    /**
     * Validate response time
     */
    public static boolean validateResponseTime(long startTime, long maxResponseTime) {
        long responseTime = System.currentTimeMillis() - startTime;
        boolean isValid = responseTime <= maxResponseTime;

        if (isValid) {
            logger.info("Response time validation passed: {}ms (max: {}ms)", responseTime, maxResponseTime);
        } else {
            logger.warn("Response time validation failed: {}ms (max: {}ms)", responseTime, maxResponseTime);
        }

        return isValid;
    }

    /**
     * Extract error message from response
     */
    public static String extractErrorMessage(APIResponse response) {
        try {
            JsonNode errorResponse = ApiUtils.getResponseBodyAsJson(response);
            if (errorResponse != null) {
                if (errorResponse.has("message")) {
                    return errorResponse.get("message").asText();
                } else if (errorResponse.has("error")) {
                    return errorResponse.get("error").asText();
                } else if (errorResponse.has("detail")) {
                    return errorResponse.get("detail").asText();
                }
            }
            return "Unknown error";
        } catch (Exception e) {
            logger.error("Failed to extract error message: {}", e.getMessage());
            return "Failed to parse error response";
        }
    }

    /**
     * Cleanup resources
     */
    public static void cleanup() {
        clearAuthToken();
        ApiUtils.closeApiContext();
        logger.info("API Request Manager cleanup completed");
    }
}

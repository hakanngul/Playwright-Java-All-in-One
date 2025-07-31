package com.starlettech.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.starlettech.config.ApiConfig;
import com.starlettech.core.PlaywrightManager;
import com.starlettech.enums.HttpMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for API operations
 */
public class ApiUtils {
    private static final Logger logger = LogManager.getLogger(ApiUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static ApiConfig apiConfig = ApiConfig.getInstance();
    private static APIRequestContext apiRequestContext;

    /**
     * Initialize API request context
     */
    public static void initializeApiContext() {
        if (apiRequestContext == null) {
            Playwright playwright = PlaywrightManager.getPlaywright();
            if (playwright == null) {
                PlaywrightManager.initializePlaywright();
                playwright = PlaywrightManager.getPlaywright();
            }

            APIRequest apiRequest = playwright.request();
            apiRequestContext = apiRequest.newContext(new APIRequest.NewContextOptions()
                .setBaseURL(apiConfig.getBaseUrl())
                .setTimeout(apiConfig.getTimeout()));

            logger.info("API request context initialized with base URL: {}", apiConfig.getBaseUrl());
        }
    }

    /**
     * Get API request context
     */
    public static APIRequestContext getApiContext() {
        if (apiRequestContext == null) {
            initializeApiContext();
        }
        return apiRequestContext;
    }

    /**
     * Make GET request
     */
    public static APIResponse get(String endpoint) {
        return get(endpoint, new HashMap<>());
    }

    /**
     * Make GET request with headers
     */
    public static APIResponse get(String endpoint, Map<String, String> headers) {
        try {
            APIRequestContext context = getApiContext();
            RequestOptions options = RequestOptions.create().setHeaders(headers);

            logger.info("Making GET request to: {}", endpoint);
            APIResponse response = context.get(endpoint, options);

            if (apiConfig.isLoggingEnabled()) {
                logResponse("GET", endpoint, response);
            }

            return response;
        } catch (Exception e) {
            logger.error("Failed to make GET request to {}: {}", endpoint, e.getMessage());
            throw new RuntimeException("GET request failed", e);
        }
    }

    /**
     * Make POST request
     */
    public static APIResponse post(String endpoint, Object body) {
        return post(endpoint, body, new HashMap<>());
    }

    /**
     * Make POST request with headers
     */
    public static APIResponse post(String endpoint, Object body, Map<String, String> headers) {
        try {
            APIRequestContext context = getApiContext();

            // Set default content type if not provided
            if (!headers.containsKey("Content-Type")) {
                headers.put("Content-Type", apiConfig.getContentType());
            }

            RequestOptions options = RequestOptions.create()
                .setHeaders(headers)
                .setData(JsonUtils.toJsonString(body));

            logger.info("Making POST request to: {}", endpoint);
            APIResponse response = context.post(endpoint, options);

            if (apiConfig.isLoggingEnabled()) {
                logResponse("POST", endpoint, response);
            }

            return response;
        } catch (Exception e) {
            logger.error("Failed to make POST request to {}: {}", endpoint, e.getMessage());
            throw new RuntimeException("POST request failed", e);
        }
    }

    /**
     * Make PUT request
     */
    public static APIResponse put(String endpoint, Object body) {
        return put(endpoint, body, new HashMap<>());
    }

    /**
     * Make PUT request with headers
     */
    public static APIResponse put(String endpoint, Object body, Map<String, String> headers) {
        try {
            APIRequestContext context = getApiContext();

            // Set default content type if not provided
            if (!headers.containsKey("Content-Type")) {
                headers.put("Content-Type", apiConfig.getContentType());
            }

            RequestOptions options = RequestOptions.create()
                .setHeaders(headers)
                .setData(JsonUtils.toJsonString(body));

            logger.info("Making PUT request to: {}", endpoint);
            APIResponse response = context.put(endpoint, options);

            if (apiConfig.isLoggingEnabled()) {
                logResponse("PUT", endpoint, response);
            }

            return response;
        } catch (Exception e) {
            logger.error("Failed to make PUT request to {}: {}", endpoint, e.getMessage());
            throw new RuntimeException("PUT request failed", e);
        }
    }

    /**
     * Make DELETE request
     */
    public static APIResponse delete(String endpoint) {
        return delete(endpoint, new HashMap<>());
    }

    /**
     * Make DELETE request with headers
     */
    public static APIResponse delete(String endpoint, Map<String, String> headers) {
        try {
            APIRequestContext context = getApiContext();
            RequestOptions options = RequestOptions.create().setHeaders(headers);

            logger.info("Making DELETE request to: {}", endpoint);
            APIResponse response = context.delete(endpoint, options);

            if (apiConfig.isLoggingEnabled()) {
                logResponse("DELETE", endpoint, response);
            }

            return response;
        } catch (Exception e) {
            logger.error("Failed to make DELETE request to {}: {}", endpoint, e.getMessage());
            throw new RuntimeException("DELETE request failed", e);
        }
    }

    /**
     * Make PATCH request
     */
    public static APIResponse patch(String endpoint, Object body) {
        return patch(endpoint, body, new HashMap<>());
    }

    /**
     * Make PATCH request with headers
     */
    public static APIResponse patch(String endpoint, Object body, Map<String, String> headers) {
        try {
            APIRequestContext context = getApiContext();

            // Set default content type if not provided
            if (!headers.containsKey("Content-Type")) {
                headers.put("Content-Type", apiConfig.getContentType());
            }

            RequestOptions options = RequestOptions.create()
                .setHeaders(headers)
                .setData(JsonUtils.toJsonString(body));

            logger.info("Making PATCH request to: {}", endpoint);
            APIResponse response = context.patch(endpoint, options);

            if (apiConfig.isLoggingEnabled()) {
                logResponse("PATCH", endpoint, response);
            }

            return response;
        } catch (Exception e) {
            logger.error("Failed to make PATCH request to {}: {}", endpoint, e.getMessage());
            throw new RuntimeException("PATCH request failed", e);
        }
    }

    /**
     * Make generic HTTP request
     */
    public static APIResponse makeRequest(HttpMethod method, String endpoint, Object body, Map<String, String> headers) {
        return switch (method) {
            case GET -> get(endpoint, headers);
            case POST -> post(endpoint, body, headers);
            case PUT -> put(endpoint, body, headers);
            case DELETE -> delete(endpoint, headers);
            case PATCH -> patch(endpoint, body, headers);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }

    /**
     * Get response body as string
     */
    public static String getResponseBody(APIResponse response) {
        try {
            return new String(response.body());
        } catch (Exception e) {
            logger.error("Failed to get response body: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Get response body as JsonNode
     */
    public static JsonNode getResponseBodyAsJson(APIResponse response) {
        try {
            String body = getResponseBody(response);
            return JsonUtils.parseJson(body);
        } catch (Exception e) {
            logger.error("Failed to parse response body as JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get response body as object
     */
    public static <T> T getResponseBodyAsObject(APIResponse response, Class<T> clazz) {
        try {
            String body = getResponseBody(response);
            return JsonUtils.fromJsonString(body, clazz);
        } catch (Exception e) {
            logger.error("Failed to parse response body as {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Validate response status code
     */
    public static boolean isStatusCodeValid(APIResponse response, int expectedStatusCode) {
        int actualStatusCode = response.status();
        boolean isValid = actualStatusCode == expectedStatusCode;

        if (!isValid) {
            logger.warn("Status code mismatch. Expected: {}, Actual: {}", expectedStatusCode, actualStatusCode);
        }

        return isValid;
    }

    /**
     * Validate response status code is in success range (200-299)
     */
    public static boolean isSuccessStatusCode(APIResponse response) {
        int statusCode = response.status();
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Get response header value
     */
    public static String getResponseHeader(APIResponse response, String headerName) {
        try {
            return response.headers().get(headerName.toLowerCase());
        } catch (Exception e) {
            logger.error("Failed to get response header {}: {}", headerName, e.getMessage());
            return null;
        }
    }

    /**
     * Get all response headers
     */
    public static Map<String, String> getAllResponseHeaders(APIResponse response) {
        try {
            return response.headers();
        } catch (Exception e) {
            logger.error("Failed to get response headers: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Add authentication header
     */
    public static Map<String, String> addAuthHeader(Map<String, String> headers, String token) {
        Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put("Authorization", "Bearer " + token);
        return newHeaders;
    }

    /**
     * Add basic authentication header
     */
    public static Map<String, String> addBasicAuthHeader(Map<String, String> headers, String username, String password) {
        Map<String, String> newHeaders = new HashMap<>(headers);
        String credentials = java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        newHeaders.put("Authorization", "Basic " + credentials);
        return newHeaders;
    }

    /**
     * Log response details
     */
    private static void logResponse(String method, String endpoint, APIResponse response) {
        logger.info("{} {} - Status: {}, Response Time: {}ms",
            method, endpoint, response.status(),
            System.currentTimeMillis() - System.currentTimeMillis()); // This would need proper timing implementation

        if (logger.isDebugEnabled()) {
            logger.debug("Response Headers: {}", response.headers());
            logger.debug("Response Body: {}", getResponseBody(response));
        }
    }

    /**
     * Close API request context
     */
    public static void closeApiContext() {
        if (apiRequestContext != null) {
            apiRequestContext.dispose();
            apiRequestContext = null;
            logger.info("API request context closed");
        }
    }

    /**
     * Validate JSON schema (basic validation)
     */
    public static boolean validateJsonSchema(JsonNode jsonNode, String[] requiredFields) {
        if (jsonNode == null) {
            return false;
        }

        for (String field : requiredFields) {
            if (!jsonNode.has(field)) {
                logger.warn("Required field '{}' is missing from JSON response", field);
                return false;
            }
        }

        return true;
    }

    /**
     * Extract value from JSON response by path
     */
    public static String extractValueFromResponse(APIResponse response, String jsonPath) {
        try {
            JsonNode jsonNode = getResponseBodyAsJson(response);
            return JsonUtils.getValueByPath(jsonNode, jsonPath);
        } catch (Exception e) {
            logger.error("Failed to extract value from response using path {}: {}", jsonPath, e.getMessage());
            return null;
        }
    }
}

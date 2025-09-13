package com.starlettech.core.base;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import com.starlettech.config.ApiConfig;
import com.starlettech.config.TestConfig;
import com.starlettech.managers.ApiRequestManager;
import com.starlettech.managers.PlaywrightManager;
import com.starlettech.utils.ApiUtils;
import com.starlettech.utils.TestDataReader;

/**
 * Base API Test class for API tests
 */
public abstract class BaseApiTest {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected ApiConfig apiConfig;
    protected TestConfig testConfig;
    protected TestDataReader testDataReader;
    protected long testStartTime;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        logger.info("Starting API test suite execution");
        apiConfig = ApiConfig.getInstance();
        testConfig = TestConfig.getInstance();
        testDataReader = TestDataReader.getInstance();

        // Initialize Playwright for API context
        PlaywrightManager.initializePlaywright();
        ApiUtils.initializeApiContext();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        logger.info("Starting API test class: {}", this.getClass().getSimpleName());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        logger.info("Starting API test method: {}", method.getName());
        testStartTime = System.currentTimeMillis();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        long testDuration = System.currentTimeMillis() - testStartTime;

        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("API test method failed: {} (Duration: {}ms)", methodName, testDuration);
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("API test method passed: {} (Duration: {}ms)", methodName, testDuration);
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("API test method skipped: {}", methodName);
        }

        // Clear authentication token after each test
        ApiRequestManager.clearAuthToken();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("Completed API test class: {}", this.getClass().getSimpleName());
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        // Cleanup API resources
        ApiRequestManager.cleanup();
        PlaywrightManager.cleanup();
        logger.info("Completed API test suite execution");
    }

    // ========== Helper Methods ==========

    /**
     * Login with test user credentials
     */
    protected String loginWithTestUser() {
        JsonNode userData = testDataReader.getUserData("testuser1");
        if (userData != null) {
            String username = userData.get("username").asText();
            String password = userData.get("password").asText();
            return ApiRequestManager.login(username, password);
        } else {
            throw new RuntimeException("Test user data not found");
        }
    }

    /**
     * Login with admin credentials
     */
    protected String loginWithAdmin() {
        JsonNode adminData = testDataReader.getAdminUser();
        if (adminData != null) {
            String username = adminData.get("username").asText();
            String password = adminData.get("password").asText();
            return ApiRequestManager.login(username, password);
        } else {
            throw new RuntimeException("Admin user data not found");
        }
    }

    /**
     * Validate response status code
     */
    protected void validateStatusCode(APIResponse response, int expectedStatusCode) {
        int actualStatusCode = response.status();
        Assert.assertEquals(actualStatusCode, expectedStatusCode,
            "Status code mismatch. Expected: " + expectedStatusCode + ", Actual: " + actualStatusCode);
    }

    /**
     * Validate response is successful (2xx)
     */
    protected void validateSuccessResponse(APIResponse response) {
        Assert.assertTrue(ApiUtils.isSuccessStatusCode(response),
            "Response should be successful but got status: " + response.status());
    }

    /**
     * Validate response contains expected fields
     */
    protected void validateResponseFields(APIResponse response, String... expectedFields) {
        JsonNode responseJson = ApiUtils.getResponseBodyAsJson(response);
        Assert.assertNotNull(responseJson, "Response body should be valid JSON");

        for (String field : expectedFields) {
            Assert.assertTrue(responseJson.has(field),
                "Response should contain field: " + field);
        }
    }

    /**
     * Validate response field value
     */
    protected void validateResponseFieldValue(APIResponse response, String fieldPath, String expectedValue) {
        JsonNode responseJson = ApiUtils.getResponseBodyAsJson(response);
        Assert.assertNotNull(responseJson, "Response body should be valid JSON");

        String actualValue = com.starlettech.utils.JsonUtils.getValueByPath(responseJson, fieldPath);
        Assert.assertEquals(actualValue, expectedValue,
            "Field '" + fieldPath + "' value mismatch");
    }

    /**
     * Validate response time
     */
    protected void validateResponseTime(long maxResponseTimeMs) {
        long actualResponseTime = System.currentTimeMillis() - testStartTime;
        Assert.assertTrue(actualResponseTime <= maxResponseTimeMs,
            "Response time exceeded limit. Expected: <=" + maxResponseTimeMs + "ms, Actual: " + actualResponseTime + "ms");
    }

    /**
     * Validate error response
     */
    protected void validateErrorResponse(APIResponse response, int expectedStatusCode, String expectedErrorMessage) {
        validateStatusCode(response, expectedStatusCode);

        String actualErrorMessage = ApiRequestManager.extractErrorMessage(response);
        Assert.assertTrue(actualErrorMessage.contains(expectedErrorMessage),
            "Error message should contain: '" + expectedErrorMessage + "', but was: '" + actualErrorMessage + "'");
    }

    /**
     * Get test data payload
     */
    protected JsonNode getTestPayload(String payloadName) {
        JsonNode payload = testDataReader.getApiPayload(payloadName);
        Assert.assertNotNull(payload, "Test payload '" + payloadName + "' not found");
        return payload;
    }

    /**
     * Create test user and return user data
     */
    protected JsonNode createTestUser() {
        JsonNode createUserPayload = getTestPayload("createUser");
        APIResponse response = ApiRequestManager.authenticatedPost(apiConfig.getUserEndpoint(), createUserPayload);
        validateStatusCode(response, 201);
        return ApiUtils.getResponseBodyAsJson(response);
    }

    /**
     * Delete test user
     */
    protected void deleteTestUser(String userId) {
        ApiRequestManager.deleteUser(userId);
        logger.info("Test user deleted: {}", userId);
    }

    /**
     * Validate JSON array response
     */
    protected void validateJsonArrayResponse(APIResponse response, int expectedStatusCode) {
        validateStatusCode(response, expectedStatusCode);
        JsonNode responseJson = ApiUtils.getResponseBodyAsJson(response);
        Assert.assertNotNull(responseJson, "Response body should be valid JSON");
        Assert.assertTrue(responseJson.isArray(), "Response should be a JSON array");
    }

    /**
     * Validate JSON object response
     */
    protected void validateJsonObjectResponse(APIResponse response, int expectedStatusCode) {
        validateStatusCode(response, expectedStatusCode);
        JsonNode responseJson = ApiUtils.getResponseBodyAsJson(response);
        Assert.assertNotNull(responseJson, "Response body should be valid JSON");
        Assert.assertTrue(responseJson.isObject(), "Response should be a JSON object");
    }

    /**
     * Validate response header
     */
    protected void validateResponseHeader(APIResponse response, String headerName, String expectedValue) {
        String actualValue = ApiUtils.getResponseHeader(response, headerName);
        Assert.assertEquals(actualValue, expectedValue,
            "Header '" + headerName + "' value mismatch");
    }

    /**
     * Validate response header exists
     */
    protected void validateResponseHeaderExists(APIResponse response, String headerName) {
        String headerValue = ApiUtils.getResponseHeader(response, headerName);
        Assert.assertNotNull(headerValue, "Header '" + headerName + "' should exist");
        Assert.assertFalse(headerValue.isEmpty(), "Header '" + headerName + "' should not be empty");
    }

    /**
     * Validate content type
     */
    protected void validateContentType(APIResponse response, String expectedContentType) {
        validateResponseHeader(response, "content-type", expectedContentType);
    }

    /**
     * Validate JSON content type
     */
    protected void validateJsonContentType(APIResponse response) {
        String contentType = ApiUtils.getResponseHeader(response, "content-type");
        Assert.assertNotNull(contentType, "Content-Type header should exist");
        Assert.assertTrue(contentType.contains("application/json"),
            "Content-Type should be JSON but was: " + contentType);
    }

    /**
     * Log response details for debugging
     */
    protected void logResponseDetails(APIResponse response) {
        if (logger.isDebugEnabled()) {
            logger.debug("Response Status: {}", response.status());
            logger.debug("Response Headers: {}", response.headers());
            logger.debug("Response Body: {}", ApiUtils.getResponseBody(response));
        }
    }

    /**
     * Wait for async operation to complete
     */
    protected void waitForAsyncOperation(int timeoutSeconds) {
        try {
            Thread.sleep(timeoutSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }

    /**
     * Generate unique test data
     */
    protected String generateUniqueId() {
        return "test_" + System.currentTimeMillis();
    }

    /**
     * Generate unique email
     */
    protected String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * Generate unique username
     */
    protected String generateUniqueUsername() {
        return "testuser_" + System.currentTimeMillis();
    }

    /**
     * Validate pagination response
     */
    protected void validatePaginationResponse(APIResponse response, String[] expectedFields) {
        validateJsonObjectResponse(response, 200);
        validateResponseFields(response, expectedFields);

        JsonNode responseJson = ApiUtils.getResponseBodyAsJson(response);
        Assert.assertTrue(responseJson.has("data") || responseJson.has("items"),
            "Pagination response should have 'data' or 'items' field");
    }

    /**
     * Validate empty response
     */
    protected void validateEmptyResponse(APIResponse response, int expectedStatusCode) {
        validateStatusCode(response, expectedStatusCode);
        String responseBody = ApiUtils.getResponseBody(response);
        Assert.assertTrue(responseBody == null || responseBody.trim().isEmpty(),
            "Response body should be empty");
    }
}

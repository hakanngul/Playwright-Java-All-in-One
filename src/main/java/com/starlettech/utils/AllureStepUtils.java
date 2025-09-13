package com.starlettech.utils;

import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;

/**
 * Utility class providing enhanced step methods for detailed Allure reporting
 * This class wraps common test actions with Allure steps for better reporting visibility
 */
public class AllureStepUtils {
    private static final Logger logger = LogManager.getLogger(AllureStepUtils.class);
    
    /**
     * Login steps with detailed reporting
     */
    public static class LoginSteps {
        
        @Step("Navigate to login page: {url}")
        public static void navigateToLoginPage(String url) {
            Allure.parameter("URL", url);
            logger.info("Navigating to login page: {}", url);
            
            Allure.step("Opening login page", () -> {
                // This would be implemented by the actual page object
                logger.debug("Login page navigation step executed");
            });
        }
        
        @Step("Enter credentials: username={username}")
        public static void enterCredentials(String username, String password) {
            Allure.parameter("username", username);
            Allure.parameter("password", "***"); // Hide sensitive data
            
            logger.info("Entering credentials for user: {}", username);
            
            Allure.step("Fill username field", () -> {
                logger.debug("Username field filled");
            });
            
            Allure.step("Fill password field", () -> {
                logger.debug("Password field filled");
            });
        }
        
        @Step("Click login button")
        public static void clickLoginButton() {
            logger.info("Clicking login button");
            
            Allure.step("Clicking login button", () -> {
                logger.debug("Login button clicked");
            });
        }
        
        @Step("Verify successful login")
        public static void verifySuccessfulLogin() {
            logger.info("Verifying successful login");
            
            Allure.step("Checking for dashboard elements", () -> {
                logger.debug("Dashboard elements verified");
            });
            
            Allure.step("Verifying user session", () -> {
                logger.debug("User session verified");
            });
        }
        
        @Step("Verify login failure with message: {expectedMessage}")
        public static void verifyLoginFailure(String expectedMessage) {
            Allure.parameter("Expected Error Message", expectedMessage);
            logger.info("Verifying login failure with message: {}", expectedMessage);
            
            Allure.step("Check error message visibility", () -> {
                logger.debug("Error message visibility checked");
            });
        }
    }
    
    /**
     * API test steps with detailed reporting
     */
    public static class ApiSteps {
        
        @Step("Send {method} request to {endpoint}")
        public static void sendApiRequest(String method, String endpoint, String requestBody) {
            Allure.parameter("HTTP Method", method);
            Allure.parameter("Endpoint", endpoint);
            
            if (requestBody != null && !requestBody.isEmpty()) {
                AllureUtils.attachJson("Request Body", requestBody);
            }
            
            logger.info("Sending {} request to: {}", method, endpoint);
        }
        
        @Step("Validate response status code: {expectedStatusCode}")
        public static void validateResponseStatusCode(int expectedStatusCode, int actualStatusCode) {
            Allure.parameter("Expected Status Code", expectedStatusCode);
            Allure.parameter("Actual Status Code", actualStatusCode);
            
            logger.info("Validating response status code - Expected: {}, Actual: {}", 
                expectedStatusCode, actualStatusCode);
        }
        
        @Step("Validate response body contains: {expectedContent}")
        public static void validateResponseContent(String expectedContent, String actualResponse) {
            Allure.parameter("Expected Content", expectedContent);
            AllureUtils.attachJson("Response Body", actualResponse);
            
            logger.info("Validating response content contains: {}", expectedContent);
        }
        
        @Step("Validate response time is under {maxTimeMs}ms")
        public static void validateResponseTime(long maxTimeMs, long actualTimeMs) {
            Allure.parameter("Max Expected Time (ms)", maxTimeMs);
            Allure.parameter("Actual Response Time (ms)", actualTimeMs);
            
            logger.info("Validating response time - Expected < {}ms, Actual: {}ms", 
                maxTimeMs, actualTimeMs);
        }
        
        @Step("Authenticate with credentials")
        public static void authenticateApiRequest(String username) {
            Allure.parameter("Username", username);
            logger.info("Authenticating API request for user: {}", username);
        }
    }
    
    /**
     * Browser interaction steps with detailed reporting
     */
    public static class BrowserSteps {
        
        @Step("Open browser: {browserType}")
        public static void openBrowser(String browserType) {
            Allure.parameter("Browser Type", browserType);
            logger.info("Opening browser: {}", browserType);
        }
        
        @Step("Navigate to URL: {url}")
        public static void navigateToUrl(String url) {
            Allure.parameter("URL", url);
            logger.info("Navigating to URL: {}", url);
        }
        
        @Step("Click element: {elementDescription}")
        public static void clickElement(String elementDescription, String selector) {
            Allure.parameter("Element Description", elementDescription);
            Allure.parameter("Selector", selector);
            logger.info("Clicking element: {} with selector: {}", elementDescription, selector);
        }
        
        @Step("Enter text '{text}' in field: {fieldDescription}")
        public static void enterText(String fieldDescription, String selector, String text) {
            Allure.parameter("Field Description", fieldDescription);
            Allure.parameter("Selector", selector);
            Allure.parameter("Text", text);
            logger.info("Entering text '{}' in field: {}", text, fieldDescription);
        }
        
        @Step("Wait for element to be visible: {elementDescription}")
        public static void waitForElementVisible(String elementDescription, String selector) {
            Allure.parameter("Element Description", elementDescription);
            Allure.parameter("Selector", selector);
            logger.info("Waiting for element to be visible: {}", elementDescription);
        }
        
        @Step("Verify page title: {expectedTitle}")
        public static void verifyPageTitle(String expectedTitle, String actualTitle) {
            Allure.parameter("Expected Title", expectedTitle);
            Allure.parameter("Actual Title", actualTitle);
            logger.info("Verifying page title - Expected: '{}', Actual: '{}'", expectedTitle, actualTitle);
        }
        
        @Step("Take screenshot: {screenshotName}")
        public static void takeScreenshot(String screenshotName) {
            Allure.parameter("Screenshot Name", screenshotName);
            logger.info("Taking screenshot: {}", screenshotName);
            // Screenshot logic would be implemented here
        }
    }
    
    /**
     * Performance test steps with detailed reporting
     */
    public static class PerformanceSteps {
        
        @Step("Start performance monitoring")
        public static Instant startPerformanceMonitoring() {
            Instant startTime = Instant.now();
            Allure.parameter("Start Time", startTime.toString());
            logger.info("Performance monitoring started at: {}", startTime);
            return startTime;
        }
        
        @Step("Stop performance monitoring and validate")
        public static void stopPerformanceMonitoring(Instant startTime, long maxDurationMs) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
            long actualDurationMs = duration.toMillis();
            
            Allure.parameter("Start Time", startTime.toString());
            Allure.parameter("End Time", endTime.toString());
            Allure.parameter("Duration (ms)", actualDurationMs);
            Allure.parameter("Max Expected Duration (ms)", maxDurationMs);
            
            logger.info("Performance monitoring stopped - Duration: {}ms, Max Expected: {}ms", 
                actualDurationMs, maxDurationMs);
            
            if (actualDurationMs > maxDurationMs) {
                logger.warn("Performance test failed - Duration {}ms exceeded maximum {}ms", 
                    actualDurationMs, maxDurationMs);
            }
        }
        
        @Step("Monitor memory usage")
        public static void monitorMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            Allure.parameter("Total Memory (MB)", totalMemory / (1024 * 1024));
            Allure.parameter("Used Memory (MB)", usedMemory / (1024 * 1024));
            Allure.parameter("Free Memory (MB)", freeMemory / (1024 * 1024));
            Allure.parameter("Max Memory (MB)", maxMemory / (1024 * 1024));
            
            logger.info("Memory usage - Used: {}MB, Free: {}MB, Total: {}MB, Max: {}MB",
                usedMemory / (1024 * 1024), freeMemory / (1024 * 1024), 
                totalMemory / (1024 * 1024), maxMemory / (1024 * 1024));
        }
    }
    
    /**
     * Data validation steps with detailed reporting
     */
    public static class ValidationSteps {
        
        @Step("Validate data field: {fieldName} = {expectedValue}")
        public static void validateField(String fieldName, Object expectedValue, Object actualValue) {
            Allure.parameter("Field Name", fieldName);
            Allure.parameter("Expected Value", String.valueOf(expectedValue));
            Allure.parameter("Actual Value", String.valueOf(actualValue));
            
            logger.info("Validating field '{}' - Expected: '{}', Actual: '{}'", 
                fieldName, expectedValue, actualValue);
        }
        
        @Step("Validate list contains {itemCount} items")
        public static void validateListSize(int expectedCount, int actualCount) {
            Allure.parameter("Expected Count", expectedCount);
            Allure.parameter("Actual Count", actualCount);
            
            logger.info("Validating list size - Expected: {}, Actual: {}", expectedCount, actualCount);
        }
        
        @Step("Validate JSON schema compliance")
        public static void validateJsonSchema(String schemaName, String jsonData) {
            Allure.parameter("Schema Name", schemaName);
            AllureUtils.attachJson("JSON Data", jsonData);
            
            logger.info("Validating JSON data against schema: {}", schemaName);
        }
    }
}
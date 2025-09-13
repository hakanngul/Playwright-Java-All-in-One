package starlettech.tests.api;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.BaseApiTest;
import com.starlettech.enums.TestPriority;

import starlettech.api.clients.AuthApiClient;
import starlettech.api.models.AuthResponse;
import starlettech.api.models.User;

/**
 * Authentication API Tests
 */
@ApiTest
public class AuthApiTests extends BaseApiTest {
    
    private AuthApiClient authApiClient;
    
    @BeforeMethod
    public void setUp() {
        authApiClient = new AuthApiClient();
    }
    
    @AfterMethod
    public void tearDown() {
        // Cleanup authentication token
        try {
            authApiClient.logout();
        } catch (Exception e) {
            logger.debug("Cleanup logout failed: {}", e.getMessage());
        }
    }
    
    @Test(groups = {"smoke", "api", "auth"}, priority = 1)
    @TestInfo(description = "Test successful login with valid credentials", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testValidLogin() {
        // Test data
        String username = "testuser1";
        String password = "password123";
        
        // Perform login
        AuthResponse authResponse = authApiClient.login(username, password);
        
        // Assertions
        Assert.assertNotNull(authResponse, "Auth response should not be null");
        Assert.assertNotNull(authResponse.getToken(), "Token should not be null");
        Assert.assertNotNull(authResponse.getUser(), "User should not be null");
        Assert.assertEquals(authResponse.getUser().getUsername(), username, "Username should match");
        Assert.assertTrue(authResponse.getExpiresIn() > 0, "Token should have expiration time");
        
        logger.info("Valid login test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 2)
    @TestInfo(description = "Test login with invalid credentials", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testInvalidLogin() {
        // Test data
        String username = "invaliduser";
        String password = "wrongpassword";
        
        // Perform login and expect failure
        try {
            authApiClient.login(username, password);
            Assert.fail("Login should have failed with invalid credentials");
        } catch (RuntimeException e) {
            // Expected exception
            Assert.assertTrue(e.getMessage().contains("Login failed"), 
                "Error message should indicate login failure");
        }
        
        logger.info("Invalid login test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 3)
    @TestInfo(description = "Test login with empty credentials", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testEmptyCredentialsLogin() {
        // Test data
        String username = "";
        String password = "";
        
        // Perform login and expect failure
        try {
            authApiClient.login(username, password);
            Assert.fail("Login should have failed with empty credentials");
        } catch (RuntimeException e) {
            // Expected exception
            logger.info("Login correctly failed with empty credentials");
        }
        
        logger.info("Empty credentials login test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 4)
    @TestInfo(description = "Test token validation", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testTokenValidation() {
        // Login first
        String token = authApiClient.loginAndSetToken("testuser1", "password123");
        Assert.assertNotNull(token, "Token should not be null");
        
        // Validate token
        boolean isValid = authApiClient.validateToken();
        Assert.assertTrue(isValid, "Token should be valid");
        
        logger.info("Token validation test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 5)
    @TestInfo(description = "Test getting current user profile", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testGetCurrentUser() {
        // Login first
        authApiClient.loginAndSetToken("testuser1", "password123");
        
        // Get current user
        User currentUser = authApiClient.getCurrentUser();
        
        // Assertions
        Assert.assertNotNull(currentUser, "Current user should not be null");
        Assert.assertEquals(currentUser.getUsername(), "testuser1", "Username should match");
        Assert.assertNotNull(currentUser.getEmail(), "Email should not be null");
        
        logger.info("Get current user test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 6)
    @TestInfo(description = "Test logout functionality", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testLogout() {
        // Login first
        authApiClient.loginAndSetToken("testuser1", "password123");
        
        // Verify token is valid
        Assert.assertTrue(authApiClient.validateToken(), "Token should be valid before logout");
        
        // Logout
        authApiClient.logout();
        
        // Verify token is no longer valid
        Assert.assertFalse(authApiClient.validateToken(), "Token should be invalid after logout");
        
        logger.info("Logout test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 7)
    @TestInfo(description = "Test password reset request", 
              author = "API Test Engineer", 
              priority = TestPriority.LOW)
    public void testPasswordResetRequest() {
        // Test data
        String email = "testuser1@example.com";
        
        // Request password reset
        boolean resetRequested = authApiClient.requestPasswordReset(email);
        
        // Assertion
        Assert.assertTrue(resetRequested, "Password reset should be requested successfully");
        
        logger.info("Password reset request test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "auth"}, priority = 8)
    @TestInfo(description = "Test authentication status", 
              author = "API Test Engineer", 
              priority = TestPriority.LOW)
    public void testAuthStatus() {
        // Login first
        authApiClient.loginAndSetToken("testuser1", "password123");
        
        // Get auth status
        JsonNode authStatus = authApiClient.getAuthStatus();
        
        // Assertions
        Assert.assertNotNull(authStatus, "Auth status should not be null");
        Assert.assertTrue(authStatus.has("authenticated"), "Status should have authenticated field");
        Assert.assertTrue(authStatus.get("authenticated").asBoolean(), "User should be authenticated");
        
        logger.info("Auth status test completed successfully");
    }
    
    @Test(groups = {"performance", "api", "auth"}, priority = 9)
    @TestInfo(description = "Test login performance", 
              author = "API Test Engineer", 
              priority = TestPriority.LOW)
    public void testLoginPerformance() {
        long startTime = System.currentTimeMillis();
        
        // Perform login
        authApiClient.loginAndSetToken("testuser1", "password123");
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Performance assertion (login should complete within 2 seconds)
        Assert.assertTrue(responseTime < 2000, 
            "Login should complete within 2 seconds, actual: " + responseTime + "ms");
        
        logger.info("Login performance test completed - Response time: {}ms", responseTime);
    }
    
    @Test(groups = {"security", "api", "auth"}, priority = 10)
    @TestInfo(description = "Test SQL injection in login", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testSqlInjectionLogin() {
        // Test data with SQL injection attempt
        String maliciousUsername = "admin'; DROP TABLE users; --";
        String password = "password";
        
        // Attempt login with malicious input
        try {
            authApiClient.login(maliciousUsername, password);
            Assert.fail("Login should have failed with malicious input");
        } catch (RuntimeException e) {
            // Expected - login should fail
            logger.info("SQL injection attempt correctly blocked");
        }
        
        logger.info("SQL injection test completed successfully");
    }
}

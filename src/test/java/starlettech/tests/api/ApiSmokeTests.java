package starlettech.tests.api;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.BaseApiTest;
import com.starlettech.enums.TestPriority;

import starlettech.api.clients.AuthApiClient;
import starlettech.api.clients.UserApiClient;
import starlettech.api.models.AuthResponse;
import starlettech.api.models.User;

/**
 * API Smoke Tests - Critical API functionality verification
 */
@ApiTest
public class ApiSmokeTests extends BaseApiTest {
    
    private AuthApiClient authApiClient;
    private UserApiClient userApiClient;
    
    @BeforeMethod
    public void setUp() {
        authApiClient = new AuthApiClient();
        userApiClient = new UserApiClient();
    }
    
    @AfterMethod
    public void tearDown() {
        try {
            authApiClient.logout();
        } catch (Exception e) {
            logger.debug("Cleanup logout failed: {}", e.getMessage());
        }
    }
    
    @Test(groups = {"smoke", "api"}, priority = 1)
    @TestInfo(description = "Smoke test - API health check", 
              author = "API Test Engineer", 
              priority = TestPriority.CRITICAL)
    public void testApiHealthCheck() {
        // Simple health check by attempting to login
        try {
            AuthResponse authResponse = authApiClient.login("testuser1", "password123");
            Assert.assertNotNull(authResponse, "API should be accessible and responding");
            Assert.assertNotNull(authResponse.getToken(), "Authentication should work");
            
            logger.info("API health check passed - API is accessible and functional");
        } catch (Exception e) {
            Assert.fail("API health check failed - API may be down or unreachable: " + e.getMessage());
        }
    }
    
    @Test(groups = {"smoke", "api"}, priority = 2, dependsOnMethods = {"testApiHealthCheck"})
    @TestInfo(description = "Smoke test - Authentication flow", 
              author = "API Test Engineer", 
              priority = TestPriority.CRITICAL)
    public void testAuthenticationFlow() {
        // Test complete authentication flow
        
        // 1. Login
        String token = authApiClient.loginAndSetToken("testuser1", "password123");
        Assert.assertNotNull(token, "Login should return valid token");
        
        // 2. Validate token
        boolean isValid = authApiClient.validateToken();
        Assert.assertTrue(isValid, "Token should be valid after login");
        
        // 3. Get current user
        User currentUser = authApiClient.getCurrentUser();
        Assert.assertNotNull(currentUser, "Should be able to get current user with valid token");
        Assert.assertEquals(currentUser.getUsername(), "testuser1", "Current user should match logged in user");
        
        // 4. Logout
        authApiClient.logout();
        
        // 5. Verify token is invalid after logout
        boolean isValidAfterLogout = authApiClient.validateToken();
        Assert.assertFalse(isValidAfterLogout, "Token should be invalid after logout");
        
        logger.info("Authentication flow smoke test passed");
    }
    
    @Test(groups = {"smoke", "api"}, priority = 3, dependsOnMethods = {"testAuthenticationFlow"})
    @TestInfo(description = "Smoke test - User management operations", 
              author = "API Test Engineer", 
              priority = TestPriority.CRITICAL)
    public void testUserManagementOperations() {
        // Login as admin for user management
        authApiClient.loginAndSetToken("admin", "admin123");
        
        // 1. Get all users
        List<User> users = userApiClient.getAllUsers();
        Assert.assertNotNull(users, "Should be able to get users list");
        Assert.assertTrue(users.size() > 0, "Should have at least one user");
        
        int initialUserCount = users.size();
        
        // 2. Create a test user
        User testUser = new User("smoketest_" + System.currentTimeMillis(), 
                                "smoketest" + System.currentTimeMillis() + "@example.com",
                                "password123", "Smoke", "Test");
        
        User createdUser = userApiClient.createUser(testUser);
        Assert.assertNotNull(createdUser, "User should be created successfully");
        Assert.assertNotNull(createdUser.getId(), "Created user should have ID");
        
        // 3. Get user by ID
        User retrievedUser = userApiClient.getUserById(createdUser.getId());
        Assert.assertNotNull(retrievedUser, "Should be able to retrieve created user");
        Assert.assertEquals(retrievedUser.getUsername(), testUser.getUsername(), "Retrieved user should match created user");
        
        // 4. Update user
        retrievedUser.setFirstName("Updated");
        User updatedUser = userApiClient.updateUser(retrievedUser.getId(), retrievedUser);
        Assert.assertNotNull(updatedUser, "User should be updated successfully");
        Assert.assertEquals(updatedUser.getFirstName(), "Updated", "User should be updated");
        
        // 5. Delete user
        boolean deleted = userApiClient.deleteUser(createdUser.getId());
        Assert.assertTrue(deleted, "User should be deleted successfully");
        
        // 6. Verify user is deleted
        User deletedUser = userApiClient.getUserById(createdUser.getId());
        Assert.assertNull(deletedUser, "Deleted user should not be found");
        
        // 7. Verify user count is back to original
        List<User> finalUsers = userApiClient.getAllUsers();
        Assert.assertEquals(finalUsers.size(), initialUserCount, "User count should be back to original after deletion");
        
        logger.info("User management operations smoke test passed");
    }
    
    @Test(groups = {"smoke", "api"}, priority = 4)
    @TestInfo(description = "Smoke test - Error handling", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testErrorHandling() {
        // Test various error scenarios
        
        // 1. Invalid login
        try {
            authApiClient.login("invaliduser", "wrongpassword");
            Assert.fail("Invalid login should throw exception");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Login failed"), "Should get proper error message");
        }
        
        // 2. Unauthorized access (without login)
        try {
            userApiClient.getAllUsers();
            Assert.fail("Unauthorized access should throw exception");
        } catch (RuntimeException e) {
            // Expected - should fail without authentication
            logger.info("Unauthorized access correctly blocked");
        }
        
        // 3. Non-existent resource
        authApiClient.loginAndSetToken("admin", "admin123");
        User nonExistentUser = userApiClient.getUserById(99999L);
        Assert.assertNull(nonExistentUser, "Non-existent user should return null");
        
        logger.info("Error handling smoke test passed");
    }
    
    @Test(groups = {"smoke", "api", "performance"}, priority = 5)
    @TestInfo(description = "Smoke test - Basic performance check", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testBasicPerformance() {
        // Test basic performance requirements
        
        long startTime = System.currentTimeMillis();
        
        // Login
        authApiClient.loginAndSetToken("testuser1", "password123");
        
        long loginTime = System.currentTimeMillis();
        long loginDuration = loginTime - startTime;
        
        // Get users
        List<User> users = userApiClient.getAllUsers();
        
        long endTime = System.currentTimeMillis();
        long getUsersDuration = endTime - loginTime;
        long totalDuration = endTime - startTime;
        
        // Performance assertions
        Assert.assertTrue(loginDuration < 5000, 
            "Login should complete within 5 seconds, actual: " + loginDuration + "ms");
        
        Assert.assertTrue(getUsersDuration < 3000, 
            "Get users should complete within 3 seconds, actual: " + getUsersDuration + "ms");
        
        Assert.assertTrue(totalDuration < 8000, 
            "Total operations should complete within 8 seconds, actual: " + totalDuration + "ms");
        
        Assert.assertNotNull(users, "Users should be retrieved successfully");
        Assert.assertTrue(users.size() > 0, "Should retrieve at least one user");
        
        logger.info("Basic performance smoke test passed - Login: {}ms, GetUsers: {}ms, Total: {}ms", 
                   loginDuration, getUsersDuration, totalDuration);
    }
    
    @Test(groups = {"smoke", "api", "security"}, priority = 6)
    @TestInfo(description = "Smoke test - Basic security checks", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testBasicSecurity() {
        // Test basic security measures
        
        // 1. SQL Injection attempt
        try {
            authApiClient.login("admin'; DROP TABLE users; --", "password");
            Assert.fail("SQL injection attempt should be blocked");
        } catch (RuntimeException e) {
            // Expected - should be blocked
            logger.info("SQL injection attempt correctly blocked");
        }
        
        // 2. XSS attempt in user creation
        authApiClient.loginAndSetToken("admin", "admin123");
        
        User xssUser = new User("<script>alert('xss')</script>", 
                               "xss@example.com",
                               "password123", 
                               "<script>alert('xss')</script>", 
                               "Test");
        
        try {
            User createdUser = userApiClient.createUser(xssUser);
            // If user is created, verify XSS is sanitized
            if (createdUser != null) {
                Assert.assertFalse(createdUser.getUsername().contains("<script>"), 
                    "XSS should be sanitized in username");
                Assert.assertFalse(createdUser.getFirstName().contains("<script>"), 
                    "XSS should be sanitized in first name");
                
                // Cleanup
                userApiClient.deleteUser(createdUser.getId());
            }
        } catch (RuntimeException e) {
            // Also acceptable - XSS input rejected
            logger.info("XSS input correctly rejected");
        }
        
        logger.info("Basic security smoke test passed");
    }
}

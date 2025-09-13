package starlettech.tests.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.base.BaseApiTest;
import com.starlettech.enums.TestPriority;

import starlettech.api.clients.AuthApiClient;
import starlettech.api.clients.UserApiClient;
import starlettech.api.models.User;

/**
 * User API Tests
 */
@ApiTest
public class UserApiTests extends BaseApiTest {
    
    private UserApiClient userApiClient;
    private AuthApiClient authApiClient;
    private User testUser;
    
    @BeforeMethod
    public void setUp() {
        userApiClient = new UserApiClient();
        authApiClient = new AuthApiClient();
        
        // Login with admin user for user management operations
        authApiClient.loginAndSetToken("admin", "admin123");
        
        // Create test user for some tests
        testUser = new User("testuser_" + System.currentTimeMillis(), 
                           "testuser" + System.currentTimeMillis() + "@example.com",
                           "password123", "Test", "User");
    }
    
    @AfterMethod
    public void tearDown() {
        // Cleanup: delete test user if it was created
        if (testUser != null && testUser.getId() != null) {
            try {
                userApiClient.deleteUser(testUser.getId());
            } catch (Exception e) {
                logger.debug("Cleanup delete user failed: {}", e.getMessage());
            }
        }
        
        // Logout
        try {
            authApiClient.logout();
        } catch (Exception e) {
            logger.debug("Cleanup logout failed: {}", e.getMessage());
        }
    }
    
    @Test(groups = {"smoke", "api", "users"}, priority = 1)
    @TestInfo(description = "Test getting all users", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testGetAllUsers() {
        // Get all users
        List<User> users = userApiClient.getAllUsers();
        
        // Assertions
        Assert.assertNotNull(users, "Users list should not be null");
        Assert.assertTrue(users.size() > 0, "Should have at least one user");
        
        // Verify user structure
        User firstUser = users.get(0);
        Assert.assertNotNull(firstUser.getId(), "User ID should not be null");
        Assert.assertNotNull(firstUser.getUsername(), "Username should not be null");
        Assert.assertNotNull(firstUser.getEmail(), "Email should not be null");
        
        logger.info("Get all users test completed - Found {} users", users.size());
    }
    
    @Test(groups = {"smoke", "api", "users"}, priority = 2)
    @TestInfo(description = "Test getting user by ID", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testGetUserById() {
        // First get all users to get a valid ID
        List<User> users = userApiClient.getAllUsers();
        Assert.assertTrue(users.size() > 0, "Should have at least one user");
        
        Long userId = users.get(0).getId();
        
        // Get user by ID
        User user = userApiClient.getUserById(userId);
        
        // Assertions
        Assert.assertNotNull(user, "User should not be null");
        Assert.assertEquals(user.getId(), userId, "User ID should match");
        Assert.assertNotNull(user.getUsername(), "Username should not be null");
        Assert.assertNotNull(user.getEmail(), "Email should not be null");
        
        logger.info("Get user by ID test completed - User: {}", user.getUsername());
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 3)
    @TestInfo(description = "Test getting non-existent user", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testGetNonExistentUser() {
        // Try to get user with non-existent ID
        Long nonExistentId = 99999L;
        User user = userApiClient.getUserById(nonExistentId);
        
        // Assertion
        Assert.assertNull(user, "Non-existent user should return null");
        
        logger.info("Get non-existent user test completed successfully");
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 4)
    @TestInfo(description = "Test creating new user", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testCreateUser() {
        // Create new user
        User createdUser = userApiClient.createUser(testUser);
        
        // Assertions
        Assert.assertNotNull(createdUser, "Created user should not be null");
        Assert.assertNotNull(createdUser.getId(), "Created user should have ID");
        Assert.assertEquals(createdUser.getUsername(), testUser.getUsername(), "Username should match");
        Assert.assertEquals(createdUser.getEmail(), testUser.getEmail(), "Email should match");
        Assert.assertEquals(createdUser.getFirstName(), testUser.getFirstName(), "First name should match");
        Assert.assertEquals(createdUser.getLastName(), testUser.getLastName(), "Last name should match");
        Assert.assertNotNull(createdUser.getCreatedAt(), "Created date should not be null");
        
        // Update testUser with created user data for cleanup
        testUser = createdUser;
        
        logger.info("Create user test completed - Created user ID: {}", createdUser.getId());
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 5)
    @TestInfo(description = "Test updating user", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testUpdateUser() {
        // First create a user
        User createdUser = userApiClient.createUser(testUser);
        testUser = createdUser;
        
        // Update user data
        createdUser.setFirstName("Updated");
        createdUser.setLastName("Name");
        createdUser.setEmail("updated" + System.currentTimeMillis() + "@example.com");
        
        // Update user
        User updatedUser = userApiClient.updateUser(createdUser.getId(), createdUser);
        
        // Assertions
        Assert.assertNotNull(updatedUser, "Updated user should not be null");
        Assert.assertEquals(updatedUser.getId(), createdUser.getId(), "User ID should remain same");
        Assert.assertEquals(updatedUser.getFirstName(), "Updated", "First name should be updated");
        Assert.assertEquals(updatedUser.getLastName(), "Name", "Last name should be updated");
        Assert.assertEquals(updatedUser.getEmail(), createdUser.getEmail(), "Email should be updated");
        
        logger.info("Update user test completed - Updated user: {}", updatedUser.getUsername());
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 6)
    @TestInfo(description = "Test partially updating user", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testPatchUser() {
        // First create a user
        User createdUser = userApiClient.createUser(testUser);
        testUser = createdUser;
        
        // Prepare partial update
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "Patched");
        updates.put("lastName", "User");
        
        // Patch user
        User patchedUser = userApiClient.patchUser(createdUser.getId(), updates);
        
        // Assertions
        Assert.assertNotNull(patchedUser, "Patched user should not be null");
        Assert.assertEquals(patchedUser.getId(), createdUser.getId(), "User ID should remain same");
        Assert.assertEquals(patchedUser.getFirstName(), "Patched", "First name should be patched");
        Assert.assertEquals(patchedUser.getLastName(), "User", "Last name should be patched");
        Assert.assertEquals(patchedUser.getUsername(), createdUser.getUsername(), "Username should remain same");
        
        logger.info("Patch user test completed - Patched user: {}", patchedUser.getUsername());
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 7)
    @TestInfo(description = "Test deleting user", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testDeleteUser() {
        // First create a user
        User createdUser = userApiClient.createUser(testUser);
        
        // Delete user
        boolean deleted = userApiClient.deleteUser(createdUser.getId());
        
        // Assertions
        Assert.assertTrue(deleted, "User should be deleted successfully");
        
        // Verify user is deleted
        User deletedUser = userApiClient.getUserById(createdUser.getId());
        Assert.assertNull(deletedUser, "Deleted user should not be found");
        
        // Clear testUser to avoid cleanup issues
        testUser.setId(null);
        
        logger.info("Delete user test completed - Deleted user ID: {}", createdUser.getId());
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 8)
    @TestInfo(description = "Test searching users", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testSearchUsers() {
        // Search for users
        String searchQuery = "test";
        List<User> searchResults = userApiClient.searchUsers(searchQuery);
        
        // Assertions
        Assert.assertNotNull(searchResults, "Search results should not be null");
        
        // If results found, verify they contain the search term
        if (searchResults.size() > 0) {
            boolean foundMatch = searchResults.stream()
                .anyMatch(user -> user.getUsername().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchQuery.toLowerCase())));
            
            Assert.assertTrue(foundMatch, "At least one result should match the search query");
        }
        
        logger.info("Search users test completed - Found {} results for query: {}", searchResults.size(), searchQuery);
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 9)
    @TestInfo(description = "Test getting users with pagination", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testGetUsersWithPagination() {
        // Get paginated users
        int page = 0;
        int size = 5;
        JsonNode paginatedResponse = userApiClient.getUsersWithPagination(page, size);
        
        // Assertions
        Assert.assertNotNull(paginatedResponse, "Paginated response should not be null");
        
        // Check for common pagination fields
        if (paginatedResponse.has("content")) {
            JsonNode content = paginatedResponse.get("content");
            Assert.assertTrue(content.isArray(), "Content should be an array");
        } else if (paginatedResponse.has("data")) {
            JsonNode data = paginatedResponse.get("data");
            Assert.assertTrue(data.isArray(), "Data should be an array");
        }
        
        logger.info("Get users with pagination test completed");
    }
    
    @Test(groups = {"regression", "api", "users"}, priority = 10)
    @TestInfo(description = "Test checking username existence", 
              author = "API Test Engineer", 
              priority = TestPriority.LOW)
    public void testUsernameExists() {
        // Check existing username
        boolean exists = userApiClient.usernameExists("testuser1");
        Assert.assertTrue(exists, "Known username should exist");
        
        // Check non-existing username
        boolean notExists = userApiClient.usernameExists("nonexistentuser" + System.currentTimeMillis());
        Assert.assertFalse(notExists, "Non-existent username should not exist");
        
        logger.info("Username exists test completed successfully");
    }
    
    @Test(groups = {"performance", "api", "users"}, priority = 11)
    @TestInfo(description = "Test user creation performance", 
              author = "API Test Engineer", 
              priority = TestPriority.LOW)
    public void testUserCreationPerformance() {
        long startTime = System.currentTimeMillis();
        
        // Create user
        User createdUser = userApiClient.createUser(testUser);
        testUser = createdUser;
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Performance assertion (user creation should complete within 3 seconds)
        Assert.assertTrue(responseTime < 3000, 
            "User creation should complete within 3 seconds, actual: " + responseTime + "ms");
        
        logger.info("User creation performance test completed - Response time: {}ms", responseTime);
    }
}

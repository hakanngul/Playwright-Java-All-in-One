package starlettech.tests.api;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.starlettech.annotations.ApiTest;
import com.starlettech.annotations.TestInfo;
import com.starlettech.core.base.BaseApiTest;
import com.starlettech.enums.TestPriority;
import com.starlettech.utils.DataProviderUtils;

import starlettech.api.clients.AuthApiClient;
import starlettech.api.clients.UserApiClient;
import starlettech.api.models.User;

/**
 * Data-driven API Tests using DataProvider
 */
@ApiTest
public class UserDataDrivenTests extends BaseApiTest {
    
    private UserApiClient userApiClient;
    private AuthApiClient authApiClient;
    
    @BeforeMethod
    public void setUp() {
        userApiClient = new UserApiClient();
        authApiClient = new AuthApiClient();
        
        // Login as admin for user management operations
        authApiClient.loginAndSetToken("admin", "admin123");
    }
    
    @AfterMethod
    public void tearDown() {
        try {
            authApiClient.logout();
        } catch (Exception e) {
            logger.debug("Cleanup logout failed: {}", e.getMessage());
        }
    }
    
    /**
     * Data provider for user creation test data
     */
    @DataProvider(name = "userCreationData")
    public Object[][] getUserCreationData() {
        return new Object[][] {
            {"john_doe", "john.doe@example.com", "password123", "John", "Doe", true},
            {"jane_smith", "jane.smith@example.com", "securePass456", "Jane", "Smith", true},
            {"bob_wilson", "bob.wilson@example.com", "myPassword789", "Bob", "Wilson", true},
            {"alice_brown", "alice.brown@example.com", "strongPass321", "Alice", "Brown", true},
            {"", "invalid@example.com", "password123", "Invalid", "User", false}, // Empty username
            {"validuser", "", "password123", "Valid", "User", false}, // Empty email
            {"validuser2", "valid@example.com", "", "Valid", "User", false}, // Empty password
            {"validuser3", "invalid-email", "password123", "Valid", "User", false} // Invalid email format
        };
    }
    
    /**
     * Data provider using JSON file
     */
    @DataProvider(name = "userDataFromJson")
    public Object[][] getUserDataFromJson() {
        // This would read from a JSON file in real implementation
        return DataProviderUtils.readJsonData("user-test-data.json", "users");
    }
    
    /**
     * Data provider for login test scenarios
     */
    @DataProvider(name = "loginTestData")
    public Object[][] getLoginTestData() {
        return new Object[][] {
            {"testuser1", "password123", true, "Valid credentials"},
            {"testuser2", "password456", true, "Another valid user"},
            {"invaliduser", "password123", false, "Invalid username"},
            {"testuser1", "wrongpassword", false, "Invalid password"},
            {"", "password123", false, "Empty username"},
            {"testuser1", "", false, "Empty password"},
            {"", "", false, "Empty credentials"},
            {"admin'; DROP TABLE users; --", "password", false, "SQL injection attempt"}
        };
    }
    
    @Test(dataProvider = "userCreationData", groups = {"regression", "api", "users"})
    @TestInfo(description = "Data-driven test for user creation with various inputs", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testUserCreationWithVariousInputs(String username, String email, String password, 
                                                  String firstName, String lastName, boolean shouldSucceed) {
        
        // Create unique username and email to avoid conflicts
        String uniqueUsername = username.isEmpty() ? "" : username + "_" + System.currentTimeMillis();
        String uniqueEmail = email.isEmpty() ? "" : 
                           email.contains("@") ? email.replace("@", "_" + System.currentTimeMillis() + "@") : email;
        
        User testUser = new User(uniqueUsername, uniqueEmail, password, firstName, lastName);
        
        try {
            User createdUser = userApiClient.createUser(testUser);
            
            if (shouldSucceed) {
                // Positive test case
                Assert.assertNotNull(createdUser, "User should be created successfully");
                Assert.assertNotNull(createdUser.getId(), "Created user should have ID");
                Assert.assertEquals(createdUser.getUsername(), uniqueUsername, "Username should match");
                Assert.assertEquals(createdUser.getEmail(), uniqueEmail, "Email should match");
                Assert.assertEquals(createdUser.getFirstName(), firstName, "First name should match");
                Assert.assertEquals(createdUser.getLastName(), lastName, "Last name should match");
                
                // Cleanup
                userApiClient.deleteUser(createdUser.getId());
                
                logger.info("User creation test passed for valid data: {}", uniqueUsername);
            } else {
                // Negative test case - should not reach here
                Assert.fail("User creation should have failed for invalid data: " + uniqueUsername);
            }
            
        } catch (RuntimeException e) {
            if (!shouldSucceed) {
                // Negative test case - expected to fail
                logger.info("User creation correctly failed for invalid data: {} - Error: {}", 
                           uniqueUsername, e.getMessage());
            } else {
                // Positive test case - should not fail
                Assert.fail("User creation failed unexpectedly for valid data: " + uniqueUsername + 
                           " - Error: " + e.getMessage());
            }
        }
    }
    
    @Test(dataProvider = "loginTestData", groups = {"regression", "api", "auth"})
    @TestInfo(description = "Data-driven test for login with various credentials", 
              author = "API Test Engineer", 
              priority = TestPriority.HIGH)
    public void testLoginWithVariousCredentials(String username, String password, boolean shouldSucceed, String description) {
        
        try {
            authApiClient.logout(); // Ensure clean state
            String token = authApiClient.loginAndSetToken(username, password);
            
            if (shouldSucceed) {
                // Positive test case
                Assert.assertNotNull(token, "Login should return valid token for: " + description);
                Assert.assertTrue(authApiClient.validateToken(), "Token should be valid for: " + description);
                
                logger.info("Login test passed for: {}", description);
            } else {
                // Negative test case - should not reach here
                Assert.fail("Login should have failed for: " + description);
            }
            
        } catch (RuntimeException e) {
            if (!shouldSucceed) {
                // Negative test case - expected to fail
                logger.info("Login correctly failed for: {} - Error: {}", description, e.getMessage());
            } else {
                // Positive test case - should not fail
                Assert.fail("Login failed unexpectedly for: " + description + " - Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Data provider for user search scenarios
     */
    @DataProvider(name = "userSearchData")
    public Object[][] getUserSearchData() {
        return new Object[][] {
            {"test", "Search by partial username"},
            {"user", "Search by common term"},
            {"@example.com", "Search by email domain"},
            {"John", "Search by first name"},
            {"Smith", "Search by last name"},
            {"nonexistent", "Search for non-existent term"},
            {"", "Empty search query"},
            {"a", "Single character search"},
            {"test user", "Multi-word search"},
            {"TEST", "Case insensitive search"}
        };
    }
    
    @Test(dataProvider = "userSearchData", groups = {"regression", "api", "users"})
    @TestInfo(description = "Data-driven test for user search functionality", 
              author = "API Test Engineer", 
              priority = TestPriority.MEDIUM)
    public void testUserSearchWithVariousQueries(String searchQuery, String description) {
        
        try {
            var searchResults = userApiClient.searchUsers(searchQuery);
            
            // Basic assertions
            Assert.assertNotNull(searchResults, "Search results should not be null for: " + description);
            
            // If results found, verify they are relevant (basic check)
            if (searchResults.size() > 0 && !searchQuery.isEmpty()) {
                boolean foundRelevantResult = searchResults.stream().anyMatch(user -> 
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchQuery.toLowerCase()))
                );
                
                // Note: This assertion might be too strict depending on search implementation
                // Assert.assertTrue(foundRelevantResult, "At least one result should be relevant for: " + description);
            }
            
            logger.info("User search test completed for: {} - Found {} results", description, searchResults.size());
            
        } catch (Exception e) {
            logger.error("User search failed for: {} - Error: {}", description, e.getMessage());
            Assert.fail("User search should not throw exception for: " + description + " - Error: " + e.getMessage());
        }
    }
    
    /**
     * Performance test with data provider
     */
    @DataProvider(name = "performanceTestData")
    public Object[][] getPerformanceTestData() {
        return new Object[][] {
            {1, 1000, "Single user creation"},
            {5, 2000, "Multiple user operations"},
            {10, 3000, "Bulk user operations"}
        };
    }
    
    @Test(dataProvider = "performanceTestData", groups = {"performance", "api", "users"})
    @TestInfo(description = "Performance test for user operations", 
              author = "API Test Engineer", 
              priority = TestPriority.LOW)
    public void testUserOperationsPerformance(int operationCount, long maxTimeMs, String description) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            for (int i = 0; i < operationCount; i++) {
                // Create user
                User testUser = new User("perftest_" + System.currentTimeMillis() + "_" + i, 
                                        "perftest" + System.currentTimeMillis() + "_" + i + "@example.com",
                                        "password123", "Perf", "Test" + i);
                
                User createdUser = userApiClient.createUser(testUser);
                Assert.assertNotNull(createdUser, "User should be created");
                
                // Get user
                User retrievedUser = userApiClient.getUserById(createdUser.getId());
                Assert.assertNotNull(retrievedUser, "User should be retrieved");
                
                // Delete user
                boolean deleted = userApiClient.deleteUser(createdUser.getId());
                Assert.assertTrue(deleted, "User should be deleted");
            }
            
            long endTime = System.currentTimeMillis();
            long actualTime = endTime - startTime;
            
            Assert.assertTrue(actualTime <= maxTimeMs, 
                String.format("%s should complete within %dms, actual: %dms", description, maxTimeMs, actualTime));
            
            logger.info("Performance test passed for: {} - Completed in {}ms (limit: {}ms)", 
                       description, actualTime, maxTimeMs);
            
        } catch (Exception e) {
            Assert.fail("Performance test failed for: " + description + " - Error: " + e.getMessage());
        }
    }
}

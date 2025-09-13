package starlettech.api.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import com.starlettech.managers.ApiRequestManager;
import com.starlettech.utils.ApiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import starlettech.api.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User API Client
 */
public class UserApiClient {
    private static final Logger logger = LogManager.getLogger(UserApiClient.class);
    private static final String USERS_ENDPOINT = "/api/users";
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        logger.info("Getting all users");
        
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            JsonNode jsonResponse = ApiUtils.getResponseBodyAsJson(response);
            List<User> users = new java.util.ArrayList<>();

            assert jsonResponse != null;
            if (jsonResponse.isArray()) {
                for (JsonNode userNode : jsonResponse) {
                    User user = ApiUtils.convertToObject(userNode, User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
            
            logger.info("Retrieved {} users", users.size());
            return users;
        } else {
            logger.error("Failed to get users with status: {}", response.status());
            throw new RuntimeException("Failed to get users: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        logger.info("Getting user by ID: {}", userId);
        
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT + "/" + userId);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            User user = ApiUtils.getResponseBodyAsObject(response, User.class);
            logger.info("Retrieved user: {}", user.getUsername());
            return user;
        } else if (response.status() == 404) {
            logger.warn("User not found with ID: {}", userId);
            return null;
        } else {
            logger.error("Failed to get user with status: {}", response.status());
            throw new RuntimeException("Failed to get user: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Create new user
     */
    public User createUser(User user) {
        logger.info("Creating new user: {}", user.getUsername());
        
        APIResponse response = ApiRequestManager.authenticatedPost(USERS_ENDPOINT, user);
        
        if (response.status() == 201) {
            User createdUser = ApiUtils.getResponseBodyAsObject(response, User.class);
            logger.info("User created successfully with ID: {}", createdUser.getId());
            return createdUser;
        } else {
            logger.error("Failed to create user with status: {}", response.status());
            throw new RuntimeException("Failed to create user: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Update existing user
     */
    public User updateUser(Long userId, User user) {
        logger.info("Updating user with ID: {}", userId);
        
        APIResponse response = ApiRequestManager.authenticatedPut(USERS_ENDPOINT + "/" + userId, user);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            User updatedUser = ApiUtils.getResponseBodyAsObject(response, User.class);
            logger.info("User updated successfully: {}", updatedUser.getUsername());
            return updatedUser;
        } else if (response.status() == 404) {
            logger.warn("User not found for update with ID: {}", userId);
            return null;
        } else {
            logger.error("Failed to update user with status: {}", response.status());
            throw new RuntimeException("Failed to update user: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Partially update user
     */
    public User patchUser(Long userId, Map<String, Object> updates) {
        logger.info("Partially updating user with ID: {}", userId);
        
        APIResponse response = ApiRequestManager.authenticatedPatch(USERS_ENDPOINT + "/" + userId, updates);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            User updatedUser = ApiUtils.getResponseBodyAsObject(response, User.class);
            logger.info("User patched successfully: {}", updatedUser.getUsername());
            return updatedUser;
        } else if (response.status() == 404) {
            logger.warn("User not found for patch with ID: {}", userId);
            return null;
        } else {
            logger.error("Failed to patch user with status: {}", response.status());
            throw new RuntimeException("Failed to patch user: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        
        APIResponse response = ApiRequestManager.authenticatedDelete(USERS_ENDPOINT + "/" + userId);
        
        if (response.status() == 204 || response.status() == 200) {
            logger.info("User deleted successfully with ID: {}", userId);
            return true;
        } else if (response.status() == 404) {
            logger.warn("User not found for deletion with ID: {}", userId);
            return false;
        } else {
            logger.error("Failed to delete user with status: {}", response.status());
            throw new RuntimeException("Failed to delete user: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Search users by criteria
     */
    public List<User> searchUsers(String query) {
        logger.info("Searching users with query: {}", query);
        
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT + "/search?q=" + query);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            JsonNode jsonResponse = ApiUtils.getResponseBodyAsJson(response);
            List<User> users = new java.util.ArrayList<>();
            
            if (jsonResponse.isArray()) {
                for (JsonNode userNode : jsonResponse) {
                    User user = ApiUtils.convertToObject(userNode, User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
            
            logger.info("Found {} users matching query: {}", users.size(), query);
            return users;
        } else {
            logger.error("Failed to search users with status: {}", response.status());
            throw new RuntimeException("Failed to search users: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Get users with pagination
     */
    public JsonNode getUsersWithPagination(int page, int size) {
        logger.info("Getting users with pagination - page: {}, size: {}", page, size);
        
        String endpoint = USERS_ENDPOINT + "?page=" + page + "&size=" + size;
        APIResponse response = ApiRequestManager.authenticatedGet(endpoint);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            JsonNode paginatedResponse = ApiUtils.getResponseBodyAsJson(response);
            logger.info("Retrieved paginated users response");
            return paginatedResponse;
        } else {
            logger.error("Failed to get paginated users with status: {}", response.status());
            throw new RuntimeException("Failed to get paginated users: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        logger.info("Checking if username exists: {}", username);
        
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT + "/check-username?username=" + username);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            JsonNode jsonResponse = ApiUtils.getResponseBodyAsJson(response);
            boolean exists = jsonResponse.has("exists") && jsonResponse.get("exists").asBoolean();
            logger.info("Username '{}' exists: {}", username, exists);
            return exists;
        } else {
            logger.error("Failed to check username with status: {}", response.status());
            throw new RuntimeException("Failed to check username: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        logger.info("Checking if email exists: {}", email);
        
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT + "/check-email?email=" + email);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            JsonNode jsonResponse = ApiUtils.getResponseBodyAsJson(response);
            boolean exists = jsonResponse.has("exists") && jsonResponse.get("exists").asBoolean();
            logger.info("Email '{}' exists: {}", email, exists);
            return exists;
        } else {
            logger.error("Failed to check email with status: {}", response.status());
            throw new RuntimeException("Failed to check email: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Get user statistics
     */
    public JsonNode getUserStatistics() {
        logger.info("Getting user statistics");
        
        APIResponse response = ApiRequestManager.authenticatedGet(USERS_ENDPOINT + "/stats");
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            JsonNode stats = ApiUtils.getResponseBodyAsJson(response);
            logger.info("Retrieved user statistics");
            return stats;
        } else {
            logger.error("Failed to get user statistics with status: {}", response.status());
            throw new RuntimeException("Failed to get user statistics: " + ApiUtils.getResponseBody(response));
        }
    }
}

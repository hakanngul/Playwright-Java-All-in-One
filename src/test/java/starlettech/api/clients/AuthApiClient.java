package starlettech.api.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.APIResponse;
import com.starlettech.managers.ApiRequestManager;
import com.starlettech.utils.ApiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import starlettech.api.models.AuthResponse;
import starlettech.api.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication API Client
 */
public class AuthApiClient {
    private static final Logger logger = LogManager.getLogger(AuthApiClient.class);
    private static final String AUTH_ENDPOINT = "/api/auth";
    
    /**
     * Login with username and password
     */
    public AuthResponse login(String username, String password) {
        logger.info("Attempting login for user: {}", username);
        
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);
        
        APIResponse response = ApiUtils.post(AUTH_ENDPOINT + "/login", loginRequest);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            AuthResponse authResponse = ApiUtils.getResponseBodyAsObject(response, AuthResponse.class);
            logger.info("Login successful for user: {}", username);
            return authResponse;
        } else {
            logger.error("Login failed for user: {} with status: {}", username, response.status());
            throw new RuntimeException("Login failed: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Login and set token in ApiRequestManager
     */
    public String loginAndSetToken(String username, String password) {
        AuthResponse authResponse = login(username, password);
        String token = authResponse.getToken();
        ApiRequestManager.setAuthToken(token);
        logger.info("Authentication token set for user: {}", username);
        return token;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        logger.info("Logging out current user");
        
        try {
            APIResponse response = ApiRequestManager.authenticatedPost(AUTH_ENDPOINT + "/logout", null);
            
            if (ApiUtils.isSuccessStatusCode(response)) {
                logger.info("Logout successful");
            } else {
                logger.warn("Logout request failed with status: {}", response.status());
            }
        } catch (Exception e) {
            logger.warn("Logout request failed: {}", e.getMessage());
        } finally {
            ApiRequestManager.clearAuthToken();
        }
    }
    
    /**
     * Refresh authentication token
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Refreshing authentication token");
        
        Map<String, Object> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);
        
        APIResponse response = ApiUtils.post(AUTH_ENDPOINT + "/refresh", refreshRequest);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            AuthResponse authResponse = ApiUtils.getResponseBodyAsObject(response, AuthResponse.class);
            logger.info("Token refresh successful");
            return authResponse;
        } else {
            logger.error("Token refresh failed with status: {}", response.status());
            throw new RuntimeException("Token refresh failed: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Validate current token
     */
    public boolean validateToken() {
        logger.info("Validating current authentication token");
        
        try {
            APIResponse response = ApiRequestManager.authenticatedGet(AUTH_ENDPOINT + "/validate");
            boolean isValid = ApiUtils.isSuccessStatusCode(response);
            
            if (isValid) {
                logger.info("Token validation successful");
            } else {
                logger.warn("Token validation failed with status: {}", response.status());
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current user profile
     */
    public User getCurrentUser() {
        logger.info("Getting current user profile");
        
        APIResponse response = ApiRequestManager.authenticatedGet(AUTH_ENDPOINT + "/me");
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            User user = ApiUtils.getResponseBodyAsObject(response, User.class);
            logger.info("Retrieved user profile: {}", user.getUsername());
            return user;
        } else {
            logger.error("Failed to get user profile with status: {}", response.status());
            throw new RuntimeException("Failed to get user profile: " + ApiUtils.getResponseBody(response));
        }
    }
    
    /**
     * Change password
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        logger.info("Changing password for current user");
        
        Map<String, Object> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("currentPassword", currentPassword);
        changePasswordRequest.put("newPassword", newPassword);
        
        APIResponse response = ApiRequestManager.authenticatedPost(AUTH_ENDPOINT + "/change-password", changePasswordRequest);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            logger.info("Password change successful");
            return true;
        } else {
            logger.error("Password change failed with status: {}", response.status());
            return false;
        }
    }
    
    /**
     * Request password reset
     */
    public boolean requestPasswordReset(String email) {
        logger.info("Requesting password reset for email: {}", email);
        
        Map<String, Object> resetRequest = new HashMap<>();
        resetRequest.put("email", email);
        
        APIResponse response = ApiUtils.post(AUTH_ENDPOINT + "/forgot-password", resetRequest);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            logger.info("Password reset request sent successfully");
            return true;
        } else {
            logger.error("Password reset request failed with status: {}", response.status());
            return false;
        }
    }
    
    /**
     * Reset password with token
     */
    public boolean resetPassword(String resetToken, String newPassword) {
        logger.info("Resetting password with token");
        
        Map<String, Object> resetRequest = new HashMap<>();
        resetRequest.put("token", resetToken);
        resetRequest.put("newPassword", newPassword);
        
        APIResponse response = ApiUtils.post(AUTH_ENDPOINT + "/reset-password", resetRequest);
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            logger.info("Password reset successful");
            return true;
        } else {
            logger.error("Password reset failed with status: {}", response.status());
            return false;
        }
    }
    
    /**
     * Get authentication status
     */
    public JsonNode getAuthStatus() {
        logger.info("Getting authentication status");
        
        APIResponse response = ApiRequestManager.authenticatedGet(AUTH_ENDPOINT + "/status");
        
        if (ApiUtils.isSuccessStatusCode(response)) {
            return ApiUtils.getResponseBodyAsJson(response);
        } else {
            logger.error("Failed to get auth status with status: {}", response.status());
            throw new RuntimeException("Failed to get auth status: " + ApiUtils.getResponseBody(response));
        }
    }
}

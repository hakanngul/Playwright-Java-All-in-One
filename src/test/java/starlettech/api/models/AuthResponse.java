package starlettech.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Authentication response model
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("user")
    private User user;
    
    @JsonProperty("expiresIn")
    private Long expiresIn;
    
    @JsonProperty("tokenType")
    private String tokenType;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    // Default constructor
    public AuthResponse() {}
    
    // Constructor
    public AuthResponse(String token, User user, Long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", user=" + user +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}

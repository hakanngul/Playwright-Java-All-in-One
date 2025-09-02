package starlettech.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Album model for JSONPlaceholder API
 * Represents a photo album with user association
 * 
 * @author API Test Engineer
 * @version 1.0
 * @since 2024-01-01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Album {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("userId")
    private Integer userId;
    
    @JsonProperty("title")
    private String title;
    
    /**
     * Default constructor
     */
    public Album() {}
    
    /**
     * Constructor for creating new album
     */
    public Album(Integer userId, String title) {
        this.userId = userId;
        this.title = title;
    }
    
    /**
     * Full constructor
     */
    public Album(Integer id, Integer userId, String title) {
        this.id = id;
        this.userId = userId;
        this.title = title;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Album album = (Album) o;
        
        if (id != null ? !id.equals(album.id) : album.id != null) return false;
        if (userId != null ? !userId.equals(album.userId) : album.userId != null) return false;
        return title != null ? title.equals(album.title) : album.title == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}

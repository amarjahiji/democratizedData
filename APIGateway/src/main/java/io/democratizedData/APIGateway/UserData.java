package io.democratizedData.APIGateway;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class UserData {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("role")
    private String role;

    @JsonProperty("city")
    private String city;

    @JsonProperty("gender")
    private String gender;

    public UserData() {}

    public UserData(Map<String, Object> userMap) {
        this.userId = userMap.get("userId") != null ?
                Long.valueOf(userMap.get("userId").toString()) : null;
        this.username = (String) userMap.get("username");
        this.role = (String) userMap.get("role");
        this.city = (String) userMap.get("city");
        this.gender = (String) userMap.get("gender");
    }

    public UserData(Long userId, String username, String role, String city, String gender) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.city = city;
        this.gender = gender;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", city='" + city + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
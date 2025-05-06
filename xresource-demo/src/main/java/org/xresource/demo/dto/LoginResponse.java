package org.xresource.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("is_admin")
    private boolean isAdmin;

    // Constructors
    public LoginResponse(String token, String userId, boolean isAdmin) {
        this.token = token;
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

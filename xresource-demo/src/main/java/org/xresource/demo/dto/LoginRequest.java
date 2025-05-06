package org.xresource.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @Valid
    @NotNull(message = "user_id is required and must not be blank")
    private String userId;

    @Valid
    @NotNull(message = "password is required and must not be blank")
    private String password;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

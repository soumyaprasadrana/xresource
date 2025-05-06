package org.xresource.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogoutRequest {

    @JsonProperty("session_id")
    private String sessionId;

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

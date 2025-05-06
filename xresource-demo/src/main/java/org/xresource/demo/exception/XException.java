package org.xresource.demo.exception;

import org.springframework.http.HttpStatus;

import java.util.*;

public class XException extends RuntimeException {

    private static final Map<String, Map<String, String>> MESSAGES = new HashMap<>();

    static {
        Map<String, String> authMessages = new HashMap<>();
        authMessages.put("invalid_cred", "Invalid username or password.");
        authMessages.put("user_locked", "User account is locked.");
        MESSAGES.put("auth", authMessages);

        // Add more groups as needed
        Map<String, String> userMessages = new HashMap<>();
        userMessages.put("not_found", "User not found.");
        MESSAGES.put("user", userMessages);
    }

    private final String groupId;
    private final String exceptionId;
    private final HttpStatus status;

    public XException(String groupId, String exceptionId) {
        this(groupId, exceptionId, HttpStatus.BAD_REQUEST);
    }

    public XException(String groupId, String exceptionId, HttpStatus status) {
        super(resolveMessage(groupId, exceptionId));
        this.groupId = groupId;
        this.exceptionId = exceptionId;
        this.status = status;
    }

    private static String resolveMessage(String groupId, String exceptionId) {
        return Optional.ofNullable(MESSAGES.get(groupId))
                .map(map -> map.getOrDefault(exceptionId, "Unknown error"))
                .orElse("Unknown error group");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getExceptionId() {
        return exceptionId;
    }
}

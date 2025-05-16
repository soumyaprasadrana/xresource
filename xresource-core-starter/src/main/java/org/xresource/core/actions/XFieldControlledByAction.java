package org.xresource.core.actions;

import java.util.List;

import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface XFieldControlledByAction {
    ResponseEntity<?> action(HttpServletRequest request, HttpServletResponse response, String resourceName,
            String fieldName,
            Object resourceEntity, Object inputEntity, List<String> roles, String actionSucessMessage);
}

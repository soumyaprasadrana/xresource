package org.xresource.internal.actions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.xresource.core.actions.XFieldControlledByAction;
import org.xresource.core.service.XResourceService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class XFieldControlledByActionAbsractImpl implements XFieldControlledByAction {

    @Autowired
    private final XResourceService service;

    @Override
    public ResponseEntity<?> action(HttpServletRequest request, HttpServletResponse response, String resourceName,
            String fieldName,
            Object resourceEntity, Object inputEntity, List<String> roles, String actionSucessMessage) {

        Object updatedEntity = service.update(resourceName, resourceEntity, inputEntity, roles);

        return ResponseEntity.ok(Map.of(
                "message",
                actionSucessMessage,
                "status", true));
    }

}

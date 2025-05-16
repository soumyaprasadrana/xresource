package org.xresource.core.actions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.xresource.core.model.XFieldMetadata;
import org.xresource.core.model.XResourceMetadata;
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

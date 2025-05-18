package org.xresource.internal.exception;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xresource.core.exception.XValidationException;
import org.xresource.internal.context.XResourceRequestContext;
import org.xresource.internal.context.XResourceRequestContextHolder;

import io.swagger.v3.oas.annotations.Hidden;
import static org.xresource.internal.config.XResourceConfigProperties.API_RESPONSE_ERROR_CONTEXT;

@RestControllerAdvice
@Hidden
public class XResourceExceptionHandler {

    @Value(API_RESPONSE_ERROR_CONTEXT)
    private boolean includeContext;

    private Map<String, Object> enrichWithContext(Map<String, Object> originalBody, Throwable exception) {
        if (!includeContext)
            return originalBody;

        XResourceRequestContext reqContext = XResourceRequestContextHolder.get();
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("httpMethod", reqContext.getHttpMethod());
        context.put("fullUrl", reqContext.getFullUrl());
        if (reqContext.getResourceName() != null)
            context.put("resourceName", reqContext.getResourceName());
        if (reqContext.getId() != null)
            context.put("id", reqContext.getId());
        if (reqContext.getField() != null)
            context.put("field", reqContext.getField());
        if (reqContext.getQueryName() != null)
            context.put("queryName", reqContext.getQueryName());
        if (reqContext.getActionName() != null)
            context.put("actionName", reqContext.getActionName());
        if (reqContext.getOperationType() != null)
            context.put("operationType", reqContext.getOperationType());

        if (exception != null) {
            String trace = Arrays.stream(exception.getStackTrace())
                    .limit(5)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
            context.put("stackTrace", trace);
        }

        Map<String, Object> enriched = new LinkedHashMap<>(originalBody);
        enriched.put("_xrequest", context);
        return enriched;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(enrichWithContext(Map.of("error", ex.getMessage()), ex));
    }

    @ExceptionHandler(XValidationException.class)
    public ResponseEntity<?> handleValidationError(XValidationException ex) {
        List<Map<String, Object>> violations = ex.getViolations().stream()
                .map(v -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("field", v.getField());
                    map.put("message", v.getMessage());
                    if (v.getValidatorType() != null)
                        map.put("validatorType", v.getValidatorType());
                    if (v.getRejectedValue() != null)
                        map.put("rejectedValue", v.getRejectedValue());
                    if (v.getException() != null && includeContext) {
                        String trace = Arrays.stream(v.getException().getStackTrace())
                                .limit(5)
                                .map(StackTraceElement::toString)
                                .collect(Collectors.joining("\n"));
                        map.put("exception", trace);
                    }
                    return map;
                })
                .toList();

        Map<String, Object> body = Map.of(
                "message", ex.getMessage(),
                "error", "Validation Failed",
                "violations", violations);

        return ResponseEntity.status(400).body(enrichWithContext(body, ex));
    }

    @ExceptionHandler(XAccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(XAccessDeniedException ex) {
        return ResponseEntity.status(403).body(enrichWithContext(Map.of("error", ex.getMessage()), ex));
    }

    @ExceptionHandler(XResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleAlreadyExistException(XResourceAlreadyExistsException ex) {
        return ResponseEntity.status(400).body(enrichWithContext(Map.of("error", ex.getMessage()), ex));
    }

    @ExceptionHandler(XResourceException.class)
    public ResponseEntity<?> handleXResourceException(XResourceException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "XResource Error");
        error.put("message", ex.getCustomMessage());
        if (includeContext && ex.getOriginalMessage() != null) {
            error.put("cause", ex.getOriginalMessage());
        }
        return ResponseEntity.status(500).body(enrichWithContext(error, ex.getCause() != null ? ex.getCause() : ex));
    }

}

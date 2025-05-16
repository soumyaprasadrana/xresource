package org.xresource.core.exception;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;

@RestControllerAdvice
@Hidden
public class XResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(XValidationException.class)
    public ResponseEntity<?> handleValidationError(XValidationException ex) {
        List<Map<String, Object>> simplifiedViolations = ex.getViolations().stream()
                .map(v -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("field", v.getField());
                    map.put("message", v.getMessage());
                    if (v.getValidatorType() != null) {
                        map.put("validatorType", v.getValidatorType());
                    }
                    if (v.getRejectedValue() != null) {
                        map.put("rejectedValue", v.getRejectedValue());
                    }
                    if (v.getException() != null) {
                        String trace = Arrays.stream(v.getException().getStackTrace())
                                .limit(5)
                                .map(StackTraceElement::toString)
                                .collect(Collectors.joining("\n"));
                        map.put("exception", trace);
                    }
                    return map;
                })
                .toList();

        return ResponseEntity.status(400).body(
                Map.of(
                        "message", ex.getMessage(),
                        "error", "Validation Failed",
                        "violations", simplifiedViolations));
    }

    @ExceptionHandler(XAccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(XAccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(XResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleAlreadyExistException(XResourceAlreadyExistsException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Unexpected error", "details", ex.getMessage()));
    }
}

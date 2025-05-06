package org.xresource.core.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class XResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(XValidationException.class)
    public ResponseEntity<?> handleValidationError(XValidationException ex) {
        return ResponseEntity.status(400).body(Map.of("message", ex.getMessage(),"error","Validation Failed"));
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

package org.xresource.core.actions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class XActionAbstractImpl implements XResourceAction{
    @Override
    public ResponseEntity<?> handleGet(Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        return methodNotAllowed("GET");
    }

    @Override
    public ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        return methodNotAllowed("POST");
    }

    @Override
    public ResponseEntity<?> handlePut(Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        return methodNotAllowed("PUT");
    }

    @Override
    public ResponseEntity<?> handleDelete(Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        return methodNotAllowed("DELETE");
    }

    protected ResponseEntity<?> methodNotAllowed(String method) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Method " + method + " not supported for this action.");
    }
}

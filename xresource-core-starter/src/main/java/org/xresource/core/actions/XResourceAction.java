package org.xresource.core.actions;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface XResourceAction {
    
     // Handle GET request
     ResponseEntity<?> handleGet(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);

     // Handle POST request
     ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);
     
     // Handle PUT request
     ResponseEntity<?> handlePut(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);
     
     // Handle DELETE request
     ResponseEntity<?> handleDelete(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);
 
}

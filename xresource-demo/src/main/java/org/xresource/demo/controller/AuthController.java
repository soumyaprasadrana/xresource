package org.xresource.demo.controller;

import org.xresource.demo.dto.LoginRequest;
import org.xresource.demo.dto.LoginResponse;
import org.xresource.demo.dto.LogoutRequest;
import org.xresource.demo.security.CustomUserDetails;
import org.xresource.demo.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/login", consumes = { "application/json", "application/json;charset=UTF-8" })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/whoami")
    public ResponseEntity<?> whoAmI() {
        CustomUserDetails currentUser = authService.getCurrentUserDetails();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
        }

        String jwt = authHeader.substring(7);
        boolean valid = authService.isSessionValid(jwt);

        if (valid) {
            return ResponseEntity.ok("Token/session is valid");
        } else {
            return ResponseEntity.status(401).body("Session expired or invalid");
        }
    }
}

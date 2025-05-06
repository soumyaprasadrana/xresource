package org.xresource.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("ReleaseX API is running ✅");
    }

    @RequestMapping("*")
    public ResponseEntity<Object> fallback() {
        return ResponseEntity
                .status(404)
                .body("{\"error\": \"Resource not found\"}");
    }
}

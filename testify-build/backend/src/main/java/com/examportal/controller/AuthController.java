package com.examportal.controller;

import com.examportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String userId   = body.get("userId");
        String password = body.get("password");
        return ResponseEntity.ok(userService.login(userId, password));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String userId   = body.get("userId");
        String password = body.get("password");
        String name     = body.get("name");
        String email    = body.getOrDefault("email", "");
        return ResponseEntity.ok(userService.register(userId, password, name, email));
    }
}

package com.examportal.controller;

import com.examportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.updateProfile(userId, body.get("name")));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.changePassword(
                userId, body.get("oldPassword"), body.get("newPassword")));
    }
}

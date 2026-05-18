package com.examportal.service;

import com.examportal.model.User;
import com.examportal.util.CsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private CsvUtil csvUtil;

    // ─── AUTH ────────────────────────────────────────────────────────────────

    public Map<String, Object> login(String userId, String password) {
        List<User> users = csvUtil.loadUsers();
        Optional<User> match = users.stream()
                .filter(u -> u.getUserId().equals(userId) && u.checkPassword(password))
                .findFirst();

        Map<String, Object> response = new HashMap<>();
        if (match.isPresent()) {
            User u = match.get();
            response.put("success", true);
            response.put("userId", u.getUserId());
            response.put("name", u.getName());
            response.put("role", u.getRole());
            response.put("examAttempted", u.isExamAttempted());
            response.put("email", u.getEmail());
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials.");
        }
        return response;
    }

    public Map<String, Object> register(String userId, String password, String name, String email) {
        List<User> users = csvUtil.loadUsers();
        Map<String, Object> response = new HashMap<>();

        // Check duplicate userId
        boolean idExists = users.stream().anyMatch(u -> u.getUserId().equals(userId));
        if (idExists) {
            response.put("success", false);
            response.put("message", "User ID already taken. Please choose another.");
            return response;
        }

        // Check duplicate email (if provided)
        if (email != null && !email.isBlank()) {
            boolean emailExists = users.stream()
                    .anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
            if (emailExists) {
                response.put("success", false);
                response.put("message", "An account with this email already exists. Please log in.");
                return response;
            }
        }

        User newUser = new User(userId, password, name, "USER", false, email != null ? email : "");
        users.add(newUser);
        csvUtil.saveUsers(users);

        response.put("success", true);
        response.put("message", "Registration successful! You can now log in.");
        return response;
    }

    // Overload for backward compat
    public Map<String, Object> register(String userId, String password, String name) {
        return register(userId, password, name, "");
    }

    // ─── PROFILE ─────────────────────────────────────────────────────────────

    public Map<String, Object> getProfile(String userId) {
        List<User> users = csvUtil.loadUsers();
        Map<String, Object> response = new HashMap<>();

        users.stream().filter(u -> u.getUserId().equals(userId)).findFirst().ifPresentOrElse(u -> {
            response.put("success", true);
            response.put("userId", u.getUserId());
            response.put("name", u.getName());
            response.put("role", u.getRole());
            response.put("examAttempted", u.isExamAttempted());
            response.put("email", u.getEmail());
        }, () -> {
            response.put("success", false);
            response.put("message", "User not found.");
        });

        return response;
    }

    public Map<String, Object> updateProfile(String userId, String newName) {
        List<User> users = csvUtil.loadUsers();
        Map<String, Object> response = new HashMap<>();

        boolean updated = false;
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                u.setName(newName);
                updated = true;
                break;
            }
        }

        if (updated) {
            csvUtil.saveUsers(users);
            response.put("success", true);
            response.put("message", "Profile updated successfully.");
            response.put("name", newName);
        } else {
            response.put("success", false);
            response.put("message", "User not found.");
        }
        return response;
    }

    public Map<String, Object> changePassword(String userId, String oldPassword, String newPassword) {
        List<User> users = csvUtil.loadUsers();
        Map<String, Object> response = new HashMap<>();

        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                if (!u.checkPassword(oldPassword)) {
                    response.put("success", false);
                    response.put("message", "Current password is incorrect.");
                    return response;
                }
                u.setPassword(newPassword);
                csvUtil.saveUsers(users);
                response.put("success", true);
                response.put("message", "Password changed successfully.");
                return response;
            }
        }

        response.put("success", false);
        response.put("message", "User not found.");
        return response;
    }

    public void markExamAttempted(String userId) {
        List<User> users = csvUtil.loadUsers();
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                u.setExamAttempted(true);
                break;
            }
        }
        csvUtil.saveUsers(users);
    }

    public String getUserEmail(String userId) {
        return csvUtil.loadUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .map(u -> u.getEmail())
                .findFirst()
                .orElse("");
    }

    // ─── ADMIN ───────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAllUsers() {
        List<User> users = csvUtil.loadUsers();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> row = new HashMap<>();
            row.put("userId", u.getUserId());
            row.put("name", u.getName());
            row.put("role", u.getRole());
            row.put("examAttempted", u.isExamAttempted());
            row.put("email", u.getEmail());
            result.add(row);
        }
        return result;
    }

    public Map<String, Object> resetExamAttempt(String userId) {
        List<User> users = csvUtil.loadUsers();
        Map<String, Object> response = new HashMap<>();
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                u.setExamAttempted(false);
                csvUtil.saveUsers(users);
                response.put("success", true);
                response.put("message", "Exam attempt reset for " + userId);
                return response;
            }
        }
        response.put("success", false);
        response.put("message", "User not found.");
        return response;
    }

    public String getUserName(String userId) {
        return csvUtil.loadUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .map(u -> u.getName())
                .findFirst()
                .orElse(userId);
    }
}

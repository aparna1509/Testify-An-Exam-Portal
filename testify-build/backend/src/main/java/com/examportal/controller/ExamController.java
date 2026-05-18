package com.examportal.controller;

import com.examportal.model.*;
import com.examportal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    // ─── EXAM FLOW ───────────────────────────────────────────────────────────

    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> getQuestions(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(examService.getExamQuestions(category));
    }

    @PostMapping("/submit")
    public ResponseEntity<ExamResult> submit(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");

        @SuppressWarnings("unchecked")
        Map<String, Object> rawAnswers = (Map<String, Object>) body.get("answers");

        Map<String, Integer> answers = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawAnswers.entrySet()) {
            answers.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
        }

        ExamResult result = examService.submitExam(userId, answers);
        return ResponseEntity.ok(result);
    }

    // ─── ADMIN — CONFIG ──────────────────────────────────────────────────────

    @GetMapping("/config")
    public ResponseEntity<ExamConfig> getConfig() {
        return ResponseEntity.ok(examService.getConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<ExamConfig> updateConfig(@RequestBody Map<String, Integer> body) {
        int qCount    = body.getOrDefault("questionCount",  10);
        int duration  = body.getOrDefault("durationSeconds", 60);
        int threshold = body.getOrDefault("passThreshold",   60);
        return ResponseEntity.ok(examService.updateConfig(qCount, duration, threshold));
    }

    // ─── ADMIN — QUESTIONS ───────────────────────────────────────────────────

    @GetMapping("/admin/questions")
    public ResponseEntity<List<Question>> getAllQuestions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty) {
        List<Question> questions = examService.getAllQuestions();
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
            questions = questions.stream()
                    .filter(q -> q.getCategory().equalsIgnoreCase(category))
                    .collect(java.util.stream.Collectors.toList());
        }
        if (difficulty != null && !difficulty.isBlank() && !difficulty.equalsIgnoreCase("all")) {
            questions = questions.stream()
                    .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                    .collect(java.util.stream.Collectors.toList());
        }
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/admin/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(examService.getCategories());
    }

    @PostMapping("/admin/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(examService.addQuestion(question));
    }

    @DeleteMapping("/admin/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable String id) {
        return ResponseEntity.ok(examService.deleteQuestion(id));
    }

    @PostMapping("/admin/questions/upload")
    public ResponseEntity<Map<String, Object>> uploadQuestions(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(examService.uploadQuestions(file.getInputStream()));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─── ADMIN — USERS ───────────────────────────────────────────────────────

    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/admin/users/{userId}/reset")
    public ResponseEntity<Map<String, Object>> resetAttempt(@PathVariable String userId) {
        return ResponseEntity.ok(userService.resetExamAttempt(userId));
    }

    // ─── ADMIN — EXPORT CSV ──────────────────────────────────────────────────

    @GetMapping("/admin/export/results")
    public ResponseEntity<byte[]> exportResults() {
        String csv = examService.exportResultsCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "testify-results.csv");
        return ResponseEntity.ok().headers(headers).body(csv.getBytes());
    }

    // ─── ADMIN — DASHBOARD STATS ─────────────────────────────────────────────

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(examService.getDashboardStats());
    }

    // ─── LEADERBOARD ─────────────────────────────────────────────────────────

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.examportal.util.LeaderboardStore.Entry e : examService.getLeaderboard()) {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("userId", e.userId);
            row.put("name",   e.name);
            row.put("score",  e.score);
            row.put("total",  e.total);
            result.add(row);
        }
        return ResponseEntity.ok(result);
    }
}

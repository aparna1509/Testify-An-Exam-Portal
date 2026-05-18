package com.examportal.service;

import com.examportal.model.*;
import com.examportal.util.CsvUtil;
import com.examportal.util.LeaderboardStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    @Autowired private CsvUtil csvUtil;
    @Autowired private UserService userService;
    @Autowired private LeaderboardStore leaderboardStore;
    @Autowired(required = false) private EmailService emailService;

    private ExamConfig config = new ExamConfig(10, 60, 60);

    // ─── CONFIG ──────────────────────────────────────────────────────────────

    public ExamConfig getConfig() { return config; }

    public ExamConfig updateConfig(int questionCount, int durationSeconds, int passThreshold) {
        config.setQuestionCount(questionCount);
        config.setDurationSeconds(durationSeconds);
        config.setPassThreshold(Math.max(0, Math.min(100, passThreshold)));
        return config;
    }

    // ─── QUESTIONS ───────────────────────────────────────────────────────────

    public Map<String, Object> getExamQuestions() {
        return getExamQuestions(null);
    }

    public Map<String, Object> getExamQuestions(String category) {
        List<Question> allQuestions = csvUtil.loadQuestions();
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
            allQuestions = allQuestions.stream()
                    .filter(q -> q.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        if (allQuestions.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "No questions available. Please ask admin to add questions.");
            return error;
        }
        Collections.shuffle(allQuestions);
        int count = Math.min(config.getQuestionCount(), allQuestions.size());
        List<Question.QuestionDTO> selected = allQuestions.stream()
                .limit(count).map(Question::toDTO).collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("questions", selected);
        response.put("durationSeconds", config.getDurationSeconds());
        response.put("total", selected.size());
        response.put("passThreshold", config.getPassThreshold());
        return response;
    }

    public List<Question> getAllQuestions() { return csvUtil.loadQuestions(); }

    public List<String> getCategories() {
        return csvUtil.loadQuestions().stream()
                .map(Question::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, Object> addQuestion(Question question) {
        List<Question> questions = csvUtil.loadQuestions();
        question.setId(String.valueOf(questions.size() + 1));
        if (question.getCategory() == null || question.getCategory().isBlank())
            question.setCategory("General");
        if (question.getDifficulty() == null || question.getDifficulty().isBlank())
            question.setDifficulty("Medium");
        questions.add(question);
        csvUtil.saveQuestions(questions);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Question added successfully.");
        response.put("total", questions.size());
        return response;
    }

    public Map<String, Object> deleteQuestion(String questionId) {
        List<Question> questions = csvUtil.loadQuestions();
        boolean removed = questions.removeIf(q -> q.getId().equals(questionId));
        for (int i = 0; i < questions.size(); i++) questions.get(i).setId(String.valueOf(i + 1));
        csvUtil.saveQuestions(questions);
        Map<String, Object> response = new HashMap<>();
        response.put("success", removed);
        response.put("message", removed ? "Question deleted." : "Question not found.");
        response.put("total", questions.size());
        return response;
    }

    public Map<String, Object> uploadQuestions(InputStream stream) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Question> parsed = csvUtil.parseQuestionsFromStream(stream);
            if (parsed.isEmpty()) { response.put("success", false); response.put("message", "No valid questions found in the CSV."); return response; }
            csvUtil.saveQuestions(parsed);
            response.put("success", true);
            response.put("message", parsed.size() + " questions loaded from CSV.");
            response.put("total", parsed.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to parse CSV: " + e.getMessage());
        }
        return response;
    }

    // ─── SUBMISSION ──────────────────────────────────────────────────────────

    public ExamResult submitExam(String userId, Map<String, Integer> answers) {
        List<Question> allQuestions = csvUtil.loadQuestions();
        Map<String, Question> questionMap = new HashMap<>();
        for (Question q : allQuestions) questionMap.put(q.getId(), q);

        List<ExamResult.QuestionResult> results = new ArrayList<>();
        int score = 0;

        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            String qId = entry.getKey();
            int userAnswer = entry.getValue();
            Question q = questionMap.get(qId);
            if (q != null) {
                boolean correct = q.isCorrect(userAnswer);
                if (correct) score++;
                results.add(new ExamResult.QuestionResult(
                        qId, q.getQuestionText(), q.getOptions(), userAnswer,
                        q.getCorrectOption(), q.getCategory(), q.getDifficulty()));
            }
        }

        results.sort(Comparator.comparingInt(r -> Integer.parseInt(r.getQuestionId())));
        userService.markExamAttempted(userId);

        String userName = userService.getUserName(userId);
        leaderboardStore.record(userId, userName, score, answers.size());

        int passThreshold = config.getPassThreshold();
        ExamResult examResult = new ExamResult(userId, score, answers.size(), results, passThreshold);

        // Send email result summary if user has an email and email is configured
        if (emailService != null) {
            String userEmail = userService.getUserEmail(userId);
            if (userEmail != null && !userEmail.isBlank()) {
                new Thread(() -> emailService.sendResultEmail(userEmail, userName, examResult)).start();
            }
        }

        return examResult;
    }

    // ─── LEADERBOARD ─────────────────────────────────────────────────────────

    public List<LeaderboardStore.Entry> getLeaderboard() {
        return leaderboardStore.getLeaderboard();
    }

    // ─── DASHBOARD STATS ─────────────────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        List<LeaderboardStore.Entry> entries = leaderboardStore.getAllEntries();
        Map<String, Object> stats = new LinkedHashMap<>();

        int total = entries.size();
        stats.put("totalAttempts", total);

        if (total == 0) {
            stats.put("averageScore", 0);
            stats.put("passRate", 0);
            stats.put("categoryBreakdown", new HashMap<>());
            stats.put("difficultyBreakdown", new HashMap<>());
            return stats;
        }

        double avgPct = entries.stream()
                .mapToDouble(e -> e.total > 0 ? (double) e.score * 100 / e.total : 0)
                .average().orElse(0);
        stats.put("averageScore", Math.round(avgPct * 10) / 10.0);

        long passed = entries.stream()
                .filter(e -> e.total > 0 && (double) e.score * 100 / e.total >= config.getPassThreshold())
                .count();
        stats.put("passRate", Math.round((double) passed * 100 / total));
        stats.put("passThreshold", config.getPassThreshold());

        // Question-wise performance across all questions
        List<Question> questions = csvUtil.loadQuestions();
        Map<String, long[]> categoryStats = new LinkedHashMap<>();
        Map<String, long[]> difficultyStats = new LinkedHashMap<>();
        for (Question q : questions) {
            categoryStats.computeIfAbsent(q.getCategory(), k -> new long[]{0, 0});
            difficultyStats.computeIfAbsent(q.getDifficulty(), k -> new long[]{0, 0});
        }
        stats.put("categoryBreakdown", categoryStats);
        stats.put("difficultyBreakdown", difficultyStats);
        stats.put("topStudents", leaderboardStore.getLeaderboard().stream().limit(5)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.name);
                    m.put("score", e.score);
                    m.put("total", e.total);
                    m.put("pct", e.total > 0 ? Math.round((double) e.score * 100 / e.total) : 0);
                    return m;
                }).collect(Collectors.toList()));

        return stats;
    }

    // ─── CSV EXPORT ──────────────────────────────────────────────────────────

    public String exportResultsCsv() {
        List<LeaderboardStore.Entry> entries = leaderboardStore.getLeaderboard();
        StringBuilder sb = new StringBuilder();
        sb.append("Rank,Name,User ID,Score,Total,Percentage,Status\n");
        int rank = 1;
        for (LeaderboardStore.Entry e : entries) {
            int pct = e.total > 0 ? (int) Math.round((double) e.score * 100 / e.total) : 0;
            String status = pct >= config.getPassThreshold() ? "Pass" : "Fail";
            sb.append(rank++).append(",")
              .append(escapeCsv(e.name)).append(",")
              .append(escapeCsv(e.userId)).append(",")
              .append(e.score).append(",")
              .append(e.total).append(",")
              .append(pct).append("%,")
              .append(status).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n"))
            return "\"" + val.replace("\"", "\"\"") + "\"";
        return val;
    }
}

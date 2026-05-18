package com.examportal.util;

import com.examportal.model.Question;
import com.examportal.model.User;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class CsvUtil {

    // ─── Resolve the writable data directory ────────────────────────────────
    private Path getDataDir() throws IOException {
        Path dir = Paths.get("data");
        Files.createDirectories(dir);
        return dir;
    }

    private Path getUsersFile() throws IOException {
        Path file = getDataDir().resolve("users.csv");
        if (!Files.exists(file)) seedFile(file, "userId,password,name,role,examAttempted,email\n" +
                "admin,admin123,Administrator,ADMIN,false,\n" +
                "user123,pass123,Test User,USER,false,\n");
        return file;
    }

    private Path getQuestionsFile() throws IOException {
        Path file = getDataDir().resolve("questions.csv");
        if (!Files.exists(file)) {
            try (InputStream res = getClass().getClassLoader().getResourceAsStream("data/questions.csv")) {
                if (res != null) Files.copy(res, file);
                else seedFile(file, "questionText,option1,option2,option3,option4,correctOption,category,difficulty\n");
            }
        }
        return file;
    }

    private void seedFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    // ─── USER OPERATIONS ────────────────────────────────────────────────────

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(getUsersFile());
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            for (CSVRecord record : parser) {
                String email = safeGetStr(record, "email", "");
                User u = new User(
                        record.get("userId"),
                        record.get("password"),
                        record.get("name"),
                        record.get("role"),
                        Boolean.parseBoolean(record.get("examAttempted")),
                        email
                );
                users.add(u);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        try (Writer writer = Files.newBufferedWriter(getUsersFile());
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .builder().setHeader("userId", "password", "name", "role", "examAttempted", "email").build())) {

            for (User u : users) {
                printer.printRecord(u.getUserId(), u.getPassword(), u.getName(),
                        u.getRole(), u.isExamAttempted(), u.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // ─── QUESTION OPERATIONS ────────────────────────────────────────────────

    public List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(getQuestionsFile());
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            int idx = 1;
            for (CSVRecord record : parser) {
                String[] opts = {
                        record.get("option1"),
                        record.get("option2"),
                        record.get("option3"),
                        record.get("option4")
                };
                String category = safeGetStr(record, "category", "General");
                String difficulty = safeGetStr(record, "difficulty", "Medium");

                Question q = new Question(
                        String.valueOf(idx++),
                        record.get("questionText"),
                        opts,
                        Integer.parseInt(record.get("correctOption").trim()),
                        category,
                        difficulty
                );
                questions.add(q);
            }
        } catch (Exception e) {
            System.err.println("Error loading questions: " + e.getMessage());
        }
        return questions;
    }

    private String safeGetStr(CSVRecord record, String col, String defaultVal) {
        try {
            String val = record.get(col);
            return (val != null && !val.isBlank()) ? val.trim() : defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public void saveQuestions(List<Question> questions) {
        try (Writer writer = Files.newBufferedWriter(getQuestionsFile());
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("questionText", "option1", "option2", "option3", "option4",
                                "correctOption", "category", "difficulty")
                     .build())) {

            for (Question q : questions) {
                printer.printRecord(q.getQuestionText(),
                        q.getOptions()[0], q.getOptions()[1],
                        q.getOptions()[2], q.getOptions()[3],
                        q.getCorrectOption(),
                        q.getCategory(),
                        q.getDifficulty());
            }
        } catch (Exception e) {
            System.err.println("Error saving questions: " + e.getMessage());
        }
    }

    public List<Question> parseQuestionsFromStream(InputStream stream) throws IOException {
        List<Question> questions = new ArrayList<>();
        try (Reader reader = new InputStreamReader(stream);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            int idx = 1;
            for (CSVRecord record : parser) {
                String[] opts = {
                        record.get("option1"),
                        record.get("option2"),
                        record.get("option3"),
                        record.get("option4")
                };
                String category  = safeGetStr(record, "category", "General");
                String difficulty = safeGetStr(record, "difficulty", "Medium");

                Question q = new Question(
                        String.valueOf(idx++),
                        record.get("questionText"),
                        opts,
                        Integer.parseInt(record.get("correctOption").trim()),
                        category,
                        difficulty
                );
                questions.add(q);
            }
        }
        return questions;
    }
}

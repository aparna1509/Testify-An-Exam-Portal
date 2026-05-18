# Testify — An Exam Portal

A full-stack online examination system built with Spring Boot (backend) and Vanilla JS (frontend).

## Features
- Student login / registration
- Timed exam with auto-submit
- **Flag questions** for later review
- **Answered progress bar** — track how many questions you've answered
- **Tab-leave warning** — popup if you switch tabs; auto-submits after 3 violations
- **Correct answers + explanations** shown in results
- **Leaderboard** — see how you rank against other students
- Admin panel — manage questions, users, exam config
- CSV bulk question upload

## Running

**Terminal 1 — Backend:**
```
cd backend
mvn spring-boot:run
```
Runs on http://localhost:8080

**Terminal 2 — Frontend:**
```
cd frontend
python -m http.server 5500
```
Open http://localhost:5500

## Default Credentials
- Admin: `admin / admin123`
- Student: `user123 / pass123`

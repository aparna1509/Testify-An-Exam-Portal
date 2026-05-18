# Testify — An Exam Portal

A full-stack exam portal built with Spring Boot (backend) and vanilla HTML/CSS/JS (frontend).

## Features
- Student login & registration (with email)
- Timed online exams with auto-submit
- Auto-save answers (survives page refresh)
- Instant results with question review
- Leaderboard
- Admin panel (manage questions, users, exam config)
- Dark / Light mode toggle
- Mobile responsive
- Email result summary after submission

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.2, Apache Commons CSV
- **Frontend:** HTML5, CSS3, Vanilla JS
- **Data:** CSV-based flat file storage

## Running Locally
1. Start the backend: `cd testify-build/backend && mvn spring-boot:run`
2. Open `testify-build/frontend/index.html` in a browser (or use Live Server)
3. Default admin login: `admin / admin123`

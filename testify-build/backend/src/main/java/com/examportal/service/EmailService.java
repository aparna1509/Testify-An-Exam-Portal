package com.examportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import com.examportal.model.ExamResult;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Sends an exam result summary email.
     * Silently skips if mail is not configured or email is blank.
     */
    public void sendResultEmail(String toEmail, String name, ExamResult result) {
        if (mailSender == null || toEmail == null || toEmail.isBlank()) return;
        try {
            int pct = result.getTotal() > 0
                    ? (int) Math.round((double) result.getScore() * 100 / result.getTotal())
                    : 0;
            String status = result.isPassed() ? "✅ PASSED" : "❌ FAILED";
            long correct  = result.getQuestionResults().stream().filter(r -> r.isCorrect()).count();
            long wrong    = result.getQuestionResults().stream().filter(r -> !r.isCorrect() && r.getUserAnswer() > 0).count();
            long skipped  = result.getQuestionResults().stream().filter(r -> r.getUserAnswer() <= 0).count();

            String html = buildResultEmailHtml(name, pct, status, result.getScore(),
                    result.getTotal(), result.getPassThreshold(), correct, wrong, skipped);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Testify — Your Exam Result: " + pct + "% (" + (result.isPassed() ? "PASS" : "FAIL") + ")");
            helper.setText(html, true);
            mailSender.send(msg);
            System.out.println("Result email sent to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send result email: " + e.getMessage());
        }
    }

    private String buildResultEmailHtml(String name, int pct, String status,
            int score, int total, int threshold, long correct, long wrong, long skipped) {
        String accentColor = pct >= threshold ? "#4ade80" : "#f87171";
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/>" +
            "<style>body{font-family:'Helvetica Neue',Arial,sans-serif;background:#0b0c0f;color:#f0f1f5;margin:0;padding:0}" +
            ".wrap{max-width:520px;margin:0 auto;padding:2rem}" +
            ".header{text-align:center;padding:2rem;background:#161820;border-radius:16px 16px 0 0;border:1px solid #2a2d38;border-bottom:none}" +
            ".logo{font-size:2rem;font-weight:bold;color:#f0f1f5}" +
            ".logo span{color:#f5a623}" +
            ".body{background:#161820;border:1px solid #2a2d38;border-top:none;border-radius:0 0 16px 16px;padding:2rem}" +
            ".score-big{text-align:center;font-size:4rem;font-weight:bold;color:" + accentColor + ";margin:1rem 0 0.25rem}" +
            ".status{text-align:center;font-size:1.2rem;margin-bottom:1.5rem}" +
            ".stats{display:flex;gap:1rem;justify-content:center;flex-wrap:wrap;margin:1.5rem 0;padding:1.5rem;background:#1a1c23;border-radius:10px}" +
            ".stat{text-align:center;min-width:80px}" +
            ".stat-val{font-size:1.6rem;font-weight:bold}" +
            ".stat-lbl{font-size:0.75rem;color:#9ca3b0;text-transform:uppercase;letter-spacing:0.04em}" +
            ".footer{margin-top:2rem;text-align:center;font-size:0.8rem;color:#5a6070}" +
            "p{color:#9ca3b0}" +
            "</style></head><body><div class='wrap'>" +
            "<div class='header'><div class='logo'>Testi<span>fy</span></div></div>" +
            "<div class='body'>" +
            "<p>Hi <strong style='color:#f0f1f5'>" + name + "</strong>, your exam results are in!</p>" +
            "<div class='score-big'>" + pct + "%</div>" +
            "<div class='status'>" + status + " &nbsp;(Pass mark: " + threshold + "%)</div>" +
            "<div class='stats'>" +
            "<div class='stat'><div class='stat-val' style='color:#4ade80'>" + correct + "</div><div class='stat-lbl'>Correct</div></div>" +
            "<div class='stat'><div class='stat-val' style='color:#f87171'>" + wrong + "</div><div class='stat-lbl'>Wrong</div></div>" +
            "<div class='stat'><div class='stat-val' style='color:#9ca3b0'>" + skipped + "</div><div class='stat-lbl'>Skipped</div></div>" +
            "<div class='stat'><div class='stat-val' style='color:#f0f1f5'>" + score + "/" + total + "</div><div class='stat-lbl'>Score</div></div>" +
            "</div>" +
            "<p>Log back in to Testify to review your answers and see the full question-by-question breakdown.</p>" +
            "<div class='footer'>This is an automated message from Testify — An Exam Portal.</div>" +
            "</div></div></body></html>";
    }
}

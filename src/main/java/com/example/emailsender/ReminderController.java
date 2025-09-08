package com.example.emailsender;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/jobs")
public class ReminderController {

    private final JdbcTemplate jdbc;

    public ReminderController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Example call:
     *   http://localhost:9090/api/jobs/reminder?recipient=rasmus.thoren@outlook.com&projectname=DemoProjekt&date=2025-09-08
     */
    @GetMapping("/reminder")
    public ResponseEntity<Boolean> createReminder(
            @RequestParam String recipient,
            @RequestParam String projectname,
            @RequestParam String date
    ) {
        if (isBlank(recipient) || isBlank(projectname) || isBlank(date)) {
            return ResponseEntity.ok(false);
        }

        final LocalDate when;
        try {
            when = LocalDate.parse(date); // parse yyyy-MM-dd
        } catch (Exception ex) {
            return ResponseEntity.ok(false);
        }

        final String subject = "Påminelse om att radera personuppgifter för projekt \"" + projectname + "\"";
        final String bodyHtml = "<p>Påminelse om att radera personuppgifter för projekt \"" +
                escapeHtml(projectname) + "\".</p>";

        int rows = jdbc.update(
                "INSERT INTO outbound_emails (recipient, subject, body, scheduled_at, status) VALUES (?,?,?,?,?)",
                recipient,
                subject,
                bodyHtml,
                Date.valueOf(when),   // store DATE
                "PENDING"
        );

        return ResponseEntity.ok(rows == 1);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // Minimal HTML escaping for safe email body
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

package com.example.emailsender;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.Instant;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/jobs")
public class ReminderController {

    private final JdbcTemplate jdbc;

    public ReminderController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Example call:
     *   http://localhost:9090/api/jobs/reminder?recipient=rasmus.thoren@outlook.com&projectname=DemoProjekt&date=2025-09-08T10:00:00+02:00
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

        final Instant whenUtc;
        try {
            whenUtc = OffsetDateTime.parse(date).toInstant();
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
                new Date(whenUtc.toEpochMilli()), // store only DATE part
                "PENDING"
        );

        return ResponseEntity.ok(rows == 1);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

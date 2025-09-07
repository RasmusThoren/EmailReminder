package com.example.emailsender;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class ReminderController {

    private final JdbcTemplate jdbc;

    public ReminderController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Create a reminder email job.
     * Body must include: recipient, projectname, date (ISO-8601 with timezone, e.g. 2025-09-08T10:00:00+02:00)
     *
     * Example:
     * {
     *   "recipient": "rasmus.thoren@outlook.com",
     *   "projectname": "DemoProjekt",
     *   "date": "2025-09-08T10:00:00+02:00"
     * }
     */
    @PostMapping("/reminder")
    public ResponseEntity<?> createReminder(@RequestBody ReminderRequest req) {
        if (isBlank(req.getRecipient()) || isBlank(req.getProjectname()) || isBlank(req.getDate())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "recipient, projectname and date are required"
            ));
        }

        // Parse date to UTC
        final Instant whenUtc;
        try {
            whenUtc = OffsetDateTime.parse(req.getDate()).toInstant();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "date must be ISO-8601 with timezone, e.g. 2025-09-08T10:00:00+02:00",
                    "details", ex.getMessage()
            ));
        }

        // Subject and body text (Swedish) per your spec
        final String subject = "Påminelse om att radera personuppgifter för projekt \"" + req.getProjectname() + "\"";
        final String bodyHtml = "<p>Påminelse om att radera personuppgifter för projekt \"" +
                escapeHtml(req.getProjectname()) + "\".</p>";

        int rows = jdbc.update(
                "INSERT INTO outbound_emails (recipient, subject, body, scheduled_at) VALUES (?,?,?,?)",
                req.getRecipient(), subject, bodyHtml, Timestamp.from(whenUtc)
        );

        return ResponseEntity.ok(Map.of(
                "status", rows == 1 ? "queued" : "not-inserted",
                "recipient", req.getRecipient(),
                "projectname", req.getProjectname(),
                "scheduledAtUtc", whenUtc.toString()
        ));
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    // Minimal HTML escape for project name to be safe in email body
    private static String escapeHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}

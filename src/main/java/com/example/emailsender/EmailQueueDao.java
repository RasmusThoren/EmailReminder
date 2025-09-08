package com.example.emailsender;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
public class EmailQueueDao {

    private final JdbcTemplate jdbc;

    public EmailQueueDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<EmailRecord> fetchDue(int limit) {
        String sql =
                "SELECT id, recipient, subject, body, scheduled_at, status, sent_at " +
                        "FROM outbound_emails " +
                        "WHERE status=('PENDING', 'FAILED') AND scheduled_at <= CURDATE() " +
                        "ORDER BY scheduled_at ASC " +
                        "LIMIT ?";
        return jdbc.query(sql, (ResultSet rs, int rowNum) -> {
            EmailRecord r = new EmailRecord();
            r.id = rs.getLong("id");
            r.recipient = rs.getString("recipient");
            r.subject = rs.getString("subject");
            r.body = rs.getString("body");

            // scheduled_at is DATE → map to LocalDate
            java.sql.Date sched = rs.getDate("scheduled_at");
            r.scheduledAt = (sched != null ? sched.toLocalDate() : null);

            r.status = rs.getString("status");

            // sent_at is DATETIME → map to LocalDateTime
            Timestamp sent = rs.getTimestamp("sent_at");
            r.sentAt = (sent != null ? sent.toLocalDateTime() : null);

            return r;
        }, limit);
    }

    public void markSent(long id) {
        jdbc.update("UPDATE outbound_emails SET status='SENT', sent_at=UTC_TIMESTAMP() WHERE id=?", id);
    }

    public void markFailed(long id) {
        jdbc.update("UPDATE outbound_emails SET status='FAILED' WHERE id=?", id);
    }

    public List<EmailRecord> fetchDueForToday(LocalDate today, int limit) {
        String sql =
                "SELECT id, recipient, subject, body, scheduled_at, status, sent_at " +
                        "FROM outbound_emails " +
                        "WHERE status='PENDING' AND DATE(scheduled_at) = ? " +
                        "ORDER BY scheduled_at ASC " +
                        "LIMIT ?";

        return jdbc.query(sql, (ResultSet rs, int rowNum) -> {
            EmailRecord r = new EmailRecord();
            r.id = rs.getLong("id");
            r.recipient = rs.getString("recipient");
            r.subject = rs.getString("subject");
            r.body = rs.getString("body");

            // scheduled_at is DATE → map to LocalDate
            java.sql.Date sched = rs.getDate("scheduled_at");
            r.scheduledAt = (sched != null ? sched.toLocalDate() : null);

            r.status = rs.getString("status");

            // sent_at is DATETIME → map to LocalDateTime
            Timestamp sent = rs.getTimestamp("sent_at");
            r.sentAt = (sent != null ? sent.toLocalDateTime() : null);

            return r;
        }, today, limit);
    }
}

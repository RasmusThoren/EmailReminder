package com.example.emailsender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmailRecord {
    public long id;
    public String recipient;
    public String subject;
    public String body;               // HTML or plain text
    public LocalDate scheduledAt;     // DATE (UTC, but only yyyy-MM-dd)
    public String status;             // PENDING | SENT | FAILED
    public LocalDateTime sentAt;      // DATETIME
}

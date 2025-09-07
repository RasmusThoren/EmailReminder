package com.example.emailsender;

import java.time.LocalDateTime;

public class EmailRecord {
    public long id;
    public String recipient;
    public String subject;
    public String body;               // HTML or plain text
    public LocalDateTime scheduledAt; // UTC
    public String status;             // PENDING | SENT | FAILED
    public LocalDateTime sentAt;
}

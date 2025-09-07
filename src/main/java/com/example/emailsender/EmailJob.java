package com.example.emailsender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailJob {

    private final EmailQueueDao dao;
    private final MailService mail;

    // Comes from application.yml (spring.mail.username), set via env var
    private final String gmailAddress;

    public EmailJob(EmailQueueDao dao, MailService mail,
                    @Value("${spring.mail.username}") String gmailAddress) {
        this.dao = dao;
        this.mail = mail;
        this.gmailAddress = gmailAddress;
    }

    // Run every 15 seconds
    @Scheduled(fixedDelay = 15_000)
    public void dispatch() {
        try {
            List<EmailRecord> due = dao.fetchDue(50);
            if (due.isEmpty()) return;

            for (EmailRecord r : due) {
                try {
                    mail.sendHtml(gmailAddress, r.recipient, r.subject, r.body);
                    dao.markSent(r.id);
                    System.out.printf("SENT id=%d to %s%n", r.id, r.recipient);
                } catch (Exception ex) {
                    System.err.printf("FAILED id=%d to %s : %s%n", r.id, r.recipient, ex.getMessage());
                    dao.markFailed(r.id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

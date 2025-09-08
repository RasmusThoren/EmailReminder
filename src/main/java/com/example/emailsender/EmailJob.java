package com.example.emailsender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Scheduled(cron = "*/15 * * * * ?")
    public void dispatch() {
        System.out.printf("%n[DEBUG] ===== Scheduler tick at %s =====%n", LocalDateTime.now());
        try {
            LocalDate today = LocalDate.now();
            System.out.printf("[DEBUG] Today = %s%n", today);

            List<EmailRecord> due = dao.fetchDueForToday(today, 50);
            System.out.printf("[DEBUG] DAO returned %d records%n", due.size());

            if (due.isEmpty()) {
                System.out.println("[DEBUG] No emails to send this tick.");
                return;
            }

            for (EmailRecord r : due) {
                System.out.printf("[DEBUG] Processing record id=%d, recipient=%s, subject=%s, status=%s, scheduledAt=%s%n",
                        r.id, r.recipient, r.subject, r.status, r.scheduledAt);

                try {
                    System.out.printf("[DEBUG] Attempting to send email id=%d to %s%n", r.id, r.recipient);
                    mail.sendHtml(gmailAddress, r.recipient, r.subject, r.body);
                    dao.markSent(r.id);
                    System.out.printf("[INFO] SENT id=%d to %s%n", r.id, r.recipient);
                } catch (Exception ex) {
                    System.err.printf("[ERROR] FAILED id=%d to %s : %s%n",
                            r.id, r.recipient, ex.getMessage());
                    ex.printStackTrace(System.err);
                    dao.markFailed(r.id);
                }
            }
        } catch (Exception e) {
            System.err.printf("[FATAL] Exception in scheduler: %s%n", e.getMessage());
            e.printStackTrace(System.err);
        }
        System.out.println("[DEBUG] ===== End of scheduler tick =====\n");
    }
}

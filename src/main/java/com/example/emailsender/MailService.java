package com.example.emailsender;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mail;

    public MailService(JavaMailSender mail) {
        this.mail = mail;
    }

    public void sendHtml(String fromGmail, String to, String subject, String bodyHtml) throws Exception {
        MimeMessage mime = mail.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(mime, "UTF-8");
        h.setFrom(fromGmail);
        h.setTo(to);
        h.setSubject(subject);
        h.setText(bodyHtml, true);
        mail.send(mime);
    }
}

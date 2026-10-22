package com.lostandfound.backend.mail;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

/**
 * Sends claim emails. Emails go out only AFTER the claim transaction commits (so a rolled-back
 * claim never produces an email) and on a background thread (so SMTP problems never fail or slow
 * the API request). A failed email is logged and dropped.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final EmailTemplates templates;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String from,
                        @Value("${app.mail.platform-name:Lost & Found}") String platformName,
                        @Value("${app.mail.timezone:Asia/Singapore}") String timezone) {
        this.mailSender = mailSender;
        this.from = from;
        this.templates = new EmailTemplates(platformName, ZoneId.of(timezone));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClaimEmail(ClaimEmailEvent e) {
        send(e.to(), templates.subject(e), templates.html(e), "claim " + e.kind() + " " + e.claimId());
    }

    /** Account emails are sent straight away (in the background): the caller has already saved the token. */
    @Async
    public void sendVerificationEmail(String to, String name, String link, int hours) {
        send(to, templates.verifyEmailSubject(), templates.verifyEmailHtml(name, link, hours), "verify-email");
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String link, int minutes) {
        send(to, templates.resetPasswordSubject(), templates.resetPasswordHtml(name, link, minutes), "password-reset");
    }

    private void send(String to, String subject, String html, String label) {
        if (to == null || to.isBlank()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, false, StandardCharsets.UTF_8.name());
            h.setFrom(from);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent: {}", label);
        } catch (Exception ex) {
            log.error("Could not send email: {}", label, ex);
        }
    }
}
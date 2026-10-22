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
        if (e.to() == null || e.to().isBlank()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, false, StandardCharsets.UTF_8.name());
            h.setFrom(from);
            h.setTo(e.to());
            h.setSubject(templates.subject(e));
            h.setText(templates.html(e), true);
            mailSender.send(msg);
            log.info("Claim email {} sent for claim {}", e.kind(), e.claimId());
        } catch (Exception ex) {
            log.error("Could not send claim email {} for claim {}", e.kind(), e.claimId(), ex);
        }
    }
}
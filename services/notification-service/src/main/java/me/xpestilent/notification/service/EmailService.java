package me.xpestilent.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public Mono<Void> sendHtmlEmail(String to, String subject, String htmlBody) {
        return Mono.fromRunnable(() -> {
                try {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                    helper.setFrom("noreply@foundation.com");
                    helper.setTo(to);
                    helper.setSubject(subject);
                    helper.setText(htmlBody, true);

                    mailSender.send(message);
                    log.info(
                        "Email sent successfully",
                        kv("subject", subject),
                        kv("to", to)
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to send email", e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
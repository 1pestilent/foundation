package me.xpestilent.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.notification.dto.RenderedTemplate;
import me.xpestilent.notification.repository.NotificationTemplateRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final SpringWebFluxTemplateEngine templateEngine;

    public Mono<RenderedTemplate> renderTemplate(String templateCode, Map<String, Object> variables) {
        return templateRepository.findByCode(templateCode)
            .switchIfEmpty(Mono.error(new RuntimeException("Шаблон не найден в БД: " + templateCode)))
            .map(template -> {

                Context context = new Context();
                context.setVariables(variables);

                String htmlBody = templateEngine.process(template.getHtmlBody(), context);

                String subject = template.getSubject();
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    subject = subject.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }

                return new RenderedTemplate(subject, htmlBody);
            });
    }
}

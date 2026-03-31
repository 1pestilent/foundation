package me.xpestilent.notification.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notification_templates")
public class NotificationTemplateEntity {
    @Id
    private Long id;
    private String code;
    private String subject;
    private String htmlBody;
}
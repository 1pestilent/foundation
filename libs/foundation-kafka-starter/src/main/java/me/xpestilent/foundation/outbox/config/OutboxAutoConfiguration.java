package me.xpestilent.foundation.outbox.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackages = "me.xpestilent.foundation.outbox.entity")
@EnableJpaRepositories(basePackages = "me.xpestilent.foundation.outbox.repository")
@ComponentScan(basePackages = "me.xpestilent.foundation.outbox.publisher")
public class OutboxAutoConfiguration {

}

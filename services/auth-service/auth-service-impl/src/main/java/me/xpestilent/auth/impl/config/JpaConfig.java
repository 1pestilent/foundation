package me.xpestilent.auth.impl.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
    "me.xpestilent.auth.impl.repository",
    "me.xpestilent.foundation.outbox.repository"
})
@EntityScan(basePackages = {
    "me.xpestilent.auth.impl.entity",
    "me.xpestilent.foundation.outbox.entity"
})
public class JpaConfig {
}

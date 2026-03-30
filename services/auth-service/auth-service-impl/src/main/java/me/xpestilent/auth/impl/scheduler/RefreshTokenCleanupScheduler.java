package me.xpestilent.auth.impl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.impl.repository.RefreshTokenRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(name = "cleanupTokens", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens...");

        try {
            int deletedCount = refreshTokenRepository.deleteAllExpired();

            if (deletedCount > 0) {
                log.info("Expired tokens cleanup finished", keyValue("deletedCount", deletedCount));
            } else {
                log.debug("No expired tokens found to cleanup");
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
        }
    }

}

package me.xpestilent.foundation.logging.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.logging.marker.LoggableAsError;
import me.xpestilent.foundation.logging.marker.LoggableAsWarning;
import me.xpestilent.foundation.logging.marker.NotLoggable;
import me.xpestilent.foundation.logging.service.ExceptionLoggerService;
import me.xpestilent.foundation.web.exception.BusinessException;
import me.xpestilent.foundation.web.exception.SystemException;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExceptionLoggerServiceImpl implements ExceptionLoggerService {

    public void log(Throwable ex) {
        if (ex instanceof NotLoggable) {
            return;
        }

        String message = "Exception occurred: " + ex.getMessage();

        LoggingEventBuilder logBuilder = log.atInfo();

        if (ex instanceof LoggableAsWarning) {
            logBuilder = log.atWarn();
        } else if (ex instanceof LoggableAsError || !(ex instanceof RuntimeException)) {
            logBuilder = log.atError().setCause(ex);
        }

        if (ex instanceof BusinessException bizEx && !bizEx.getDetails().isEmpty()) {
            bizEx.getDetails().forEach(logBuilder::addKeyValue);
        } else if (ex instanceof SystemException sysEx && !sysEx.getDetails().isEmpty()) {
            sysEx.getDetails().forEach(logBuilder::addKeyValue);
        }

        logBuilder.log(message);
    }
}

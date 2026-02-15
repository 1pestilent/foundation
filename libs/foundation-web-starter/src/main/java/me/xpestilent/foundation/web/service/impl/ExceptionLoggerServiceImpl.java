package me.xpestilent.foundation.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.web.marker.LoggableAsError;
import me.xpestilent.foundation.web.marker.LoggableAsWarning;
import me.xpestilent.foundation.web.marker.NotLoggable;
import me.xpestilent.foundation.web.service.ExceptionLoggerService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExceptionLoggerServiceImpl implements ExceptionLoggerService {
    public void log(Throwable ex) {
        if (ex instanceof NotLoggable) {
            return;
        }

        String message = "Exception occurred: " + ex.getMessage();

        if (ex instanceof LoggableAsWarning) {
            log.warn(message);
        } else if (ex instanceof LoggableAsError || !(ex instanceof RuntimeException)) {
            log.error(message, ex);
        } else {
            log.info(message);
        }
    }
}

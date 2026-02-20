package me.xpestilent.foundation.logging.service;

import org.springframework.stereotype.Service;

@Service
public interface ExceptionLoggerService {

    void log(Throwable ex);
}

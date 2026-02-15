package me.xpestilent.foundation.web.service;

import org.springframework.stereotype.Service;

@Service
public interface ExceptionLoggerService {

    void log(Throwable ex);
}

package me.xpestilent.foundation.web.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public interface MetricService {

    void recordError(Throwable exception, HttpStatus httpStatus);
}

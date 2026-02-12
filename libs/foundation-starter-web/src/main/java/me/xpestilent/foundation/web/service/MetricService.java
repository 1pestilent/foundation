package me.xpestilent.foundation.web.service;

import org.springframework.http.HttpStatus;

public interface MetricService {

    void enrich(Throwable exception, HttpStatus httpStatus);
}

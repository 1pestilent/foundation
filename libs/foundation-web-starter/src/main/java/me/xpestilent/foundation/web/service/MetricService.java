package me.xpestilent.foundation.web.service;

import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public interface MetricService {

    void enrich(ApiResponse<?> response, Throwable exception, HttpStatus httpStatus);
}

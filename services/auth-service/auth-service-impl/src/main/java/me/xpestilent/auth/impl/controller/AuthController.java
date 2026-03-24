package me.xpestilent.auth.impl.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.xpestilent.auth.api.client.AuthApi;
import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RefreshRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.auth.impl.service.JwtService;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.web.exception.BusinessException;
import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ApiResponse.success(response, 201);
    }

    @Override
    public ApiResponse<LoginResponse> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = httpRequest.getRemoteAddr();
        }

        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceId = httpRequest.getHeader("X-Device-Id");

        LoginResponse response = userService.login(request, ip, userAgent, deviceId);
        return ApiResponse.success(response, 200);
    }

    @Override
    public ApiResponse<LoginResponse> refresh(
        HttpServletRequest httpRequest,
        RefreshRequest request
    ) {
        String refreshToken = request.refreshToken();

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new BusinessException("Отсутствует или некорректный токен", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String token = refreshToken.substring(7);

        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = httpRequest.getRemoteAddr();
        }
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceId = httpRequest.getHeader("X-Device-Id");

        LoginResponse response = userService.refresh(token, ip, userAgent, deviceId);
        return ApiResponse.success(response, 200);
    }

    @Override
    public String getPublicKey() {
        return jwtService.getPublicKeyPem();
    }
}

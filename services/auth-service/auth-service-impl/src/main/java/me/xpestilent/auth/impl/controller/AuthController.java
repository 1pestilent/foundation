package me.xpestilent.auth.impl.controller;

import lombok.RequiredArgsConstructor;
import me.xpestilent.auth.api.client.AuthApi;
import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RefreshRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.auth.impl.service.JwtService;
import me.xpestilent.auth.impl.service.TokenService;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.annotation.ClientData;
import me.xpestilent.foundation.model.ClientInfo;
import me.xpestilent.foundation.web.exception.BusinessException;
import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtService jwtService;

    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ApiResponse.success(response, 201);
    }

    @Override
    public ApiResponse<LoginResponse> login(
        @RequestBody LoginRequest request,
        @ClientData ClientInfo clientInfo
    ) {
        LoginResponse response = userService.login(
            request,
            clientInfo
        );

        return ApiResponse.success(response, 200);
    }

    @Override
    public ApiResponse<LoginResponse> refresh(
        RefreshRequest request,
        @ClientData ClientInfo clientInfo
    ) {
        String refreshToken = request.refreshToken();

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new BusinessException("Отсутствует или некорректный токен", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        String token = refreshToken.substring(7);

        LoginResponse response = tokenService.rotateSession(
            token,
            clientInfo
        );
        return ApiResponse.success(response, 200);
    }

    @Override
    public String getPublicKey() {
        return jwtService.getPublicKeyPem();
    }

    @Override
    public ApiResponse<String> verifyEmail(String token) {
        userService.verifyEmail(token);
        return ApiResponse.success("Email успешно подтвержден. Теперь вы можете войти в систему.");
    }
}

package me.xpestilent.auth.impl.controller;

import lombok.RequiredArgsConstructor;
import me.xpestilent.auth.api.client.AuthApi;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;

    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ApiResponse.success(response, 201);
    }
}

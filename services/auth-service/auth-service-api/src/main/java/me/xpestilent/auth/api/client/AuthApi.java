package me.xpestilent.auth.api.client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RequestMapping("api/v1/auth")
public interface AuthApi {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    ApiResponse<LoginResponse> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        @RequestHeader(value = "X-Device-Id", required = false) String deviceId
    );
}

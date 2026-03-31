package me.xpestilent.auth.api.client;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RefreshRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.foundation.annotation.ClientData;
import me.xpestilent.foundation.model.ClientInfo;
import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;


@RequestMapping("api/v1/auth")
public interface AuthApi {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    ApiResponse<LoginResponse> login(
        @RequestBody LoginRequest request,
        @Parameter(hidden = true) @ClientData ClientInfo clientInfo
    );

    @PostMapping("/refresh")
    ApiResponse<LoginResponse> refresh(
        @Valid @RequestBody RefreshRequest request,
        @Parameter(hidden = true) @ClientData ClientInfo clientInfo
    );

    @GetMapping(value = "/public-key", produces = "text/plain")
    String getPublicKey();

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    ApiResponse<String> verifyEmail(
        @RequestParam String token
    );
}


package me.xpestilent.auth.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "Токен обновления обязателен")
    String refreshToken
) {}
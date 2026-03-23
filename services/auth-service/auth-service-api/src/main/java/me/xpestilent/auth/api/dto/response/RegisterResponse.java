package me.xpestilent.auth.api.dto.response;

import me.xpestilent.auth.api.enums.UserStatus;

import java.util.UUID;

public record RegisterResponse(
    UUID userId,
    UserStatus userStatus,
    String message
) {
}

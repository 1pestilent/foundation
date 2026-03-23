package me.xpestilent.auth.api.dto.response;

import java.time.Instant;

public record GeneratedToken(
    String token,
    Instant expiresAt
) {
}

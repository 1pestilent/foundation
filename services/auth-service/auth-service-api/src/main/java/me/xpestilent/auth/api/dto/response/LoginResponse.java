package me.xpestilent.auth.api.dto.response;

public record LoginResponse(

    String accessToken,

    String refreshToken,

    String tokenType,

    long expiresIn,

    long issuedAt
) {
}

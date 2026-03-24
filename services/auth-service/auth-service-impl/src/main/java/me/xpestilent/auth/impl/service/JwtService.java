package me.xpestilent.auth.impl.service;

import io.jsonwebtoken.Claims;
import me.xpestilent.auth.api.dto.response.GeneratedToken;
import me.xpestilent.auth.impl.entity.UserEntity;

import java.util.UUID;

public interface JwtService {

    GeneratedToken generateAccessToken(UserEntity user, UUID sessionId);

    GeneratedToken generateRefreshToken(UserEntity user, UUID jti);

    String getPublicKeyPem();

    Claims parseToken(String token);
}

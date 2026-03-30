package me.xpestilent.auth.impl.service.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.api.dto.response.GeneratedToken;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.impl.entity.RefreshTokenEntity;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.auth.impl.repository.RefreshTokenRepository;
import me.xpestilent.auth.impl.service.JwtService;
import me.xpestilent.auth.impl.service.TokenService;
import me.xpestilent.foundation.web.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${foundation.security.jwt.type.tokens}")
    private String tokensType;

    @Value("${foundation.security.jwt.type.refresh}")
    private String refreshTokenType;

    @Value("${foundation.security.jwt.expiration.access}")
    private long accessExpiration;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public LoginResponse createSession(UserEntity user, String ip, String userAgent, String deviceId) {
        UUID jti = UuidCreator.getTimeOrderedEpoch();

        GeneratedToken access = jwtService.generateAccessToken(user, jti);
        GeneratedToken refresh = jwtService.generateRefreshToken(user, jti);

        RefreshTokenEntity session = RefreshTokenEntity.builder()
            .jti(jti)
            .user(user)
            .ipAddress(ip)
            .userAgent(userAgent)
            .deviceId(deviceId)
            .expiresAt(refresh.expiresAt())
            .build();

        refreshTokenRepository.save(session);

        log.info("Session created successfully",
            keyValue("userId", user.getId()),
            keyValue("jti", jti));

        return new LoginResponse(
            access.token(),
            refresh.token(),
            tokensType,
            accessExpiration / 1000,
            access.expiresAt().toEpochMilli()
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = BusinessException.class)
    public LoginResponse rotateSession(String refreshToken, String ip, String userAgent, String deviceId) {
        Claims claims;
        try {
            claims = jwtService.parseToken(refreshToken, refreshTokenType);
        } catch (Exception e) {
            log.warn("Invalid refresh token provided", keyValue("error", e.getMessage()));
            throw new BusinessException("Невалидный токен обновления", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        UUID jti = UUID.fromString(claims.get("jti", String.class));

        RefreshTokenEntity session = refreshTokenRepository.findById(jti)
            .orElseThrow(() -> {
                log.warn("Refresh token reuse attempt or session expired", keyValue("jti", jti));
                return new BusinessException("Сессия не найдена или уже обновлена", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            });

        if (session.getDeviceId() != null && !session.getDeviceId().equals(deviceId)) {
            log.error("Device ID mismatch! Deleting compromised session.", keyValue("jti", jti));
            refreshTokenRepository.delete(session);
            throw new BusinessException("Попытка доступа с неизвестного устройства", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        UserEntity user = session.getUser();

        refreshTokenRepository.delete(session);

        return createSession(user, ip, userAgent, deviceId);
    }
}

package me.xpestilent.auth.impl.service.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.GeneratedToken;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.auth.api.event.UserRegisteredEvent;
import me.xpestilent.auth.impl.entity.RefreshTokenEntity;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.auth.impl.mapper.UserMapper;
import me.xpestilent.auth.impl.repository.RefreshTokenRepository;
import me.xpestilent.auth.impl.repository.UserRepository;
import me.xpestilent.auth.impl.service.JwtService;
import me.xpestilent.auth.impl.service.RoleService;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.outbox.publisher.EventPublisher;
import me.xpestilent.foundation.web.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${foundation.security.jwt.type.tokens}")
    String tokensType;

    @Value("${foundation.security.jwt.expiration.access}")
    long accessExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        userRepository.findByUsernameOrEmail(request.username(), request.email())
            .ifPresent(user -> {
                if (user.getUsername().equals(request.username())) {
                    throw new BusinessException(
                        "Username already taken",
                        "AUTH_USERNAME_TAKEN",
                        HttpStatus.CONFLICT,
                        Map.of("username", request.username())
                    );
                } else {
                    throw new BusinessException(
                        "Email already taken",
                        "AUTH_EMAIL_TAKEN",
                        HttpStatus.CONFLICT,
                        Map.of("email", request.email())
                    );
                }
            });

        UserEntity user = userMapper.registerUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        roleService.assignDefaultRole(user);
        user = userRepository.save(user);

        log.debug("User entity successfully saved to database", keyValue("userId", user.getId()));

        UserRegisteredEvent event = UserRegisteredEvent.builder()
            .eventId(UuidCreator.getTimeOrderedEpoch())
            .aggregateId(user.getId().toString())
            .username(user.getUsername())
            .email(user.getEmail())
            .build();

        eventPublisher.publish(event);

        log.info("User successfully registered and event published",
            keyValue("eventId", event.getEventId()),
            keyValue("userId", event.getAggregateId()),
            keyValue("username", user.getUsername()));

        return new RegisterResponse(user.getId(), user.getStatus(), "Пользователь успешно зарегистрирован. Пожалуйста, подтвердите email.");
    }

    @Override
    @Transactional
    public LoginResponse login(
        LoginRequest request,
        String ip,
        String userAgent,
        String deviceId
    ) {
        UserEntity user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException("Неверный логин или пароль", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Invalid password for user", keyValue("userId", user.getId()));
            throw new BusinessException("Неверный логин или пароль", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        UUID jti = UuidCreator.getTimeOrderedEpoch();

        GeneratedToken access = jwtService.generateAccessToken(user, jti);
        GeneratedToken refresh = jwtService.generateRefreshToken(user, jti);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
            .jti(jti)
            .user(user)
            .ipAddress(ip)
            .userAgent(userAgent)
            .deviceId(deviceId)
            .expiresAt(refresh.expiresAt())
            .build();

        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully", keyValue("userId", user.getId()), keyValue("jti", jti));

        return new LoginResponse(
            access.token(),
            refresh.token(),
            tokensType,
            accessExpiration / 1000,
            access.expiresAt().toEpochMilli()
        );
    }

    @Override
    @Transactional
    public LoginResponse refresh(String refreshToken, String ip, String userAgent, String deviceId) {

        Claims claims;

        try {
            claims = jwtService.parseToken(refreshToken);
        } catch (Exception e) {
            log.warn("Invalid refresh token provided", keyValue("error", e.getMessage()));
            throw new BusinessException("Невалидный токен обновления", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        if (!"refresh".equals(claims.get("typ"))) {
            throw new BusinessException("Ожидается токен типа refresh", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
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

        UUID newJti = UuidCreator.getTimeOrderedEpoch();
        GeneratedToken newAccess = jwtService.generateAccessToken(user, newJti);
        GeneratedToken newRefresh = jwtService.generateRefreshToken(user, newJti);

        RefreshTokenEntity newSession = RefreshTokenEntity.builder()
            .jti(newJti)
            .user(user)
            .ipAddress(ip)
            .userAgent(userAgent)
            .deviceId(deviceId)
            .expiresAt(newRefresh.expiresAt())
            .build();

        refreshTokenRepository.save(newSession);

        log.info("Token successfully rotated", keyValue("userId", user.getId()), keyValue("oldJti", jti), keyValue("newJti", newJti));

        return new LoginResponse(
            newAccess.token(),
            newRefresh.token(),
            tokensType,
            accessExpiration / 1000,
            newAccess.expiresAt().toEpochMilli()
        );
    }
}

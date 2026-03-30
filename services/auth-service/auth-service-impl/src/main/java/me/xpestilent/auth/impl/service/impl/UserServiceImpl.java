package me.xpestilent.auth.impl.service.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.auth.api.enums.UserStatus;
import me.xpestilent.auth.api.event.UserRegisteredEvent;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.auth.impl.mapper.UserMapper;
import me.xpestilent.auth.impl.repository.UserRepository;
import me.xpestilent.auth.impl.service.JwtService;
import me.xpestilent.auth.impl.service.RoleService;
import me.xpestilent.auth.impl.service.TokenService;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.model.ClientInfo;
import me.xpestilent.foundation.outbox.publisher.EventPublisher;
import me.xpestilent.foundation.web.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${foundation.security.jwt.type.verification}")
    String verificationTokenType;

    private final UserRepository userRepository;
    private final TokenService tokenService;
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

        String verificationToken = jwtService.generateEmailVerificationToken(user);

        log.debug("User entity successfully saved to database", keyValue("userId", user.getId()));

        UserRegisteredEvent event = UserRegisteredEvent.builder()
            .eventId(UuidCreator.getTimeOrderedEpoch())
            .aggregateId(user.getId().toString())
            .username(user.getUsername())
            .email(user.getEmail())
            .verificationToken(verificationToken)
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
        ClientInfo clientInfo
    ) {
        UserEntity user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException("Неверный логин или пароль", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Invalid password for user", keyValue("userId", user.getId()));
            throw new BusinessException("Неверный логин или пароль", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            handleInactiveStatus(user);
        }

        return tokenService.createSession(user, clientInfo);
    }

    @Override
    @Transactional
    public void verifyEmail(String verificationToken) {
        Claims claims = jwtService.parseToken(verificationToken, verificationTokenType);

        UUID userId = UUID.fromString(claims.getSubject());

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                    "User not found",
                    "USER_NOT_FOUND",
                    HttpStatus.NOT_FOUND,
                    Map.of("userId", userId.toString())
                )
            );

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(
                "Email is already verified",
                "AUTH_EMAIL_ALREADY_VERIFIED",
                HttpStatus.BAD_REQUEST,
                Map.of("email", user.getEmail())
            );
        }

        user.setStatus(UserStatus.ACTIVE);
    }

    private void handleInactiveStatus(UserEntity user) {

        log.warn("Attempt to login with non-active status",
            keyValue("userId", user.getId()),
            keyValue("status", user.getStatus())
        );

        if (user.getStatus() == UserStatus.NOT_VERIFIED) {
            throw new BusinessException(
                "Пожалуйста, подтвердите ваш email перед входом",
                "AUTH_EMAIL_NOT_VERIFIED",
                HttpStatus.FORBIDDEN
            );
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessException(
                "Ваш аккаунт заблокирован. Пожалуйста, свяжитесь с поддержкой.",
                "AUTH_USER_BANNED",
                HttpStatus.FORBIDDEN
            );
        }

        throw new BusinessException(
            "Вход в систему временно невозможен",
            "AUTH_ACCOUNT_INACTIVE",
            HttpStatus.FORBIDDEN
        );
    }
}

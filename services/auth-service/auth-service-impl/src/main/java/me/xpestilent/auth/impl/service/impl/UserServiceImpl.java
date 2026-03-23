package me.xpestilent.auth.impl.service.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.auth.api.event.UserRegisteredEvent;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.auth.impl.mapper.UserMapper;
import me.xpestilent.auth.impl.repository.UserRepository;
import me.xpestilent.auth.impl.service.RoleService;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.outbox.publisher.EventPublisher;
import me.xpestilent.foundation.web.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

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
}

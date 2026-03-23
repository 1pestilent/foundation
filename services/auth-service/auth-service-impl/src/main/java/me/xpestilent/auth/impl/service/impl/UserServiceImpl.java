package me.xpestilent.auth.impl.service.impl;

import lombok.RequiredArgsConstructor;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.impl.repository.UserRepository;
import me.xpestilent.auth.impl.service.RoleService;
import me.xpestilent.auth.impl.service.UserService;
import me.xpestilent.foundation.outbox.publisher.EventPublisher;
import me.xpestilent.foundation.web.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            Map<String, Object> details = new HashMap<>();
            details.put("username", request.username());
            throw new BusinessException("Username already taken", "409", HttpStatus.CONFLICT, details);
        }

        if (userRepository.existsByUsername(request.username())) {
            Map<String, Object> details = new HashMap<>();
            details.put("username", request.email());
            throw new BusinessException("Email already taken", "409", HttpStatus.CONFLICT, details);
        }


    }
}

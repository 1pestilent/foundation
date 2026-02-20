package me.xpestilent.user.impl.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.web.exception.BusinessException;
import me.xpestilent.user.api.dto.request.UserCreateRequest;
import me.xpestilent.user.api.dto.response.UserDetailedResponse;
import me.xpestilent.user.impl.entity.UserEntity;
import me.xpestilent.user.impl.mapper.UserMapper;
import me.xpestilent.user.impl.repository.UserRepository;
import me.xpestilent.user.impl.service.RoleService;
import me.xpestilent.user.impl.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;

    @Override
    public UserDetailedResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already exists", "USERNAME_ALREADY_EXISTS", HttpStatus.CONFLICT, Map.of("username", request.getUsername()));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with this email already exists. ", "EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, Map.of("email", request.getEmail()));
        }

        UserEntity userEntity = userMapper.toEntity(request);
        return null;
    }
}

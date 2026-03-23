package me.xpestilent.auth.impl.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.auth.impl.entity.RoleEntity;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.auth.impl.entity.UserRoleEntity;
import me.xpestilent.auth.impl.entity.key.UserRoleId;
import me.xpestilent.auth.impl.repository.RoleRepository;
import me.xpestilent.auth.impl.service.RoleService;
import me.xpestilent.foundation.web.exception.SystemException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Cacheable(value = "roles", key = "#name")
    public RoleEntity getRoleByName(String name) {
        log.debug("Fetching role by name", keyValue("roleName", name));

        return roleRepository.findByName(name).orElseThrow(() -> new SystemException(
            "Role not found",
            "SYSTEM_ROLE_NOT_FOUND",
            Map.of("roleName", name)
        ));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void assignDefaultRole(UserEntity user) {

        RoleEntity defaultRole = getRoleByName("ROLE_USER");

        UserRoleEntity userRole = new UserRoleEntity();

        UserRoleId roleId = new UserRoleId();
        roleId.setUserId(user.getId());
        roleId.setRoleId(defaultRole.getId());

        userRole.setId(roleId);
        userRole.setUser(user);
        userRole.setRole(defaultRole);

        user.getRoles().add(userRole);
        log.info("Assigned default role to user",
            keyValue("userId", user.getId()),
            keyValue("roleName", defaultRole));
    }
}

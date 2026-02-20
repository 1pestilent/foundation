package me.xpestilent.user.impl.service.impl;

import lombok.RequiredArgsConstructor;
import me.xpestilent.user.impl.entity.RoleEntity;
import me.xpestilent.user.impl.entity.UserEntity;
import me.xpestilent.user.impl.repository.RoleRepository;
import me.xpestilent.user.impl.service.RoleService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleEntity getRoleByName(String name) {
        return roleRepository.findByName(name).orElse();
    }

    @Override
    public void assignDefaultRole(UserEntity user) {
        return;
    }
}

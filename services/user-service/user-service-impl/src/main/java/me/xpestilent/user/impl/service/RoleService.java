package me.xpestilent.user.impl.service;

import me.xpestilent.user.impl.entity.RoleEntity;
import me.xpestilent.user.impl.entity.UserEntity;

public interface RoleService {

    RoleEntity getRoleByName(String name);

    void assignDefaultRole(UserEntity user);
}

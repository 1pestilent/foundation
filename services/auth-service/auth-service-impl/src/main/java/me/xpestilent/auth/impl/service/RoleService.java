package me.xpestilent.auth.impl.service;


import me.xpestilent.auth.impl.entity.RoleEntity;
import me.xpestilent.auth.impl.entity.UserEntity;

public interface RoleService {

    RoleEntity getRoleByName(String name);

    void assignDefaultRole(UserEntity user);
}

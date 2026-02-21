package me.xpestilent.user.impl.mapper;

import me.xpestilent.user.api.dto.response.RoleResponse;
import me.xpestilent.user.impl.entity.RoleEntity;
import me.xpestilent.user.impl.entity.UserRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toDto(RoleEntity entity);

    @Mapping(target = "id", source = "role.id")
    @Mapping(target = "name", source = "role.name")
    RoleResponse toDto(UserRoleEntity userRoleEntity);
}

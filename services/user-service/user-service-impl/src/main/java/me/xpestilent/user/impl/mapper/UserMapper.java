package me.xpestilent.user.impl.mapper;

import me.xpestilent.user.api.dto.request.UserCreateRequest;
import me.xpestilent.user.api.dto.response.UserDetailedResponse;
import me.xpestilent.user.impl.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    UserDetailedResponse toDto(UserEntity entity);
}

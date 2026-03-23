package me.xpestilent.auth.impl.mapper;

import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.impl.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", constant = "NOT_VERIFIED")
    UserEntity registerUser(RegisterRequest registerRequest);
}

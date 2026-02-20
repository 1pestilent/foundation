package me.xpestilent.user.api.client;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import me.xpestilent.foundation.web.response.ApiResponse;
import me.xpestilent.user.api.constant.UserApiPaths;
import me.xpestilent.user.api.dto.request.UserCreateRequest;
import me.xpestilent.user.api.dto.response.UserDetailedResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(UserApiPaths.USERS_PUBLIC)
public interface UserApi {

    @PostMapping
    @Operation(summary = "Создание нового пользователя")
    ApiResponse<UserDetailedResponse> createUser(@Valid @RequestBody UserCreateRequest request);
}

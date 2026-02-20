package me.xpestilent.user.impl.controller;

import lombok.RequiredArgsConstructor;
import me.xpestilent.foundation.web.response.ApiResponse;
import me.xpestilent.user.api.client.UserApi;
import me.xpestilent.user.api.dto.request.UserCreateRequest;
import me.xpestilent.user.api.dto.response.UserDetailedResponse;
import me.xpestilent.user.impl.service.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ApiResponse<UserDetailedResponse> createUser(UserCreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }
}

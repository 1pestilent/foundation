package me.xpestilent.user.impl.service;

import me.xpestilent.user.api.dto.request.UserCreateRequest;
import me.xpestilent.user.api.dto.response.UserDetailedResponse;

public interface UserService {

    UserDetailedResponse createUser(UserCreateRequest request);
}

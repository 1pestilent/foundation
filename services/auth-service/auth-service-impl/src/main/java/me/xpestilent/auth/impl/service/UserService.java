package me.xpestilent.auth.impl.service;

import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.RegisterResponse;

public interface UserService {

    RegisterResponse register(RegisterRequest request);
}

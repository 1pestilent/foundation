package me.xpestilent.auth.impl.service;

import me.xpestilent.auth.api.dto.request.LoginRequest;
import me.xpestilent.auth.api.dto.request.RegisterRequest;
import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.api.dto.response.RegisterResponse;
import me.xpestilent.foundation.model.ClientInfo;

public interface UserService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(
        LoginRequest request, ClientInfo clientInfo
    );

    void verifyEmail(String verificationToken);
}

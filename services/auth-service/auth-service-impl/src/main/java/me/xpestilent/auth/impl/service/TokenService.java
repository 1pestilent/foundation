package me.xpestilent.auth.impl.service;

import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.impl.entity.UserEntity;
import me.xpestilent.foundation.model.ClientInfo;

public interface TokenService {

    LoginResponse createSession(UserEntity user, ClientInfo clientInfo);

    LoginResponse rotateSession(String refreshToken, ClientInfo clientInfo);
}

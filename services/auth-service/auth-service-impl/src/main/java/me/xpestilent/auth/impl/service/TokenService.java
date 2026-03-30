package me.xpestilent.auth.impl.service;

import me.xpestilent.auth.api.dto.response.LoginResponse;
import me.xpestilent.auth.impl.entity.UserEntity;

public interface TokenService {

    LoginResponse createSession(UserEntity user, String ip, String userAgent, String deviceId);

    LoginResponse rotateSession(String refreshToken, String ip, String userAgent, String deviceId);
}

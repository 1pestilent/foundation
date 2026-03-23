package me.xpestilent.auth.impl.service;

import me.xpestilent.auth.api.dto.request.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);
}

package me.xpestilent.user.api.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserStatus {
    NOT_VERIFIED("Почта не подтверждена"),
    ACTIVE("Полноценный пользователь"),
    BLOCKED("Заблокированный"),
    DELETED("Удаленный пользователь");

    private final String description;
}

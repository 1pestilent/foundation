package me.xpestilent.foundation.model;

public record ClientInfo(
    String ip,
    String userAgent,
    String deviceId
) {
}

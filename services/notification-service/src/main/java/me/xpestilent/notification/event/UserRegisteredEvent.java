package me.xpestilent.notification.event;

public record UserRegisteredEvent(
    String aggregateId,
    String email,
    String username,
    String verificationToken
) {
}

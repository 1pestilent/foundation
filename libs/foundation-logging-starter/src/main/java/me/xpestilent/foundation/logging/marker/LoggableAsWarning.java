package me.xpestilent.foundation.logging.marker;

/**
 * Маркер для исключений.
 * При наследовании исключение будет иметь уровень логирование WARN.
 * Применяется для ожидаемых бизнес-ошибок. Они не свидетельствуют о поломке системы!
 */
public interface LoggableAsWarning {}

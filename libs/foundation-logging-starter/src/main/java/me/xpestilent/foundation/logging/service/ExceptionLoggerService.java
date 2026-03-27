package me.xpestilent.foundation.logging.service;

import org.springframework.stereotype.Service;


/**
 * Контракт для централизованного сервиса логирования исключений.
 * <p>
 * Обеспечивает единую точку входа для записи ошибок, возникающих в контроллерах
 * (через @ControllerAdvice) или асинхронных консьюмерах.
 */
@Service
public interface ExceptionLoggerService {

    /**
     * Анализирует тип исключения и записывает его в лог с соответствующим уровнем
     * (ERROR, WARN, INFO) или игнорирует его, согласно правилам маркерных интерфейсов.
     *
     * @param ex выброшенное исключение
     */
    void log(Throwable ex);
}

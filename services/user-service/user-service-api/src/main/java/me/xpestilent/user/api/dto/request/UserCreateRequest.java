package me.xpestilent.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание нового пользователя")
public class UserCreateRequest {

    @NotBlank(message = "Username не может быть пустым")
    @Size(min = 3, max = 50, message = "Username должен содержать от 3 до 50 символов")
    @Schema(description = "Уникальное имя пользователя", example = "john_doe")
    private String username;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Электронная почта", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Schema(description = "Пароль (в открытом виде, будет захеширован)", example = "SecurePass123!")
    private String password;

    @Schema(description = "Имя пользователя", example = "John")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Doe")
    private String lastName;
}
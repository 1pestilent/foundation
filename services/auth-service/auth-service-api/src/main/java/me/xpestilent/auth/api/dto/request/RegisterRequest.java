package me.xpestilent.auth.api.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank
    @Size(min = 3, max = 50)
    String username,

    @Email
    @NotBlank
    String email,

    @NotBlank
    @Size(min = 8, max = 100)
    String password
) {
}

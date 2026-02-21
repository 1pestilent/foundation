package me.xpestilent.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RoleResponse {
    @Schema(description = "ID роли")
    private Integer id;

    @Schema(description = "Системное имя роли", example = "ROLE_USER")
    private String name;
}

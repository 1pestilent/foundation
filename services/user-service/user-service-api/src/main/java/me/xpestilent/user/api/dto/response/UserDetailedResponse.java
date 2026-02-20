package me.xpestilent.user.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.xpestilent.user.api.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailedResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Set<RoleResponse> roles;
    private Instant createdAt;
    private Instant updatedAt;
}

package me.xpestilent.user.impl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.xpestilent.user.impl.entity.key.UserRoleId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users_roles")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserRoleEntity {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @CreatedDate
    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "assigned_by")
    private UUID assignedBy;
}
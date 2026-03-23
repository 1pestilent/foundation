package me.xpestilent.auth.impl.repository;

import me.xpestilent.auth.impl.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    /**
     * Удаляет все токены (сессии) конкретного пользователя.
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Удаляет все истекшие токены из БД.
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < CURRENT_TIMESTAMP")
    int deleteAllExpired();
}

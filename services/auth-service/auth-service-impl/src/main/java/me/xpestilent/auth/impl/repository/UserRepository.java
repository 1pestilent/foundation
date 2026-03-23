package me.xpestilent.auth.impl.repository;

import me.xpestilent.auth.impl.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
}

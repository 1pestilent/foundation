package me.xpestilent.user.impl.repository;

import me.xpestilent.user.impl.entity.UserRoleEntity;
import me.xpestilent.user.impl.entity.key.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {

}

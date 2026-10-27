package org.online.shop.magnit.repository;

import org.online.shop.magnit.model.Role;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Role r where r.name = 'ROLE_ADMIN'")
    Optional<Role> lockAdminRole();
}

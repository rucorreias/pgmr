package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);
}
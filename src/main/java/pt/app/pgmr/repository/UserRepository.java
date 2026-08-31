package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
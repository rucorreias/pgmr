package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.Road;

import java.util.Optional;
import java.util.UUID;

public interface RoadRepository extends JpaRepository<Road, UUID> {

    Optional<Road> findByCode(String code);

    boolean existsByCode(String code);
}
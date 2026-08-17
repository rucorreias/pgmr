package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.Road;

import java.util.Optional;

public interface RoadRepository extends JpaRepository<Road, Long> {

    Optional<Road> findByCode(String code);

    boolean existsByCode(String code);
}
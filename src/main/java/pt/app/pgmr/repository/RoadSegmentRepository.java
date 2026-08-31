package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.RoadSegment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, UUID> {

    List<RoadSegment> findByRoadId(UUID roadId);

    Optional<RoadSegment> findByRoadIdAndCode(UUID roadId, String code);

    boolean existsByRoadIdAndCode(UUID roadId, String code);

}
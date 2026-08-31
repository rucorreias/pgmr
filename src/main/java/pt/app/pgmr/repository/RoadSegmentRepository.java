package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.RoadSegment;

import java.util.List;
import java.util.UUID;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, UUID> {

    List<RoadSegment> findByRoadId(Long roadId);

    boolean existsByRoadIdAndCode(Long roadId, String code);
}
package pt.app.pgmr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.app.pgmr.domain.model.RoadSegment;

import java.util.List;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, Long> {

    List<RoadSegment> findByRoadId(Long roadId);

    boolean existsByRoadIdAndCode(Long roadId, String code);
}
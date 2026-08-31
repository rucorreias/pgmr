package pt.app.pgmr.api.dto.road;

import pt.app.pgmr.domain.model.enums.RoadCondition;
import pt.app.pgmr.domain.model.enums.RoadStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response representing a road segment exposed through the API.
 *
 * <p>GIS geometry is intentionally not exposed as a JTS LineString
 * at this stage. The API contract should use a GIS-neutral representation
 * such as GeoJSON when spatial endpoints are introduced.</p>
 */
public record RoadSegmentResponse(
        UUID id,
        UUID roadId,
        String code,
        String name,
        BigDecimal startKm,
        BigDecimal endKm,
        RoadCondition condition,
        RoadStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
package pt.app.pgmr.api.dto.road;

import pt.app.pgmr.domain.model.enums.RoadCondition;
import pt.app.pgmr.domain.model.enums.RoadStatus;
import pt.app.pgmr.domain.model.enums.RoadType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response representing a road exposed through the API.
 */
public record RoadResponseDTO(
        UUID id,
        String code,
        String name,
        RoadType roadType,
        String description,
        BigDecimal lengthKm,
        RoadCondition condition,
        RoadStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
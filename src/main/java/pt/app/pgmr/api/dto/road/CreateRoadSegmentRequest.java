package pt.app.pgmr.api.dto.road;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.app.pgmr.domain.model.enums.RoadCondition;
import pt.app.pgmr.domain.model.enums.RoadStatus;

import java.math.BigDecimal;

/**
 * Request used to create a road segment.
 *
 * <p>The parent road is identified by the URL and is therefore
 * intentionally not part of this request.</p>
 *
 * <p>The segment must satisfy the domain rule:
 * endKm > startKm.</p>
 */
public record CreateRoadSegmentRequest(
        @NotBlank(message = "Road segment code is required")
        @Size(max = 50, message = "Road segment code must not exceed 50 characters")
        String code,

        @Size(max = 255, message = "Road segment name must not exceed 255 characters")
        String name,

        @NotNull(message = "Segment start kilometer is required")
        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Segment start kilometer must be greater than or equal to 0"
        )
        BigDecimal startKm,
        @NotNull(message = "Segment end kilometer is required")

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Segment end kilometer must be greater than or equal to 0"
        )
        BigDecimal endKm,

        RoadCondition condition,

        RoadStatus status
) {
}
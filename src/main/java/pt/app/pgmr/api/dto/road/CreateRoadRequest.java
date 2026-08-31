package pt.app.pgmr.api.dto.road;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.app.pgmr.domain.model.enums.RoadType;

import java.math.BigDecimal;

/**
 * Request used to create a new road.
 *
 * <p>The road condition and status are intentionally not exposed here.
 * New roads use the domain/database defaults:
 * GOOD condition and ACTIVE status.</p>
 */
public record CreateRoadRequest(
        @NotBlank(message = "Road code is required")
        @Size(max = 50, message = "Road code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Road name is required")
        @Size(max = 255, message = "Road name must not exceed 255 characters")
        String name,

        @NotNull(message = "Road type is required")
        RoadType roadType,

        @Size(max = 10000, message = "Description must not exceed 10000 characters")
        String description,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Road length must be greater than or equal to 0"
        )
        BigDecimal lengthKm
) {
}
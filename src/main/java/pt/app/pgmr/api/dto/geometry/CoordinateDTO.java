package pt.app.pgmr.api.dto.geometry;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a single geographic coordinate.
 *
 * <p>Coordinates follow the GeoJSON ordering convention:</p>
 *
 * <pre>
 * [longitude, latitude]
 * </pre>
 *
 * <p>Longitude must be between -180 and 180 degrees and latitude
 * between -90 and 90 degrees.</p>
 *
 * @param longitude longitude in degrees
 * @param latitude  latitude in degrees
 */
public record CoordinateDTO(

        @NotNull(message = "Longitude is required")
        Double longitude,

        @NotNull(message = "Latitude is required")
        Double latitude

) {

    /**
     * Validates that the coordinate represents a valid WGS 84 position.
     *
     * @throws IllegalArgumentException if longitude or latitude is outside
     *                                  its valid range
     */
    public void validate() {
        if (longitude == null || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }

        if (latitude == null || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }
    }
}

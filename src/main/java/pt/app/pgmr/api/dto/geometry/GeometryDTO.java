package pt.app.pgmr.api.dto.geometry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Data Transfer Object representing a spatial geometry exposed through the API.
 *
 * <p>The application currently supports LineString geometries for road segments.
 * Coordinates are represented as {@code [longitude, latitude]} pairs, following
 * the GeoJSON coordinate convention and the EPSG:4326 spatial reference system.</p>
 *
 * <p>This DTO intentionally does not expose JTS types. JTS is an infrastructure
 * concern and should remain outside the API contract.</p>
 *
 * @param type        geometry type; currently {@code LineString}
 * @param coordinates ordered list of coordinate pairs represented as
 *                    {@code [longitude, latitude]}
 */
public record GeometryDTO(

        @NotBlank(message = "Geometry type is required")
        String type,

        @NotNull(message = "Geometry coordinates are required")
        @NotEmpty(message = "Geometry must contain at least two coordinates")
        List<@NotNull CoordinateDTO> coordinates

) {

    /**
     * Supported geometry type for road segments.
     */
    public static final String LINE_STRING = "LineString";

    /**
     * EPSG identifier for WGS 84 geographic coordinates.
     */
    public static final int SRID = 4326;

    /**
     * Creates a LineString geometry DTO.
     *
     * @param coordinates ordered coordinates
     * @return a LineString geometry
     * @throws IllegalArgumentException if fewer than two coordinates are supplied
     */
    public static GeometryDTO lineString(List<CoordinateDTO> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException(
                    "A LineString must contain at least two coordinates"
            );
        }

        return new GeometryDTO(LINE_STRING, List.copyOf(coordinates));
    }

    /**
     * Validates the geometry according to the domain constraints currently
     * supported by the API.
     *
     * @throws IllegalArgumentException if the geometry is invalid
     */
    public void validate() {
        if (!LINE_STRING.equals(type)) {
            throw new IllegalArgumentException(
                    "Unsupported geometry type: " + type
            );
        }

        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException(
                    "A LineString must contain at least two coordinates"
            );
        }
    }
}
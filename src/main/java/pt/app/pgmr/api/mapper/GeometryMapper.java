package pt.app.pgmr.api.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;
import pt.app.pgmr.api.dto.geometry.GeometryDTO;
import pt.app.pgmr.api.dto.geometry.CoordinateDTO;

import java.util.List;
import java.util.Objects;

/**
 * Mapper responsible for converting between JTS geometries and API geometry DTOs.
 *
 * <p>This class isolates the API layer from JTS. Controllers and DTOs should not
 * need to know how JTS geometries are constructed or interpreted.</p>
 *
 * <p>The application currently uses:</p>
 *
 * <ul>
 *     <li>Geometry type: {@code LineString}</li>
 *     <li>Coordinate reference system: EPSG:4326</li>
 *     <li>Coordinate order: longitude, latitude</li>
 * </ul>
 *
 * <p>The mapper deliberately rejects unsupported geometry types rather than
 * silently converting them. This prevents invalid spatial data from entering
 * the domain.</p>
 */
@Component
public class GeometryMapper {

    private static final int SRID = GeometryDTO.SRID;

    private final GeometryFactory geometryFactory;

    /**
     * Creates a mapper using a WGS 84 geometry factory.
     */
    public GeometryMapper() {
        this.geometryFactory = new GeometryFactory(
                new PrecisionModel(),
                SRID
        );
    }

    /**
     * Converts a JTS LineString into an API GeometryDTO.
     *
     * @param geometry JTS LineString
     * @return geometry DTO, or {@code null} when geometry is null
     * @throws IllegalArgumentException if the geometry is not EPSG:4326
     *                                  or contains invalid coordinates
     */
    public GeometryDTO toDTO(LineString geometry) {
        if (geometry == null) {
            return null;
        }

        validateSrid(geometry);
        validateLineString(geometry);

        CoordinateSequence sequence = geometry.getCoordinateSequence();

        List<CoordinateDTO> coordinates = java.util.stream.IntStream
                .range(0, sequence.size())
                .mapToObj(i -> new CoordinateDTO(
                        sequence.getX(i),
                        sequence.getY(i)
                ))
                .toList();

        GeometryDTO dto = GeometryDTO.lineString(coordinates);

        validateCoordinates(dto);

        return dto;
    }

    /**
     * Converts an API GeometryDTO into a JTS LineString.
     *
     * @param dto geometry DTO
     * @return JTS LineString, or {@code null} when the DTO is null
     * @throws IllegalArgumentException if the DTO is invalid or represents
     *                                  an unsupported geometry type
     */
    public LineString toEntity(GeometryDTO dto) {
        if (dto == null) {
            return null;
        }

        dto.validate();

        validateCoordinates(dto);

        if (!GeometryDTO.LINE_STRING.equals(dto.type())) {
            throw new IllegalArgumentException(
                    "Unsupported geometry type: " + dto.type()
            );
        }

        Coordinate[] coordinates = dto.coordinates()
                .stream()
                .map(this::toJtsCoordinate)
                .toArray(Coordinate[]::new);

        if (coordinates.length < 2) {
            throw new IllegalArgumentException(
                    "A LineString must contain at least two coordinates"
            );
        }

        LineString lineString = geometryFactory.createLineString(coordinates);
        lineString.setSRID(SRID);

        validateLineString(lineString);

        return lineString;
    }

    /**
     * Converts a DTO coordinate into a JTS coordinate.
     *
     * @param coordinate DTO coordinate
     * @return JTS coordinate
     */
    private Coordinate toJtsCoordinate(CoordinateDTO coordinate) {
        Objects.requireNonNull(
                coordinate,
                "Geometry coordinate must not be null"
        );

        coordinate.validate();

        /*
         * JTS uses X/Y coordinates.
         *
         * X = longitude
         * Y = latitude
         */
        return new Coordinate(
                coordinate.longitude(),
                coordinate.latitude()
        );
    }

    /**
     * Validates all coordinates contained in a geometry DTO.
     *
     * @param dto geometry DTO
     * @throws IllegalArgumentException if a coordinate is invalid
     */
    private void validateCoordinates(GeometryDTO dto) {
        Objects.requireNonNull(dto, "Geometry must not be null");

        if (dto.coordinates() == null || dto.coordinates().isEmpty()) {
            throw new IllegalArgumentException(
                    "Geometry must contain coordinates"
            );
        }

        dto.coordinates().forEach(coordinate -> {
            if (coordinate == null) {
                throw new IllegalArgumentException(
                        "Geometry must not contain null coordinates"
                );
            }

            coordinate.validate();
        });
    }

    /**
     * Validates the spatial reference system of a JTS geometry.
     *
     * @param geometry JTS geometry
     * @throws IllegalArgumentException if the SRID is not EPSG:4326
     */
    private void validateSrid(org.locationtech.jts.geom.Geometry geometry) {
        if (geometry.getSRID() != SRID) {
            throw new IllegalArgumentException(
                    "Geometry must use SRID " + SRID
                            + " but was " + geometry.getSRID()
            );
        }
    }

    /**
     * Validates that a LineString contains the minimum required number
     * of coordinates and is structurally valid.
     *
     * @param geometry LineString to validate
     * @throws IllegalArgumentException if the LineString is invalid
     */
    private void validateLineString(LineString geometry) {
        if (geometry.getNumPoints() < 2) {
            throw new IllegalArgumentException(
                    "A LineString must contain at least two points"
            );
        }

        if (geometry.isEmpty()) {
            throw new IllegalArgumentException(
                    "Geometry must not be empty"
            );
        }

        if (!geometry.isValid()) {
            throw new IllegalArgumentException(
                    "Geometry is not valid"
            );
        }

        for (Coordinate coordinate : geometry.getCoordinates()) {
            validateJtsCoordinate(coordinate);
        }
    }

    /**
     * Validates a JTS coordinate against WGS 84 coordinate ranges.
     *
     * @param coordinate JTS coordinate
     * @throws IllegalArgumentException if the coordinate is invalid
     */
    private void validateJtsCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException(
                    "Geometry must not contain null coordinates"
            );
        }

        if (Double.isNaN(coordinate.getX())
                || Double.isInfinite(coordinate.getX())) {
            throw new IllegalArgumentException(
                    "Longitude must be a finite number"
            );
        }

        if (Double.isNaN(coordinate.getY())
                || Double.isInfinite(coordinate.getY())) {
            throw new IllegalArgumentException(
                    "Latitude must be a finite number"
            );
        }

        if (coordinate.getX() < -180.0 || coordinate.getX() > 180.0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }

        if (coordinate.getY() < -90.0 || coordinate.getY() > 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }
    }
}
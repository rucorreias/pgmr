package pt.app.pgmr.api.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;
import pt.app.pgmr.api.dto.geometry.GeometryDTO;

import java.util.Arrays;
import java.util.List;

@Component
public class GeometryMapper {

    /**
     * Converts a JTS LineString into a GeoJSON-compatible GeometryDTO.
     *
     * @param geometry JTS LineString geometry
     * @return GeoJSON-compatible geometry DTO, or null when geometry is null
     */
    public GeometryDTO toDto(LineString geometry) {

        if (geometry == null) {
            return null;
        }

        List<List<Double>> coordinates = Arrays.stream(geometry.getCoordinates())
                .map(this::toCoordinate)
                .toList();

        return new GeometryDTO(
                "LineString",
                coordinates
        );
    }

    private List<Double> toCoordinate(Coordinate coordinate) {
        return List.of(
                coordinate.getX(),
                coordinate.getY()
        );
    }
}
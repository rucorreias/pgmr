package pt.app.pgmr.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.app.pgmr.api.dto.road.CreateRoadRequest;
import pt.app.pgmr.api.dto.road.CreateRoadSegmentRequest;
import pt.app.pgmr.api.dto.road.RoadResponse;
import pt.app.pgmr.api.dto.road.RoadSegmentResponse;
import pt.app.pgmr.api.dto.road.UpdateRoadRequest;
import pt.app.pgmr.api.dto.road.UpdateRoadSegmentRequest;
import pt.app.pgmr.api.mapper.GeometryMapper;
import pt.app.pgmr.domain.model.Road;
import pt.app.pgmr.domain.model.RoadSegment;
import pt.app.pgmr.repository.RoadRepository;
import pt.app.pgmr.repository.RoadSegmentRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service responsible for road and road-segment operations.
 *
 * <p>This service represents the application boundary for the road
 * management domain. It coordinates repositories, applies business
 * invariants and maps domain entities to API DTOs.</p>
 *
 * <p>The service intentionally does not contain GIS-specific operations.
 * Spatial queries and PostGIS-specific functionality should be introduced
 * in a dedicated spatial repository/service layer as the GIS capabilities
 * of the application evolve.</p>
 *
 * <h2>Road domain rules</h2>
 * <ul>
 *     <li>Road codes must be unique.</li>
 *     <li>Road length, when provided, cannot be negative.</li>
 *     <li>Road condition and status are independent from the state of
 *         individual segments.</li>
 * </ul>
 *
 * <h2>Road segment domain rules</h2>
 * <ul>
 *     <li>A segment must belong to an existing road.</li>
 *     <li>Segment codes are unique within their parent road.</li>
 *     <li>{@code startKm} and {@code endKm} are mandatory.</li>
 *     <li>{@code startKm} must be greater than or equal to zero.</li>
 *     <li>{@code endKm} must be greater than {@code startKm}.</li>
 *     <li>When the parent road has a known length,
 *         {@code endKm} cannot exceed that length.</li>
 *     <li>A segment has its own condition and status and does not
 *         automatically inherit the state of its parent road.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadService {

    private final RoadRepository roadRepository;
    private final RoadSegmentRepository roadSegmentRepository;
    private final GeometryMapper geometryMapper;

    /**
     * Creates a new road.
     *
     * <p>New roads use the domain defaults for condition and status
     * when these values are not explicitly supplied by the application.</p>
     *
     * @param request data required to create the road
     * @return the newly created road
     * @throws IllegalArgumentException if the road code is already in use
     */
    @Transactional
    public RoadResponse createRoad(CreateRoadRequest request) {
        validateRoadCodeIsAvailable(request.code());

        Road road = Road.builder()
                .code(request.code().trim())
                .name(request.name().trim())
                .roadType(request.roadType())
                .description(normalizeOptionalText(request.description()))
                .lengthKm(request.lengthKm())
                .build();

        validateRoadLength(road.getLengthKm());

        Road savedRoad = roadRepository.save(road);

        return toRoadResponse(savedRoad);
    }

    /**
     * Retrieves a road by its identifier.
     *
     * @param id road identifier
     * @return the requested road
     * @throws IllegalArgumentException if no road exists with the given ID
     */
    public RoadResponse getRoad(UUID id) {
        return toRoadResponse(findRoadById(id));
    }

    /**
     * Retrieves a road by its unique business code.
     *
     * @param code road business code
     * @return the requested road
     * @throws IllegalArgumentException if no road exists with the given code
     */
    public RoadResponse getRoadByCode(String code) {
        return roadRepository.findByCode(code)
                .map(this::toRoadResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Road not found with code: " + code
                        )
                );
    }

    /**
     * Retrieves all roads.
     *
     * @return all roads
     */
    public List<RoadResponse> getAllRoads() {
        return roadRepository.findAll()
                .stream()
                .map(this::toRoadResponse)
                .toList();
    }

    /**
     * Updates an existing road.
     *
     * <p>The road identifier and existing segment associations are not
     * modified by this operation.</p>
     *
     * @param id      road identifier
     * @param request new road data
     * @return the updated road
     * @throws IllegalArgumentException if the road does not exist
     * @throws IllegalArgumentException if the new code is already used
     *                                  by another road
     */
    @Transactional
    public RoadResponse updateRoad(UUID id, UpdateRoadRequest request) {
        Road road = findRoadById(id);

        if (request.code() != null
                && !request.code().equals(road.getCode())) {

            validateRoadCodeIsAvailableForUpdate(request.code(), id);
            road.setCode(request.code().trim());
        }

        if (request.name() != null) {
            road.setName(request.name().trim());
        }

        if (request.roadType() != null) {
            road.setRoadType(request.roadType());
        }

        if (request.description() != null) {
            road.setDescription(normalizeOptionalText(request.description()));
        }

        if (request.lengthKm() != null) {
            validateRoadLength(request.lengthKm());
            validateRoadLengthAgainstSegments(road, request.lengthKm());
            road.setLengthKm(request.lengthKm());
        }

        if (request.condition() != null) {
            road.setCondition(request.condition());
        }

        if (request.status() != null) {
            road.setStatus(request.status());
        }

        road.setUpdatedAt(OffsetDateTime.now());

        return toRoadResponse(roadRepository.save(road));
    }

    /**
     * Deletes a road.
     *
     * <p>Deletion cascades to its road segments at database level,
     * according to the foreign-key definition.</p>
     *
     * @param id road identifier
     * @throws IllegalArgumentException if the road does not exist
     */
    @Transactional
    public void deleteRoad(UUID id) {
        Road road = findRoadById(id);
        roadRepository.delete(road);
    }

    /**
     * Creates a new segment belonging to a road.
     *
     * <p>The parent road is identified by {@code roadId}; consequently,
     * the create-segment request does not need to contain a road ID.</p>
     *
     * @param roadId  parent road identifier
     * @param request segment creation data
     * @return the newly created segment
     * @throws IllegalArgumentException if the road does not exist
     * @throws IllegalArgumentException if the segment code already exists
     *                                  within the road
     * @throws IllegalArgumentException if the kilometer range is invalid
     */
    @Transactional
    public RoadSegmentResponse createRoadSegment(
            UUID roadId,
            CreateRoadSegmentRequest request
    ) {
        Road road = findRoadById(roadId);

        validateSegmentCodeIsAvailable(roadId, request.code());
        validateSegmentKilometerRange(
                request.startKm(),
                request.endKm()
        );
        validateSegmentFitsRoad(
                road,
                request.startKm(),
                request.endKm()
        );

        RoadSegment segment = RoadSegment.builder()
                .road(road)
                .code(request.code().trim())
                .name(normalizeOptionalText(request.name()))
                .startKm(request.startKm())
                .endKm(request.endKm())
                .condition(
                        request.condition() != null
                                ? request.condition()
                                : road.getCondition()
                )
                .status(
                        request.status() != null
                                ? request.status()
                                : road.getStatus()
                )
                .build();

        return toRoadSegmentResponse(
                roadSegmentRepository.save(segment)
        );
    }

    /**
     * Retrieves a road segment by its identifier.
     *
     * @param id segment identifier
     * @return the requested segment
     * @throws IllegalArgumentException if no segment exists with the ID
     */
    public RoadSegmentResponse getRoadSegment(UUID id) {
        return toRoadSegmentResponse(findRoadSegmentById(id));
    }

    /**
     * Retrieves all segments belonging to a road.
     *
     * @param roadId parent road identifier
     * @return the road's segments
     * @throws IllegalArgumentException if the road does not exist
     */
    public List<RoadSegmentResponse> getRoadSegments(UUID roadId) {
        findRoadById(roadId);

        return roadSegmentRepository.findByRoadId(roadId)
                .stream()
                .map(this::toRoadSegmentResponse)
                .toList();
    }

    /**
     * Updates an existing road segment.
     *
     * <p>The parent road cannot be changed through this operation.
     * Moving a segment between roads, if ever required, should be an
     * explicit domain operation rather than an accidental side effect
     * of a generic update.</p>
     *
     * @param id      segment identifier
     * @param request new segment data
     * @return the updated segment
     * @throws IllegalArgumentException if the segment does not exist
     * @throws IllegalArgumentException if the kilometer range is invalid
     * @throws IllegalArgumentException if the new range exceeds the road
     *                                  length
     * @throws IllegalArgumentException if the new code is already used
     *                                  by another segment of the same road
     */
    @Transactional
    public RoadSegmentResponse updateRoadSegment(
            UUID id,
            UpdateRoadSegmentRequest request
    ) {
        RoadSegment segment = findRoadSegmentById(id);
        Road road = segment.getRoad();

        if (request.code() != null
                && !request.code().equals(segment.getCode())) {

            validateSegmentCodeIsAvailableForUpdate(
                    road.getId(),
                    request.code(),
                    id
            );

            segment.setCode(request.code().trim());
        }

        if (request.name() != null) {
            segment.setName(normalizeOptionalText(request.name()));
        }

        BigDecimal newStartKm =
                request.startKm() != null
                        ? request.startKm()
                        : segment.getStartKm();

        BigDecimal newEndKm =
                request.endKm() != null
                        ? request.endKm()
                        : segment.getEndKm();

        if (request.startKm() != null
                || request.endKm() != null) {

            validateSegmentKilometerRange(
                    newStartKm,
                    newEndKm
            );

            validateSegmentFitsRoad(
                    road,
                    newStartKm,
                    newEndKm
            );

            segment.setStartKm(newStartKm);
            segment.setEndKm(newEndKm);
        }

        if (request.condition() != null) {
            segment.setCondition(request.condition());
        }

        if (request.status() != null) {
            segment.setStatus(request.status());
        }

        segment.setUpdatedAt(OffsetDateTime.now());

        return toRoadSegmentResponse(
                roadSegmentRepository.save(segment)
        );
    }

    /**
     * Deletes a road segment.
     *
     * @param id segment identifier
     * @throws IllegalArgumentException if the segment does not exist
     */
    @Transactional
    public void deleteRoadSegment(UUID id) {
        RoadSegment segment = findRoadSegmentById(id);
        roadSegmentRepository.delete(segment);
    }

    /**
     * Finds a road by its identifier.
     *
     * @param id road identifier
     * @return managed road entity
     * @throws IllegalArgumentException if the road does not exist
     */
    private Road findRoadById(UUID id) {
        return roadRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Road not found with id: " + id
                        )
                );
    }

    /**
     * Finds a road segment by its identifier.
     *
     * @param id segment identifier
     * @return managed road segment entity
     * @throws IllegalArgumentException if the segment does not exist
     */
    private RoadSegment findRoadSegmentById(UUID id) {
        return roadSegmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Road segment not found with id: " + id
                        )
                );
    }

    /**
     * Validates that a road code is not already registered.
     *
     * @param code road code
     */
    private void validateRoadCodeIsAvailable(String code) {
        if (roadRepository.existsByCode(code)) {
            throw new IllegalArgumentException(
                    "Road code already exists: " + code
            );
        }
    }

    /**
     * Validates road code uniqueness during an update.
     *
     * @param code          proposed road code
     * @param currentRoadId current road identifier
     */
    private void validateRoadCodeIsAvailableForUpdate(
            String code,
            UUID currentRoadId
    ) {
        roadRepository.findByCode(code)
                .filter(existingRoad ->
                        !existingRoad.getId().equals(currentRoadId))
                .ifPresent(existingRoad -> {
                    throw new IllegalArgumentException(
                            "Road code already exists: " + code
                    );
                });
    }

    /**
     * Validates that a segment code is unique within its parent road.
     *
     * @param roadId parent road identifier
     * @param code   segment code
     */
    private void validateSegmentCodeIsAvailable(
            UUID roadId,
            String code
    ) {
        if (roadSegmentRepository.existsByRoadIdAndCode(roadId, code)) {
            throw new IllegalArgumentException(
                    "Road segment code already exists for this road: "
                            + code
            );
        }
    }

    /**
     * Validates segment code uniqueness during an update.
     *
     * @param roadId           parent road identifier
     * @param code             proposed segment code
     * @param currentSegmentId current segment identifier
     */
    private void validateSegmentCodeIsAvailableForUpdate(
            UUID roadId,
            String code,
            UUID currentSegmentId
    ) {
        roadSegmentRepository.findByRoadIdAndCode(roadId, code)
                .filter(existingSegment ->
                        !existingSegment.getId().equals(currentSegmentId))
                .ifPresent(existingSegment -> {
                    throw new IllegalArgumentException(
                            "Road segment code already exists for this road: "
                                    + code
                    );
                });
    }

    /**
     * Validates that a road length is not negative.
     *
     * @param lengthKm road length
     */
    private void validateRoadLength(BigDecimal lengthKm) {
        if (lengthKm != null && lengthKm.signum() < 0) {
            throw new IllegalArgumentException(
                    "Road length cannot be negative"
            );
        }
    }

    /**
     * Validates the kilometer range of a segment.
     *
     * <p>The domain requires:</p>
     *
     * <pre>
     * 0 <= startKm < endKm
     * </pre>
     *
     * @param startKm segment start
     * @param endKm   segment end
     */
    private void validateSegmentKilometerRange(
            BigDecimal startKm,
            BigDecimal endKm
    ) {
        if (startKm == null || endKm == null) {
            throw new IllegalArgumentException(
                    "Segment startKm and endKm are required"
            );
        }

        if (startKm.signum() < 0) {
            throw new IllegalArgumentException(
                    "Segment startKm cannot be negative"
            );
        }

        if (endKm.signum() < 0) {
            throw new IllegalArgumentException(
                    "Segment endKm cannot be negative"
            );
        }

        if (endKm.compareTo(startKm) <= 0) {
            throw new IllegalArgumentException(
                    "Segment endKm must be greater than startKm"
            );
        }
    }

    /**
     * Validates that a segment is contained within its parent road's
     * kilometer range.
     *
     * <p>If the road length is unknown, the validation cannot be applied
     * and the segment range is accepted.</p>
     *
     * @param road    parent road
     * @param startKm segment start
     * @param endKm   segment end
     */
    private void validateSegmentFitsRoad(
            Road road,
            BigDecimal startKm,
            BigDecimal endKm
    ) {
        if (road.getLengthKm() == null) {
            return;
        }

        if (startKm.compareTo(road.getLengthKm()) > 0) {
            throw new IllegalArgumentException(
                    "Segment startKm cannot exceed road length"
            );
        }

        if (endKm.compareTo(road.getLengthKm()) > 0) {
            throw new IllegalArgumentException(
                    "Segment endKm cannot exceed road length"
            );
        }
    }

    /**
     * Validates that reducing a road's length does not make any existing
     * segment invalid.
     *
     * @param road        road being updated
     * @param newLengthKm proposed new length
     */
    private void validateRoadLengthAgainstSegments(
            Road road,
            BigDecimal newLengthKm
    ) {
        if (newLengthKm == null) {
            return;
        }

        roadSegmentRepository.findByRoadId(road.getId())
                .stream()
                .filter(segment ->
                        segment.getEndKm() != null
                                && segment.getEndKm()
                                .compareTo(newLengthKm) > 0)
                .findFirst()
                .ifPresent(segment -> {
                    throw new IllegalArgumentException(
                            "Road length cannot be reduced below the endKm "
                                    + "of existing segment: "
                                    + segment.getCode()
                    );
                });
    }

    /**
     * Normalizes optional text values.
     *
     * @param value input value
     * @return trimmed value or {@code null}
     */
    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    /**
     * Maps a Road entity to its API representation.
     *
     * @param road road entity
     * @return road response
     */
    private RoadResponse toRoadResponse(Road road) {
        return new RoadResponse(
                road.getId(),
                road.getCode(),
                road.getName(),
                road.getRoadType(),
                road.getDescription(),
                road.getLengthKm(),
                road.getCondition(),
                road.getStatus(),
                road.getCreatedAt(),
                road.getUpdatedAt()
        );
    }

    /**
     * Maps a RoadSegment entity to its API representation.
     *
     * @param segment road segment entity
     * @return road segment response
     */
    private RoadSegmentResponse toRoadSegmentResponse(
            RoadSegment segment
    ) {
        return new RoadSegmentResponse(
                segment.getId(),
                segment.getRoad().getId(),
                segment.getCode(),
                segment.getName(),
                segment.getStartKm(),
                segment.getEndKm(),
                geometryMapper.toDto(segment.getGeometry()),
                segment.getCondition(),
                segment.getStatus(),
                segment.getCreatedAt(),
                segment.getUpdatedAt()
        );
    }
}
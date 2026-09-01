package pt.app.pgmr.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.app.pgmr.api.dto.road.CreateRoadSegmentRequestDTO;
import pt.app.pgmr.api.dto.road.RoadSegmentResponseDTO;
import pt.app.pgmr.api.dto.road.UpdateRoadSegmentRequestDTO;
import pt.app.pgmr.application.exception.DomainValidationException;
import pt.app.pgmr.application.exception.ResourceNotFoundException;
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
 * Application service for road segment lifecycle operations.
 *
 * <p>This service enforces the segment domain invariants, including parent-road
 * validation, unique segment codes within a road and kilometer-range
 * consistency before persisting changes.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadSegmentService {

    private final RoadRepository roadRepository;
    private final RoadSegmentRepository roadSegmentRepository;
    private final GeometryMapper geometryMapper;

    /**
     * Creates a segment and associates it with an existing road.
     *
     * @param roadId target road identifier
     * @param request segment creation payload
     * @return created segment response
     */
    @Transactional
    public RoadSegmentResponseDTO createRoadSegment(UUID roadId, CreateRoadSegmentRequestDTO request) {
        if (request == null) {
            throw new DomainValidationException("Road segment payload is required");
        }

        Road road = findRoadById(roadId);
        String normalizedCode = normalizeCode(request.code());

        validateSegmentCodeIsAvailable(roadId, normalizedCode);
        validateSegmentKilometerRange(request.startKm(), request.endKm());
        validateSegmentFitsRoad(road, request.startKm(), request.endKm());
        validateNoSegmentOverlap(roadId, request.startKm(), request.endKm(), null);

        RoadSegment segment = RoadSegment.builder()
                .road(road)
                .code(normalizedCode)
                .name(normalizeOptionalText(request.name()))
                .startKm(request.startKm())
                .endKm(request.endKm())
                .condition(request.condition() != null ? request.condition() : road.getCondition())
                .status(request.status() != null ? request.status() : road.getStatus())
                .build();

        return toRoadSegmentResponse(roadSegmentRepository.save(segment));
    }

    /**
     * Retrieves a segment by its identifier.
     *
     * @param id segment identifier
     * @return the segment response
     */
    public RoadSegmentResponseDTO getRoadSegment(UUID id) {
        return toRoadSegmentResponse(findRoadSegmentById(id));
    }

    /**
     * Retrieves a specific segment associated with a road.
     *
     * @param roadId parent road identifier
     * @param segmentId segment identifier
     * @return the segment response
     */
    public RoadSegmentResponseDTO getRoadSegmentById(UUID roadId, UUID segmentId) {
        findRoadById(roadId);
        RoadSegment segment = findRoadSegmentById(segmentId);

        if (!segment.getRoad().getId().equals(roadId)) {
            throw new ResourceNotFoundException(
                    "Road segment not found for road id: " + roadId + " and segment id: " + segmentId
            );
        }

        return toRoadSegmentResponse(segment);
    }

    /**
     * Retrieves all segments for a specific road.
     *
     * @param roadId parent road identifier
     * @return list of segment responses
     */
    public List<RoadSegmentResponseDTO> getRoadSegments(UUID roadId) {
        findRoadById(roadId);

        return roadSegmentRepository.findByRoadId(roadId)
                .stream()
                .map(this::toRoadSegmentResponse)
                .toList();
    }

    /**
     * Updates an existing segment while ensuring it belongs to the provided road.
     *
     * @param roadId parent road identifier
     * @param segmentId segment identifier
     * @param request segment update payload
     * @return updated segment response
     */
    @Transactional
    public RoadSegmentResponseDTO updateRoadSegment(
            UUID roadId,
            UUID segmentId,
            UpdateRoadSegmentRequestDTO request
    ) {
        if (request == null) {
            throw new DomainValidationException("Road segment payload is required");
        }

        RoadSegment segment = findRoadSegmentByIdInRoad(roadId, segmentId);
        Road road = segment.getRoad();

        if (request.code() != null && !request.code().equals(segment.getCode())) {
            String normalizedCode = normalizeCode(request.code());
            validateSegmentCodeIsAvailableForUpdate(road.getId(), normalizedCode, segmentId);
            segment.setCode(normalizedCode);
        }

        if (request.name() != null) {
            segment.setName(normalizeOptionalText(request.name()));
        }

        BigDecimal newStartKm = request.startKm() != null ? request.startKm() : segment.getStartKm();
        BigDecimal newEndKm = request.endKm() != null ? request.endKm() : segment.getEndKm();

        if (request.startKm() != null || request.endKm() != null) {
            validateSegmentKilometerRange(newStartKm, newEndKm);
            validateSegmentFitsRoad(road, newStartKm, newEndKm);
            validateNoSegmentOverlap(road.getId(), newStartKm, newEndKm, segmentId);
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

        return toRoadSegmentResponse(roadSegmentRepository.save(segment));
    }

    /**
     * Deletes a segment associated with a specific road.
     *
     * @param roadId parent road identifier
     * @param segmentId segment identifier
     */
    @Transactional
    public void deleteRoadSegment(UUID roadId, UUID segmentId) {
        RoadSegment segment = findRoadSegmentByIdInRoad(roadId, segmentId);
        roadSegmentRepository.delete(segment);
    }

    /**
     * Deletes a segment by identifier.
     *
     * @param id segment identifier
     */
    @Transactional
    public void deleteRoadSegment(UUID id) {
        RoadSegment segment = findRoadSegmentById(id);
        roadSegmentRepository.delete(segment);
    }

    private Road findRoadById(UUID id) {
        return roadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Road not found with id: " + id));
    }

    private RoadSegment findRoadSegmentById(UUID id) {
        return roadSegmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Road segment not found with id: " + id));
    }

    private RoadSegment findRoadSegmentByIdInRoad(UUID roadId, UUID segmentId) {
        RoadSegment segment = findRoadSegmentById(segmentId);
        if (!segment.getRoad().getId().equals(roadId)) {
            throw new ResourceNotFoundException(
                    "Road segment not found for road id: " + roadId + " and segment id: " + segmentId
            );
        }
        return segment;
    }

    private void validateSegmentCodeIsAvailable(UUID roadId, String code) {
        if (roadSegmentRepository.existsByRoadIdAndCode(roadId, code)) {
            throw new DomainValidationException(
                    "Road segment code already exists for this road: " + code
            );
        }
    }

    private void validateSegmentCodeIsAvailableForUpdate(
            UUID roadId,
            String code,
            UUID currentSegmentId
    ) {
        roadSegmentRepository.findByRoadIdAndCode(roadId, code)
                .filter(existingSegment -> !existingSegment.getId().equals(currentSegmentId))
                .ifPresent(existingSegment -> {
                    throw new DomainValidationException(
                            "Road segment code already exists for this road: " + code
                    );
                });
    }

    private void validateSegmentKilometerRange(BigDecimal startKm, BigDecimal endKm) {
        if (startKm == null || endKm == null) {
            throw new DomainValidationException("Segment startKm and endKm are required");
        }

        if (startKm.signum() < 0) {
            throw new DomainValidationException("Segment startKm cannot be negative");
        }

        if (endKm.signum() < 0) {
            throw new DomainValidationException("Segment endKm cannot be negative");
        }

        if (endKm.compareTo(startKm) <= 0) {
            throw new DomainValidationException("Segment endKm must be greater than startKm");
        }
    }

    private void validateSegmentFitsRoad(Road road, BigDecimal startKm, BigDecimal endKm) {
        if (road.getLengthKm() == null) {
            return;
        }

        if (startKm.compareTo(road.getLengthKm()) > 0) {
            throw new DomainValidationException("Segment startKm cannot exceed road length");
        }

        if (endKm.compareTo(road.getLengthKm()) > 0) {
            throw new DomainValidationException("Segment endKm cannot exceed road length");
        }
    }

    private void validateNoSegmentOverlap(
            UUID roadId,
            BigDecimal startKm,
            BigDecimal endKm,
            UUID currentSegmentId
    ) {
        roadSegmentRepository.findByRoadId(roadId)
                .stream()
                .filter(existingSegment -> !existingSegment.getId().equals(currentSegmentId))
                .filter(existingSegment -> existingSegment.getStartKm() != null
                        && existingSegment.getEndKm() != null)
                .filter(existingSegment -> startKm.compareTo(existingSegment.getEndKm()) < 0
                        && endKm.compareTo(existingSegment.getStartKm()) > 0)
                .findFirst()
                .ifPresent(existingSegment -> {
                    throw new DomainValidationException(
                            "Road segment overlaps with existing segment: " + existingSegment.getCode()
                    );
                });
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new DomainValidationException("Road segment code cannot be null");
        }

        String normalized = code.trim();
        if (normalized.isEmpty()) {
            throw new DomainValidationException("Road segment code cannot be blank");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private RoadSegmentResponseDTO toRoadSegmentResponse(RoadSegment segment) {
        return new RoadSegmentResponseDTO(
                segment.getId(),
                segment.getRoad().getId(),
                segment.getCode(),
                segment.getName(),
                segment.getStartKm(),
                segment.getEndKm(),
                geometryMapper.toDTO(segment.getGeometry()),
                segment.getCondition(),
                segment.getStatus(),
                segment.getCreatedAt(),
                segment.getUpdatedAt()
        );
    }
}

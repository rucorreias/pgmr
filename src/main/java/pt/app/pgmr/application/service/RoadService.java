package pt.app.pgmr.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.app.pgmr.api.dto.road.CreateRoadRequestDTO;
import pt.app.pgmr.api.dto.road.RoadResponseDTO;
import pt.app.pgmr.api.dto.road.UpdateRoadRequestDTO;
import pt.app.pgmr.domain.model.Road;
import pt.app.pgmr.repository.RoadRepository;
import pt.app.pgmr.repository.RoadSegmentRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for road lifecycle operations.
 *
 * <p>This service handles creation, retrieval, update and deletion of road
 * aggregate records, while keeping segment-specific validations on the parent
 * road length constraints in the same domain boundary.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadService {

    private final RoadRepository roadRepository;
    private final RoadSegmentRepository roadSegmentRepository;

    /**
     * Creates a new road.
     *
     * @param request road creation payload
     * @return the created road response
     */
    @Transactional
    public RoadResponseDTO createRoad(CreateRoadRequestDTO request) {
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
     * Retrieves a road by identifier.
     *
     * @param id road identifier
     * @return the road response
     */
    public RoadResponseDTO getRoadById(UUID id) {
        return toRoadResponse(findRoadById(id));
    }

    /**
     * Retrieves a road by its unique business code.
     *
     * @param code road code
     * @return the road response
     */
    public RoadResponseDTO getRoadByCode(String code) {
        return roadRepository.findByCode(code)
                .map(this::toRoadResponse)
                .orElseThrow(() -> new IllegalArgumentException("Road not found with code: " + code));
    }

    /**
     * Retrieves all roads.
     *
     * @return all roads in the repository
     */
    public List<RoadResponseDTO> getAllRoads() {
        return roadRepository.findAll()
                .stream()
                .map(this::toRoadResponse)
                .toList();
    }

    /**
     * Updates an existing road.
     *
     * @param id road identifier
     * @param request road update payload
     * @return the updated road response
     */
    @Transactional
    public RoadResponseDTO updateRoad(UUID id, UpdateRoadRequestDTO request) {
        Road road = findRoadById(id);

        if (request.code() != null && !request.code().equals(road.getCode())) {
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
     * Deletes a road and all of its dependent segments through cascade rules.
     *
     * @param id road identifier
     */
    @Transactional
    public void deleteRoad(UUID id) {
        Road road = findRoadById(id);
        roadRepository.delete(road);
    }

    private Road findRoadById(UUID id) {
        return roadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road not found with id: " + id));
    }

    private void validateRoadCodeIsAvailable(String code) {
        if (roadRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Road code already exists: " + code);
        }
    }

    private void validateRoadCodeIsAvailableForUpdate(String code, UUID currentRoadId) {
        roadRepository.findByCode(code)
                .filter(existingRoad -> !existingRoad.getId().equals(currentRoadId))
                .ifPresent(existingRoad -> {
                    throw new IllegalArgumentException("Road code already exists: " + code);
                });
    }

    private void validateRoadLength(BigDecimal lengthKm) {
        if (lengthKm != null && lengthKm.signum() < 0) {
            throw new IllegalArgumentException("Road length cannot be negative");
        }
    }

    private void validateRoadLengthAgainstSegments(Road road, BigDecimal newLengthKm) {
        if (newLengthKm == null) {
            return;
        }

        roadSegmentRepository.findByRoadId(road.getId())
                .stream()
                .filter(segment -> segment.getEndKm() != null && segment.getEndKm().compareTo(newLengthKm) > 0)
                .findFirst()
                .ifPresent(segment -> {
                    throw new IllegalArgumentException(
                            "Road length cannot be reduced below the endKm of existing segment: " + segment.getCode()
                    );
                });
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private RoadResponseDTO toRoadResponse(Road road) {
        return new RoadResponseDTO(
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
}

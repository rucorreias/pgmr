package pt.app.pgmr.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.app.pgmr.api.dto.road.CreateRoadSegmentRequestDTO;
import pt.app.pgmr.api.dto.road.RoadSegmentResponseDTO;
import pt.app.pgmr.api.dto.road.UpdateRoadSegmentRequestDTO;
import pt.app.pgmr.application.service.RoadSegmentService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing road segments.
 *
 * <p>Segment endpoints are isolated from the road controller so that road and
 * segment lifecycle concerns remain separate and easier to evolve.</p>
 */
@RestController
@RequestMapping("/api/v1/roads")
@RequiredArgsConstructor
public class RoadSegmentController {

    private final RoadSegmentService roadSegmentService;

    /**
     * Creates a new segment under a road.
     *
     * @param roadId parent road identifier
     * @param request segment creation payload
     * @return created segment response with HTTP 201
     */
    @PostMapping("/{roadId}/segments")
    public ResponseEntity<RoadSegmentResponseDTO> createRoadSegment(
            @PathVariable UUID roadId,
            @Valid @RequestBody CreateRoadSegmentRequestDTO request
    ) {
        RoadSegmentResponseDTO response = roadSegmentService.createRoadSegment(roadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all segments for a road.
     *
     * @param roadId parent road identifier
     * @return list of segment responses
     */
    @GetMapping("/{roadId}/segments")
    public ResponseEntity<List<RoadSegmentResponseDTO>> getRoadSegments(@PathVariable UUID roadId) {
        return ResponseEntity.ok(roadSegmentService.getRoadSegments(roadId));
    }

    /**
     * Retrieves one segment by road and segment identifiers.
     *
     * @param roadId parent road identifier
     * @param segmentId segment identifier
     * @return the segment response
     */
    @GetMapping("/{roadId}/segments/{segmentId}")
    public ResponseEntity<RoadSegmentResponseDTO> getRoadSegmentById(
            @PathVariable UUID roadId,
            @PathVariable UUID segmentId
    ) {
        return ResponseEntity.ok(roadSegmentService.getRoadSegmentById(roadId, segmentId));
    }

    /**
     * Updates a segment for a road.
     *
     * @param roadId parent road identifier
     * @param segmentId segment identifier
     * @param request segment update payload
     * @return the updated segment response
     */
    @PutMapping("/{roadId}/segments/{segmentId}")
    public ResponseEntity<RoadSegmentResponseDTO> updateRoadSegment(
            @PathVariable UUID roadId,
            @PathVariable UUID segmentId,
            @Valid @RequestBody UpdateRoadSegmentRequestDTO request
    ) {
        return ResponseEntity.ok(roadSegmentService.updateRoadSegment(roadId, segmentId, request));
    }

    /**
     * Deletes a segment from a road.
     *
     * @param roadId parent road identifier
     * @param segmentId segment identifier
     * @return HTTP 204 response
     */
    @DeleteMapping("/{roadId}/segments/{segmentId}")
    public ResponseEntity<Void> deleteRoadSegment(@PathVariable UUID roadId, @PathVariable UUID segmentId) {
        roadSegmentService.deleteRoadSegment(roadId, segmentId);
        return ResponseEntity.noContent().build();
    }
}

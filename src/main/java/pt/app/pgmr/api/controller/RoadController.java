package pt.app.pgmr.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.app.pgmr.api.dto.road.CreateRoadRequestDTO;
import pt.app.pgmr.api.dto.road.RoadResponseDTO;
import pt.app.pgmr.api.dto.road.UpdateRoadRequestDTO;
import pt.app.pgmr.application.service.RoadService;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing roads.
 *
 * <p>This controller exposes the public road endpoints and delegates business
 * workflow to {@link RoadService}.</p>
 */
@RestController
@RequestMapping("/api/v1/roads")
@RequiredArgsConstructor
public class RoadController {

    private final RoadService roadService;

    /**
     * Creates a new road.
     *
     * @param request road creation payload
     * @return created road response with HTTP 201
     */
    @PostMapping
    public ResponseEntity<RoadResponseDTO> createRoad(@Valid @RequestBody CreateRoadRequestDTO request) {
        RoadResponseDTO response = roadService.createRoad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all roads.
     *
     * @return list of road responses
     */
    @GetMapping
    public ResponseEntity<List<RoadResponseDTO>> getAllRoads() {
        return ResponseEntity.ok(roadService.getAllRoads());
    }

    /**
     * Retrieves a road by identifier.
     *
     * @param roadId road identifier
     * @return the road response
     */
    @GetMapping("/{roadId}")
    public ResponseEntity<RoadResponseDTO> getRoadById(@PathVariable UUID roadId) {
        return ResponseEntity.ok(roadService.getRoadById(roadId));
    }

    /**
     * Updates an existing road.
     *
     * @param roadId road identifier
     * @param request road update payload
     * @return the updated road response
     */
    @PutMapping("/{roadId}")
    public ResponseEntity<RoadResponseDTO> updateRoad(
            @PathVariable UUID roadId,
            @Valid @RequestBody UpdateRoadRequestDTO request
    ) {
        return ResponseEntity.ok(roadService.updateRoad(roadId, request));
    }

    /**
     * Deletes a road by identifier.
     *
     * @param roadId road identifier
     * @return HTTP 204 response
     */
    @DeleteMapping("/{roadId}")
    public ResponseEntity<Void> deleteRoad(@PathVariable UUID roadId) {
        roadService.deleteRoad(roadId);
        return ResponseEntity.noContent().build();
    }
}

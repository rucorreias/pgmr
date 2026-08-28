package pt.app.pgmr.domain.model;

import jakarta.persistence.*;
import lombok.*;
import pt.app.pgmr.domain.model.enums.RoadCondition;
import pt.app.pgmr.domain.model.enums.RoadStatus;
import pt.app.pgmr.domain.model.enums.RoadType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "road_type", nullable = false, length = 30)
    private RoadType roadType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "length_km", precision = 10, scale = 3)
    private BigDecimal lengthKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RoadCondition condition = RoadCondition.GOOD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RoadStatus status = RoadStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(
            mappedBy = "road",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<RoadSegment> segments = new ArrayList<>();
}
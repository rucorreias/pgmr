package pt.app.pgmr.domain.model;

import jakarta.persistence.*;
import lombok.*;
import pt.app.pgmr.domain.model.enums.RoadCondition;
import pt.app.pgmr.domain.model.enums.RoadStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "road_segments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_road_segment_code",
                        columnNames = {"road_id", "code"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "road_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_road_segments_road")
    )
    private Road road;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String name;

    @Column(name = "start_km", precision = 10, scale = 3)
    private BigDecimal startKm;

    @Column(name = "end_km", precision = 10, scale = 3)
    private BigDecimal endKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoadCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoadStatus status;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
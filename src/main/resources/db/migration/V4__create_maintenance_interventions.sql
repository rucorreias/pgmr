CREATE TABLE maintenance_interventions
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    road_id            UUID,
    road_segment_id    UUID,
    incident_id        UUID,
    assigned_to        UUID,

    title              VARCHAR(255) NOT NULL,
    description        TEXT,

    intervention_type  VARCHAR(30)  NOT NULL,
    priority           VARCHAR(30)  NOT NULL DEFAULT 'MEDIUM',
    status             VARCHAR(30)  NOT NULL DEFAULT 'PLANNED',

    planned_start_date DATE,
    planned_end_date   DATE,

    actual_start_date  DATE,
    actual_end_date    DATE,

    estimated_cost     DECIMAL(12, 2),
    actual_cost        DECIMAL(12, 2),

    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_interventions_road
        FOREIGN KEY (road_id)
            REFERENCES roads (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_interventions_road_segment
        FOREIGN KEY (road_segment_id)
            REFERENCES road_segments (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_interventions_incident
        FOREIGN KEY (incident_id)
            REFERENCES incidents (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_interventions_assigned_to
        FOREIGN KEY (assigned_to)
            REFERENCES users (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_intervention_type
        CHECK (
            intervention_type IN (
                                  'PREVENTIVE',
                                  'CORRECTIVE'
                )
            ),

    CONSTRAINT chk_intervention_priority
        CHECK (
            priority IN (
                         'LOW',
                         'MEDIUM',
                         'HIGH',
                         'CRITICAL'
                )
            ),

    CONSTRAINT chk_intervention_status
        CHECK (
            status IN (
                       'PLANNED',
                       'IN_PROGRESS',
                       'COMPLETED',
                       'CANCELLED'
                )
            ),

    CONSTRAINT chk_estimated_cost
        CHECK (
            estimated_cost IS NULL
                OR estimated_cost >= 0
            ),

    CONSTRAINT chk_actual_cost
        CHECK (
            actual_cost IS NULL
                OR actual_cost >= 0
            ),

    CONSTRAINT chk_planned_dates
        CHECK (
            planned_start_date IS NULL
                OR planned_end_date IS NULL
                OR planned_end_date >= planned_start_date
            ),

    CONSTRAINT chk_actual_dates
        CHECK (
            actual_start_date IS NULL
                OR actual_end_date IS NULL
                OR actual_end_date >= actual_start_date
            )
);


CREATE INDEX idx_interventions_road_id
    ON maintenance_interventions (road_id);

CREATE INDEX idx_interventions_road_segment_id
    ON maintenance_interventions (road_segment_id);

CREATE INDEX idx_interventions_incident_id
    ON maintenance_interventions (incident_id);

CREATE INDEX idx_interventions_assigned_to
    ON maintenance_interventions (assigned_to);

CREATE INDEX idx_interventions_status
    ON maintenance_interventions (status);

CREATE INDEX idx_interventions_priority
    ON maintenance_interventions (priority);
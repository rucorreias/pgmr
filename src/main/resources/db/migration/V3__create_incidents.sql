CREATE TABLE incidents (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           road_id BIGINT,
                           road_segment_id BIGINT,
                           reported_by BIGINT,
                           title VARCHAR(255) NOT NULL,
                           description TEXT,
                           incident_type VARCHAR(50) NOT NULL,
                           severity VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
                           status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
                           latitude DECIMAL(9, 6),
                           longitude DECIMAL(9, 6),
                           reported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           resolved_at TIMESTAMPTZ,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_incidents_road
                               FOREIGN KEY (road_id)
                                   REFERENCES roads(id)
                                   ON DELETE SET NULL,

                           CONSTRAINT fk_incidents_road_segment
                               FOREIGN KEY (road_segment_id)
                                   REFERENCES road_segments(id)
                                   ON DELETE SET NULL,

                           CONSTRAINT fk_incidents_reported_by
                               FOREIGN KEY (reported_by)
                                   REFERENCES users(id)
                                   ON DELETE SET NULL,

                           CONSTRAINT chk_incident_severity
                               CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

                           CONSTRAINT chk_incident_status
                               CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),

                           CONSTRAINT chk_incident_latitude
                               CHECK (
                                   latitude IS NULL
                                       OR latitude BETWEEN -90 AND 90
                                   ),

                           CONSTRAINT chk_incident_longitude
                               CHECK (
                                   longitude IS NULL
                                       OR longitude BETWEEN -180 AND 180
                                   )
);

CREATE INDEX idx_incidents_road_id
    ON incidents(road_id);

CREATE INDEX idx_incidents_road_segment_id
    ON incidents(road_segment_id);

CREATE INDEX idx_incidents_status
    ON incidents(status);

CREATE INDEX idx_incidents_severity
    ON incidents(severity);

CREATE INDEX idx_incidents_reported_at
    ON incidents(reported_at);
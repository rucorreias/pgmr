CREATE TABLE roads
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,

    road_type   VARCHAR(50)  NOT NULL,

    description TEXT,

    length_km   DECIMAL(10, 3),

    condition   VARCHAR(30)  NOT NULL DEFAULT 'GOOD',
    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_roads_length
        CHECK (
            length_km IS NULL
                OR length_km >= 0
            ),

    CONSTRAINT chk_roads_condition
        CHECK (
            condition IN (
                          'GOOD',
                          'FAIR',
                          'POOR',
                          'CRITICAL'
                )
            ),

    CONSTRAINT chk_roads_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE',
                       'UNDER_CONSTRUCTION',
                       'PERMANENTLY_CLOSED'
                )
            )
);


CREATE TABLE road_segments
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),

    road_id    UUID           NOT NULL,

    code       VARCHAR(50)    NOT NULL,
    name       VARCHAR(255),

    start_km   DECIMAL(10, 3) NOT NULL,
    end_km     DECIMAL(10, 3) NOT NULL,

    geometry   geometry(LineString, 4326),

    condition  VARCHAR(30)    NOT NULL DEFAULT 'GOOD',
    status     VARCHAR(30)    NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_road_segments_road
        FOREIGN KEY (road_id)
            REFERENCES roads (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_road_segment_code
        UNIQUE (road_id, code),

    CONSTRAINT chk_segment_start_km
        CHECK (start_km >= 0),

    CONSTRAINT chk_segment_end_km
        CHECK (end_km >= 0),

    CONSTRAINT chk_segment_km_range
        CHECK (end_km > start_km),

    CONSTRAINT chk_segment_condition
        CHECK (
            condition IN (
                          'GOOD',
                          'FAIR',
                          'POOR',
                          'CRITICAL'
                )
            ),

    CONSTRAINT chk_segment_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE',
                       'UNDER_CONSTRUCTION',
                       'PERMANENTLY_CLOSED'
                )
            )
);


CREATE INDEX idx_road_segments_road_id
    ON road_segments (road_id);

CREATE INDEX idx_roads_status
    ON roads (status);

CREATE INDEX idx_roads_condition
    ON roads (condition);

CREATE INDEX idx_road_segments_condition
    ON road_segments (condition);

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE INDEX idx_road_segments_geometry
    ON road_segments
    USING GIST (geometry);
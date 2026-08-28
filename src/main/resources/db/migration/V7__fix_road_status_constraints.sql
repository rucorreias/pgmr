ALTER TABLE roads
DROP CONSTRAINT chk_roads_status;

ALTER TABLE roads
    ADD CONSTRAINT chk_roads_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE',
                       'UNDER_CONSTRUCTION',
                       'PERMANENTLY_CLOSED'
                )
            );


ALTER TABLE road_segments
DROP CONSTRAINT chk_segment_status;

ALTER TABLE road_segments
    ADD CONSTRAINT chk_segment_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE',
                       'UNDER_CONSTRUCTION',
                       'PERMANENTLY_CLOSED'
                )
            );
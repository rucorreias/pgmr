CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    user_id     UUID,

    action      VARCHAR(100) NOT NULL,

    entity_type VARCHAR(100),
    entity_id   UUID,

    details     JSONB,

    ip_address  INET,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);


CREATE INDEX idx_audit_logs_user_id
    ON audit_logs (user_id);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs (entity_type, entity_id);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs (created_at);

CREATE INDEX idx_audit_logs_action
    ON audit_logs (action);
--liquibase formatted sql

--changeset moratorium:008-create-import-operations
CREATE TABLE import_operations (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    added_count INTEGER,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_import_operations_user ON import_operations(user_id);
CREATE INDEX idx_import_operations_status ON import_operations(status);
CREATE INDEX idx_import_operations_created ON import_operations(created_at DESC);


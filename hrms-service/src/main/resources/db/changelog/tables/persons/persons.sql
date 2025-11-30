--liquibase formatted sql

--changeset moratorium:005-create-persons
CREATE TABLE persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    eye_color VARCHAR(50) NOT NULL,
    hair_color VARCHAR(50) NOT NULL,
    location_id BIGINT,
    height BIGINT NOT NULL CHECK (height > 0),
    nationality VARCHAR(50),
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL
);

CREATE INDEX idx_persons_name ON persons(name);
CREATE INDEX idx_persons_nationality ON persons(nationality);

--liquibase formatted sql

--changeset moratorium:003-create-locations
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    x BIGINT NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z BIGINT NOT NULL,
    name VARCHAR(255)
);

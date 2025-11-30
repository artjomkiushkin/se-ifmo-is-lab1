--liquibase formatted sql

--changeset moratorium:002-create-addresses
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    zip_code VARCHAR(255) NOT NULL
);

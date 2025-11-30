--liquibase formatted sql

--changeset moratorium:001-create-users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL
);

CREATE INDEX idx_users_username ON users(username);

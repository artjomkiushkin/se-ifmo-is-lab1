--liquibase formatted sql

--changeset moratorium:004-create-organizations
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    official_address_id BIGINT,
    annual_turnover REAL NOT NULL CHECK (annual_turnover > 0),
    employees_count BIGINT CHECK (employees_count > 0),
    full_name VARCHAR(255) NOT NULL,
    rating REAL NOT NULL CHECK (rating > 0),
    type VARCHAR(50),
    FOREIGN KEY (official_address_id) REFERENCES addresses(id) ON DELETE SET NULL
);

CREATE INDEX idx_organizations_full_name ON organizations(full_name);

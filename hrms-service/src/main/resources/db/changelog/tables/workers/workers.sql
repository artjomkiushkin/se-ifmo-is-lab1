--liquibase formatted sql

--changeset moratorium:006-create-workers
CREATE TABLE workers (
    id BIGSERIAL PRIMARY KEY,
    coordinate_x DOUBLE PRECISION NOT NULL,
    coordinate_y REAL NOT NULL,
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    organization_id BIGINT NOT NULL,
    salary DOUBLE PRECISION NOT NULL CHECK (salary > 0),
    rating REAL NOT NULL CHECK (rating > 0),
    start_date DATE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    position VARCHAR(50) NOT NULL,
    person_id BIGINT NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);

CREATE INDEX idx_workers_organization ON workers(organization_id);
CREATE INDEX idx_workers_end_date ON workers(end_date);
CREATE INDEX idx_workers_person ON workers(person_id);

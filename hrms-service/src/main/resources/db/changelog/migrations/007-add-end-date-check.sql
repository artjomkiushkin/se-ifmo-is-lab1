--liquibase formatted sql

--changeset moratorium:007-add-end-date-check
ALTER TABLE workers ADD CONSTRAINT chk_end_date_after_start CHECK (end_date IS NULL OR end_date::date >= start_date);


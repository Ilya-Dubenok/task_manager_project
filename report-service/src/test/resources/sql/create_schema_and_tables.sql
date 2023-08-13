CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.report
(
    uuid uuid NOT NULL,
    dt_create timestamp(3) without time zone,
    dt_update timestamp(3) without time zone,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    description character varying NOT NULL,
    params json NOT NULL

);
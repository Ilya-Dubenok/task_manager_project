CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.audit
(

    uuid uuid NOT NULL,
    dt_create timestamp(3) without time zone,
    id character varying(255) NOT NULL,
    text character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    fio character varying(255),
    mail character varying(255),
    role character varying(255),
    user_uuid uuid,
    CONSTRAINT audit_pkey PRIMARY KEY (uuid)

);
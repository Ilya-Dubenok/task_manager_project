CREATE DATABASE report_service
WITH
    OWNER = root
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
;

\connect report_service;

CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.report
(
    uuid uuid NOT NULL,
    dt_create timestamp(3) without time zone,
    dt_update timestamp(3) without time zone,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    description character varying NOT NULL,
    params json NOT NULL,
    CONSTRAINT report_pkey PRIMARY KEY (uuid)

);


CREATE TABLE IF NOT EXISTS app.report_info
(

    report_uuid uuid NOT NULL,
    file_name character varying,
    bucket_name character varying,
    CONSTRAINT report_info_pkey PRIMARY KEY (report_uuid),
    CONSTRAINT report_info_report_foreign_key FOREIGN KEY (report_uuid)
            REFERENCES app.report (uuid) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION

);
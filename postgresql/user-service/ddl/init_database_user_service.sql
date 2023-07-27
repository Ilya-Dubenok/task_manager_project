CREATE DATABASE user_service
WITH
    OWNER = root
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
;

\connect user_service;

CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.users
(
    uuid uuid NOT NULL,
    dt_create timestamp(3) without time zone,
    dt_update timestamp(3) without time zone,
    fio character varying(255),
    mail character varying(255),
    role character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    password character varying(255),
    CONSTRAINT users_pkey PRIMARY KEY (uuid),
    CONSTRAINT user_mail_unique_constraint UNIQUE (mail)
);


CREATE TABLE IF NOT EXISTS app.verification_info
(
   	uuid uuid NOT NULL,
    mail character varying(255) NOT NULL,
	code integer NOT NULL,
	registered_time timestamp(3) without time zone,
	email_status character varying(255) NOT NULL,
	count_of_attempts integer default 1,
    CONSTRAINT verification_info_pkey PRIMARY KEY (uuid),
	CONSTRAINT verification_mail_unique_constraint UNIQUE (mail)

)
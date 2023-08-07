CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.users
(
    uuid uuid NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS app.project
(
    uuid uuid NOT NULL,
    description character varying,
    dt_create timestamp(3) without time zone,
    dt_update timestamp(3) without time zone,
    name character varying,
    project_status character varying(255),
    user_uuid uuid,
    CONSTRAINT project_pkey PRIMARY KEY (uuid),
    CONSTRAINT project_name_unique_constraint UNIQUE (name),
    CONSTRAINT project_users_foreign_key FOREIGN KEY (user_uuid)
        REFERENCES app.users (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS app.projects_users
(
    project_uuid uuid NOT NULL,
    users_uuid uuid NOT NULL,
    CONSTRAINT projects_users_pkey PRIMARY KEY (project_uuid, users_uuid),
    CONSTRAINT projects_users_project_foreign_key FOREIGN KEY (project_uuid)
        REFERENCES app.project (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT projects_users_users_foreign_key FOREIGN KEY (users_uuid)
        REFERENCES app.users (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS app.task
(
    uuid uuid NOT NULL,
    description character varying,
    dt_create timestamp(3) without time zone,
    dt_update timestamp(3) without time zone,
    status character varying(255),
    title character varying(255) NOT NULL,
    implementer_uuid uuid,
    project_uuid uuid NOT NULL,
    CONSTRAINT task_pkey PRIMARY KEY (uuid),
    CONSTRAINT task_project_foreign_key FOREIGN KEY (project_uuid)
        REFERENCES app.project (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT task_user_foreign_key FOREIGN KEY (implementer_uuid)
        REFERENCES app.users (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION

);



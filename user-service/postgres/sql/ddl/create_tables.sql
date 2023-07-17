CREATE TABLE IF NOT EXISTS app.users
(
    uuid uuid NOT NULL,
    dt_create timestamp(3) without time zone,
    dt_update timestamp(3) without time zone,
    fio character varying(255) ,
    mail character varying(255) ,
    role character varying(255) ,
    status character varying(255) ,
    password character varying(255) ,
    CONSTRAINT users_pkey PRIMARY KEY (uuid)
)
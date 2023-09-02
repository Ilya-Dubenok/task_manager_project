\connect user_service;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO app.users(
	uuid, dt_create, dt_update, fio, mail, role, status, password)
	VALUES (uuid_generate_v4(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin@admin.com', 'ADMIN', 'ACTIVATED', '$2a$10$pUm3pgcf.TXAU5an2f7CSOLhJRmPiiD4tuQeLjXTiia5UcujYE7sq');
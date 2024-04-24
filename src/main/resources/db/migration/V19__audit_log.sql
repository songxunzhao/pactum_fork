ALTER TABLE audit_event
    RENAME COLUMN principal TO username;
ALTER TABLE audit_event
    RENAME COLUMN "data" TO extra_data;
ALTER TABLE audit_event
    ADD COLUMN "description"        VARCHAR(255),
    ADD COLUMN "target_entity_type" VARCHAR(255),
    ADD COLUMN "target_entity_pk"   VARCHAR(255),
    ADD COLUMN "remote_ip"          VARCHAR(45);

CREATE INDEX idx_target_entity ON audit_event (target_entity_type, target_entity_pk);
ALTER TABLE audit_event
    ADD PRIMARY KEY (id);

UPDATE audit_event
SET type = 'AUTH_LOGIN_SUCCESSFUL'
WHERE type = 'LOGIN';
UPDATE audit_event
SET type = 'AUTH_LOGOUT'
WHERE type = 'LOGOUT';

UPDATE audit_event
SET type = 'CLIENT_CREATED'
WHERE type = 'CREATE'
  AND extra_data ->> 'client' IS NOT NULL;
UPDATE audit_event
SET type = 'CLIENT_DELETED'
WHERE type = 'DELETE'
  AND extra_data ->> 'client' IS NOT NULL;

UPDATE audit_event
SET type = 'SET_HEALTH_STATUS'
WHERE type = 'UPDATE'
  AND extra_data ->> 'client' IN ('UP', 'DOWN');

UPDATE audit_event
SET type = 'NEGOTIATION_CREATED'
WHERE type = 'CREATE'
  AND extra_data ->> 'negotiation' IS NOT NULL;
UPDATE audit_event
SET type = 'NEGOTIATION_DELETED'
WHERE type = 'DELETE'
  AND extra_data ->> 'negotiation' IS NOT NULL;

SELECT setval('audit_event_id_seq', (SELECT MAX(id) FROM audit_event));
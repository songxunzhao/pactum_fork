ALTER TABLE negotiation ADD status varchar;
ALTER TABLE negotiation ADD flow_id varchar;
ALTER TABLE negotiation ADD model_id varchar;
ALTER TABLE negotiation ADD model_key varchar;
ALTER TABLE negotiation ADD chat_version_id bigint;
ALTER TABLE negotiation ADD model_version_id bigint;

ALTER TABLE chat_asset ADD CONSTRAINT chat_asset_pk PRIMARY KEY (id);

ALTER TABLE negotiation
    ADD CONSTRAINT negotiation_chat_asset_chat_id_fk
        FOREIGN KEY (chat_version_id) REFERENCES chat_asset on delete set null;

ALTER TABLE negotiation
    ADD CONSTRAINT negotiation_chat_asset_model_id_fk
        FOREIGN KEY (model_version_id) REFERENCES chat_asset on delete set null;

ALTER TABLE client ADD negotiation_extra_fields jsonb;

INSERT INTO negotiation (id, client_id, state_id, status, flow_id, model_id, model_key, chat_version_id, model_version_id)
SELECT distinct on(state_id) nextval('client_chat_id_seq'), null, state_id, 'OPENED', chat_id, models_id, model_key, chat_version_id, model_version_id
FROM chat_state
ORDER BY state_id, time DESC;
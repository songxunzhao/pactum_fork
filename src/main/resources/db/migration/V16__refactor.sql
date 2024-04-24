ALTER TABLE chat_state RENAME TO negotiation_state;
ALTER TABLE negotiation_state ADD negotiation_id bigint;
ALTER TABLE negotiation RENAME COLUMN chat_version_id TO flow_version_id;

ALTER TABLE chat_asset RENAME TO negotiation_asset;

alter table negotiation_state
    add constraint negotiation_state_negotiation_id_fk
    foreign key (negotiation_id) references negotiation on delete set null;

insert into client(tag, name) values('pactum', 'Pactum');
insert into negotiation(id, client_id, state_id, status, flow_id, model_id, model_key, flow_version_id, model_version_id, create_time, terms)
values (0, (select id from client where tag = 'pactum'), 'demoNegotiation', 'OPENED',
        '1fbjO6mJVyCCMOZdQO90qLfXCxghoX4Ps', '1UPs_IYkoXrNO0jUCtedsfNg3wF8Yel9w', 'null', null, null, CURRENT_TIMESTAMP, null);

update negotiation_state set negotiation_id = (select id from negotiation where negotiation.state_id = negotiation_state.state_id)
where negotiation_id is null;

ALTER TABLE negotiation_state DROP COLUMN chat_id;
ALTER TABLE negotiation_state DROP COLUMN model_key;
ALTER TABLE negotiation_state DROP COLUMN models_id;
ALTER TABLE negotiation_state DROP COLUMN chat_version_id;
ALTER TABLE negotiation_state DROP COLUMN model_version_id;

ALTER TABLE negotiation ADD model_attributes jsonb;

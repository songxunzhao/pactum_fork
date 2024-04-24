create table chat_asset
(
    id              serial                                 not null,
    drive_id        varchar,
    type            varchar,
    generation_id   bigint,
    md5_checksum    varchar,
    time            timestamp with time zone default now() not null
);

alter table chat_asset owner to pactum;

ALTER TABLE chat_state ADD COLUMN chat_version_id bigint;
ALTER TABLE chat_state ADD COLUMN model_version_id bigint;

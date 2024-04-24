create table client
(
    id   serial not null constraint client_pk primary key,
    tag  varchar,
    name varchar
);

alter table client owner to pactum;
create unique index client_id_uindex on client (id);

create table client_chat
(
    id        serial not null constraint client_chat_pk primary key,
    client_id bigint constraint client_chat_client_id_fk references client on delete cascade,
    chat_id   varchar
);

alter table client_chat owner to pactum;
create unique index client_chat_id_uindex on client_chat (id);

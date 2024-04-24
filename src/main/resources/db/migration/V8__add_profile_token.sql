create table profile
(
    id          serial       not null constraint profile_pk primary key,
    email       varchar(255) not null,
    first_name  varchar(255),
    last_name   varchar(255),
    google_id   varchar(255),
    image_url   varchar(255),
    login_date  timestamp
);

create table token
(
    id          serial       not null constraint token_pk primary key,
    email       varchar(255) not null,
    token       varchar,
    expire      integer
);

alter table profile owner to pactum;
alter table token owner to pactum;

create unique index profile_email_uindex on profile (email);
create unique index profile_id_uindex on profile (id);
create unique index token_email_uindex on token (email);
create unique index token_id_uindex on token (id);

drop table oauth_client_details cascade;
drop table user_role cascade;
drop table users cascade;

insert into profile values (0, 'api', 'API', '', '', '', '2020-05-03 19:55:42.000000');
insert into token values (0, 'api', 'DMqBQpwDbdc3K++S8QmwMnSPO77YG59c9iH5Y5+0kaQF9+bK+b2wNpC/BpDWiR7vo319E9lOkF7i/tR9zO3OXg==', 1620061332);
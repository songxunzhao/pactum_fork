create table negotiation_field
(
    id             serial       not null primary key,
    negotiation_id int          not null references negotiation on update cascade on delete cascade,
    type           varchar(255) not null,
    attribute      varchar(255) not null,
    value          text         not null CHECK (char_length(value) <= 1024),
    create_time    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


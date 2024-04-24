ALTER TABLE negotiation ADD create_time timestamp default now() not null;
ALTER TABLE negotiation ALTER create_time TYPE TIMESTAMP WITH TIME ZONE USING create_time AT TIME ZONE 'UTC';


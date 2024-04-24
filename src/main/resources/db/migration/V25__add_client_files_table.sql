CREATE TABLE client_file
(
    id                     BIGSERIAL NOT NULL CONSTRAINT client_file_pk PRIMARY KEY,
    client_id              BIGINT NOT NULL CONSTRAINT client_file_client_id_fk REFERENCES client(id),
    storage_id             TEXT NOT NULL CHECK (char_length(storage_id) <= 1024),
    original_file_name     TEXT NOT NULL CHECK (char_length(original_file_name) <= 1024),
    original_file_size     BIGINT NOT NULL,
    username               VARCHAR(255) NOT NULL,
    upload_time            TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX client_file_id_uindex ON client_file (id);

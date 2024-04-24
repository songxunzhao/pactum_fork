CREATE TABLE state_blacklist
(
    state_id VARCHAR(255),
    time     TIMESTAMP NOT NULL DEFAULT NOW()
);
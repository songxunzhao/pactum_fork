ALTER TABLE negotiation ADD chat_start_time timestamp;
ALTER TABLE negotiation ALTER chat_start_time TYPE TIMESTAMP WITH TIME ZONE USING chat_start_time AT TIME ZONE 'UTC';
UPDATE negotiation SET chat_start_time =
    (SELECT time AT TIME ZONE 'UTC' FROM negotiation_state WHERE state_id = negotiation.state_id ORDER BY time ASC LIMIT 1);

ALTER TABLE negotiation ADD chat_update_time timestamp;
ALTER TABLE negotiation ALTER chat_update_time TYPE TIMESTAMP WITH TIME ZONE USING chat_update_time AT TIME ZONE 'UTC';
UPDATE negotiation SET chat_update_time =
    (SELECT time AT TIME ZONE 'UTC' FROM negotiation_state WHERE state_id = negotiation.state_id ORDER BY time DESC LIMIT 1)
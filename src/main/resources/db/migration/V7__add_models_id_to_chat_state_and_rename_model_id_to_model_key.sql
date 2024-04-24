ALTER TABLE chat_state ADD COLUMN models_id VARCHAR(255);
ALTER TABLE chat_state RENAME COLUMN model_id to model_key;
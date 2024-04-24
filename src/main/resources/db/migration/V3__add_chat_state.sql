CREATE TABLE chat_state
(
  id       SERIAL    NOT NULL,
  chat_id  VARCHAR(255),
  state_id VARCHAR(255),
  state    jsonb,
  time     TIMESTAMP NOT NULL DEFAULT NOW()
)

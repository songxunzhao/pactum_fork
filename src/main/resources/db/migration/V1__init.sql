CREATE TABLE users
(
  id       SERIAL       NOT NULL,
  email    VARCHAR(255) NOT NULL,
  password text
);

CREATE TABLE role
(
  id   SERIAL NOT NULL PRIMARY KEY,
  name TEXT   NOT NULL
);

ALTER TABLE users
  ADD PRIMARY KEY (id);

CREATE TABLE user_role
(
  user_id INTEGER REFERENCES users (id),
  role_id INTEGER REFERENCES role (id)
);

CREATE TABLE audit_event
(
  id        SERIAL NOT NULL,
  principal VARCHAR(255),
  type      VARCHAR(255),
  time      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  data      jsonb
)

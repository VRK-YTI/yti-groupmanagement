CREATE TABLE tempuser
(
  id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email                 VARCHAR(255),
  firstname             TEXT,
  lastname              TEXT,
  created_at            TIMESTAMP DEFAULT current_timestamp,
  removed_at            TIMESTAMP DEFAULT NULL,
  token_created_at      TIMESTAMP WITHOUT TIME ZONE NULL,
  token_invalidation_at TIMESTAMP WITHOUT TIME ZONE NULL,
  token_role            VARCHAR(255),
  container_uri         TEXT
);

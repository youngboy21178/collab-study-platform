DROP TABLE IF EXISTS memberships;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
  name          TEXT NOT NULL,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  avatar_url    TEXT
);

CREATE TABLE groups (
  group_id     INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL,
  description  TEXT,
  created_by   INTEGER NOT NULL,
  created_at   TEXT NOT NULL,
  avatar_url   TEXT,
  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE memberships (
  membership_id INTEGER PRIMARY KEY AUTOINCREMENT,
  group_id      INTEGER NOT NULL,
  user_id       INTEGER NOT NULL,
  role          TEXT NOT NULL,
  joined_at     TEXT NOT NULL,
  FOREIGN KEY (group_id) REFERENCES groups(group_id),
  FOREIGN KEY (user_id)  REFERENCES users(user_id)
);

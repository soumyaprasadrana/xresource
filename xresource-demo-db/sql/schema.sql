-- Drop schema if exists
DROP SCHEMA IF EXISTS xresourcedemo CASCADE;

-- Create schema
CREATE SCHEMA IF NOT EXISTS xresourcedemo;

-- 1. Team Table
CREATE TABLE xresourcedemo.team (
    team_id VARCHAR(100) PRIMARY KEY,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. User Table
CREATE TABLE xresourcedemo.user (
  user_id VARCHAR(255) PRIMARY KEY,
  user_team VARCHAR(100) NOT NULL,
  user_pass TEXT NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_team) REFERENCES xresourcedemo.team(team_id)
);

-- 3. Session Table
CREATE TABLE xresourcedemo.session (
    session_id TEXT PRIMARY KEY,
    auth_token TEXT NOT NULL,
    start_timestamp VARCHAR(255) NOT NULL,
    last_activity_timestamp VARCHAR(255),
    client_ip VARCHAR(255),
    client_os VARCHAR(255),
    client_browser VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    user_team VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES xresourcedemo.user(user_id),
    FOREIGN KEY (user_team) REFERENCES xresourcedemo.team(team_id)
);

-- 4. Authorizations Table
CREATE TABLE xresourcedemo.authorizations (
    user_id VARCHAR(255) PRIMARY KEY,
    user_team VARCHAR(100) NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES xresourcedemo.user(user_id),
    FOREIGN KEY (user_team) REFERENCES xresourcedemo.team(team_id)
);

-- 5. Demo Data Table 1 (Admin-only)
CREATE TABLE xresourcedemo.demodatatable1 (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    value TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES xresourcedemo.user(user_id)
);

-- 6. Demo Data Table 2 (Normal user access)
CREATE TABLE xresourcedemo.demodatatable2 (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES xresourcedemo.user(user_id)
);

-- Insert default admin team
INSERT INTO xresourcedemo.team (team_id, description)
VALUES ('admin-team', 'Administrator Team');

-- Insert normal user team
INSERT INTO xresourcedemo.team (team_id, description)
VALUES ('users', 'Normal User Team');

-- Insert admin user
INSERT INTO xresourcedemo.user (
    user_id, user_team, user_pass, email, first_name, last_name
) VALUES (
    'admin', 'admin-team', '$2a$10$KKMo5ZDLS5fQIz694NRAn.JooYIJB7sce7QAq589vY8WFYjBRUELy', 'admin@example.com', 'Admin', 'User'
);

-- Insert normal user
INSERT INTO xresourcedemo.user (
    user_id, user_team, user_pass, email, first_name, last_name
) VALUES (
    'user1', 'users', '$2a$10$KKMo5ZDLS5fQIz694NRAn.JooYIJB7sce7QAq589vY8WFYjBRUELy', 'user1@example.com', 'Regular', 'User'
);

-- Assign roles
INSERT INTO xresourcedemo.authorizations (user_id, user_team, is_admin)
VALUES 
    ('admin', 'admin-team', TRUE),
    ('user1', 'users', FALSE);

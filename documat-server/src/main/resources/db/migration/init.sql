-- Create database if not exists
CREATE DATABASE IF NOT EXISTS documat_db;

USE documat_db;

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (name) VALUES ('ROLE_MANAGER') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;

-- Create default admin user (password: admin123)
-- Note: The password hash is for 'admin123' using BCrypt
INSERT INTO users (username, password, email, full_name, enabled, created_at, updated_at)
VALUES ('admin', '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUa35gJdCr2v2QbVFzu', 'admin@documat.com', 'Administrator', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id=user_id;

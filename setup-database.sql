-- MySQL Database Setup Script for Blog Application
-- This script creates the database, user, and grants necessary permissions

-- Create the database
CREATE DATABASE IF NOT EXISTS blog_db;

-- Create the user (if it doesn't exist)
CREATE USER IF NOT EXISTS 'blog_user'@'localhost' IDENTIFIED BY 'blog_password';

-- Grant all privileges on the blog_db database to blog_user
GRANT ALL PRIVILEGES ON blog_db.* TO 'blog_user'@'localhost';

-- Apply the changes
FLUSH PRIVILEGES;

-- Verify the setup
SHOW DATABASES LIKE 'blog_db';
SELECT User, Host FROM mysql.user WHERE User = 'blog_user';



-- Migration to fix image URL column length issue
-- Run this SQL script in your MySQL database

-- First, drop existing foreign key constraints if they exist
SET FOREIGN_KEY_CHECKS = 0;

-- Alter the image_url column to use LONGTEXT instead of TEXT
ALTER TABLE content_images 
MODIFY COLUMN image_url LONGTEXT;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Alternatively, if you want to start fresh and don't mind losing data:
-- DROP TABLE IF EXISTS content_images;
-- DROP TABLE IF EXISTS content_tv_mapping;
-- DROP TABLE IF EXISTS content_schedules;

-- Then restart your Spring Boot application and the tables will be recreated
-- with the new schema automatically.

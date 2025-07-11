-- Migration to fix column length issues and ensure video upload support
-- Run this SQL script in your MySQL database

-- First, drop existing foreign key constraints if they exist
SET FOREIGN_KEY_CHECKS = 0;

-- Fix content_type column to ensure it can store full enum values
ALTER TABLE content_schedules 
MODIFY COLUMN content_type VARCHAR(50) NOT NULL;

-- Fix content column to handle larger text content
ALTER TABLE content_schedules 
MODIFY COLUMN content LONGTEXT;

-- Ensure image URL columns use LONGTEXT (for data URLs and long paths)
ALTER TABLE content_images 
MODIFY COLUMN image_url LONGTEXT;

-- Ensure video URL columns use LONGTEXT (for data URLs and long paths)
-- This table might not exist yet, but we'll create it if needed
CREATE TABLE IF NOT EXISTS content_videos (
    schedule_id BIGINT NOT NULL,
    video_url LONGTEXT,
    INDEX idx_schedule_video (schedule_id),
    FOREIGN KEY (schedule_id) REFERENCES content_schedules(id) ON DELETE CASCADE
);

-- Fix video URL column if it already exists
ALTER TABLE content_videos 
MODIFY COLUMN video_url LONGTEXT;

-- Ensure TV enum column can store full enum values
ALTER TABLE content_tv_mapping 
MODIFY COLUMN tv_enum VARCHAR(20) NOT NULL;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Alternatively, if you want to start fresh and don't mind losing data:
-- DROP TABLE IF EXISTS content_images;
-- DROP TABLE IF EXISTS content_videos;
-- DROP TABLE IF EXISTS content_tv_mapping;
-- DROP TABLE IF EXISTS content_schedules;

-- Then restart your Spring Boot application and the tables will be recreated
-- with the new schema automatically.

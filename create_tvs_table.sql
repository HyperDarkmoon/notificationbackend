-- Create the tvs table manually
-- This will allow the foreign key constraints to work properly

CREATE TABLE IF NOT EXISTS tvs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    location VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;    

-- Insert the default TV entries to maintain backward compatibility
INSERT INTO tvs (name, display_name, description, location, active, created_at, updated_at) VALUES
('TV1', 'Television 1', 'Main display television 1', 'Main Area', TRUE, NOW(), NOW()),
('TV2', 'Television 2', 'Main display television 2', 'Secondary Area', TRUE, NOW(), NOW()),
('TV3', 'Television 3', 'Main display television 3', 'Lobby', TRUE, NOW(), NOW()),
('TV4', 'Television 4', 'Main display television 4', 'Conference Room', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    display_name = VALUES(display_name),
    description = VALUES(description),
    location = VALUES(location),
    updated_at = NOW();

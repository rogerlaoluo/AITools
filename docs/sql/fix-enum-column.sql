-- Fix content_type column to accept lowercase enum values
-- First, change column type to VARCHAR
ALTER TABLE contents MODIFY COLUMN content_type VARCHAR(20);

-- Update any existing data to lowercase
UPDATE contents SET content_type = 'image' WHERE content_type = 'IMAGE';
UPDATE contents SET content_type = 'video' WHERE content_type = 'VIDEO';
UPDATE contents SET content_type = 'text' WHERE content_type = 'TEXT';
UPDATE contents SET content_type = 'mixed' WHERE content_type = 'MIXED';

-- Also ensure media_paths is TEXT type
ALTER TABLE contents MODIFY COLUMN media_paths TEXT;

-- Verify the changes
DESCRIBE contents;
SELECT id, content_type FROM contents LIMIT 10;

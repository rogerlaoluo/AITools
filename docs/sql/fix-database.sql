-- Fix media_paths column type
ALTER TABLE contents MODIFY COLUMN media_paths TEXT;

-- Also fix tags table if needed
ALTER TABLE tags MODIFY COLUMN created_at DATETIME;

-- Verify the changes
DESCRIBE contents;
DESCRIBE tags;

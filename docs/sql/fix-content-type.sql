-- Fix any existing data with lowercase content_type values
UPDATE contents SET content_type = 'IMAGE' WHERE content_type = 'image';
UPDATE contents SET content_type = 'VIDEO' WHERE content_type = 'video';
UPDATE contents SET content_type = 'TEXT' WHERE content_type = 'text';
UPDATE contents SET content_type = 'MIXED' WHERE content_type = 'mixed';

-- Fix media_paths column type
ALTER TABLE contents MODIFY COLUMN media_paths TEXT;

-- Verify the changes
SELECT id, content_type, media_paths FROM contents LIMIT 10;

SET @uk_exists := (
        SELECT COUNT(*)
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
            AND table_name = 'tracked_series'
            AND constraint_name = 'uk_tracked_series_user_series'
            AND constraint_type = 'UNIQUE'
    );
SET @sql := IF(
        @uk_exists = 0,
        'ALTER TABLE tracked_series ADD CONSTRAINT uk_tracked_series_user_series UNIQUE (user_id, series_id)',
        'SELECT 1'
    );
PREPARE stmt
FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
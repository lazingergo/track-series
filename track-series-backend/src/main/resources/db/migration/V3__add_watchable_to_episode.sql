SET @watchable_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'episode'
      AND column_name = 'watchable'
);

SET @watchable_sql := IF(
    @watchable_column_exists = 0,
    'ALTER TABLE episode ADD COLUMN watchable TINYINT(1) NOT NULL DEFAULT 0',
    'SELECT 1'
);

PREPARE watchable_stmt FROM @watchable_sql;
EXECUTE watchable_stmt;
DEALLOCATE PREPARE watchable_stmt;

UPDATE episode
SET watchable = CASE
    WHEN airdate IS NOT NULL AND airdate <= UTC_DATE() THEN 1
    ELSE 0
END;
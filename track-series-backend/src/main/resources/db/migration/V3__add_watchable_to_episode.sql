ALTER TABLE episode
ADD COLUMN watchable TINYINT(1) NOT NULL DEFAULT 0;
UPDATE episode
SET watchable = CASE
        WHEN airdate IS NOT NULL
        AND airdate <= UTC_DATE() THEN 1
        ELSE 0
    END;
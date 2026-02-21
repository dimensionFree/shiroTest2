ALTER TABLE article_read_record
    ADD COLUMN read_time_new DATETIME(6) NULL;

UPDATE article_read_record
SET read_time_new = COALESCE(
        STR_TO_DATE(SUBSTRING(REPLACE(read_time, 'T', ' '), 1, 26), '%Y-%m-%d %H:%i:%s.%f'),
        STR_TO_DATE(SUBSTRING(REPLACE(read_time, 'T', ' '), 1, 19), '%Y-%m-%d %H:%i:%s')
                    );

ALTER TABLE article_read_record
    DROP COLUMN read_time;

ALTER TABLE article_read_record
    CHANGE COLUMN read_time_new read_time DATETIME(6) NOT NULL;

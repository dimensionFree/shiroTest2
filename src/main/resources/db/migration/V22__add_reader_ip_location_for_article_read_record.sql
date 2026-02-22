ALTER TABLE article_read_record
    ADD COLUMN reader_ip_location VARCHAR(128) NULL AFTER reader_ip;

UPDATE article_read_record
SET reader_ip_location = 'UNKNOWN'
WHERE reader_ip_location IS NULL OR reader_ip_location = '';

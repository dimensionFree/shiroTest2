ALTER TABLE article_read_record
    ADD COLUMN reader_ip_country VARCHAR(128) NULL AFTER reader_ip_location,
    ADD COLUMN reader_ip_province VARCHAR(128) NULL AFTER reader_ip_country,
    ADD COLUMN reader_ip_city VARCHAR(128) NULL AFTER reader_ip_province;

UPDATE article_read_record
SET reader_ip_country = CASE
                            WHEN reader_ip_location = 'PRIVATE_NETWORK' THEN 'PRIVATE_NETWORK'
                            ELSE 'UNKNOWN'
    END,
    reader_ip_province = CASE
                             WHEN reader_ip_location = 'PRIVATE_NETWORK' THEN 'PRIVATE_NETWORK'
                             ELSE 'UNKNOWN'
    END,
    reader_ip_city = CASE
                         WHEN reader_ip_location IS NULL OR reader_ip_location = '' THEN 'UNKNOWN'
                         ELSE reader_ip_location
    END
WHERE reader_ip_country IS NULL
   OR reader_ip_province IS NULL
   OR reader_ip_city IS NULL;

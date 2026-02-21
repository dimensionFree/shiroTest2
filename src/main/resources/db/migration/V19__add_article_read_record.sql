CREATE TABLE IF NOT EXISTS article_read_record (
    id VARCHAR(255) NOT NULL,
    article_id VARCHAR(255) NOT NULL,
    reader_ip VARCHAR(64) NOT NULL,
    reader_user_id VARCHAR(255) NULL,
    reader_user_agent VARCHAR(512) NULL,
    read_time VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_arr_article_id (article_id),
    INDEX idx_arr_read_time (read_time),
    CONSTRAINT fk_arr_article_id FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE
);

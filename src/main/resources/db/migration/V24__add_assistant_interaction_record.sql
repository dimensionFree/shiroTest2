CREATE TABLE IF NOT EXISTS assistant_interaction_record (
    id VARCHAR(255) NOT NULL,
    interaction_type VARCHAR(64) NOT NULL,
    interaction_action VARCHAR(64) NOT NULL,
    interaction_payload VARCHAR(4000) NULL,
    client_ip VARCHAR(64) NOT NULL,
    client_ip_location VARCHAR(128) NULL,
    client_ip_country VARCHAR(128) NULL,
    client_ip_province VARCHAR(128) NULL,
    client_ip_city VARCHAR(128) NULL,
    user_id VARCHAR(255) NULL,
    user_agent VARCHAR(512) NULL,
    trigger_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_air_trigger_time (trigger_time),
    INDEX idx_air_interaction_type_action (interaction_type, interaction_action),
    INDEX idx_air_client_ip (client_ip)
);

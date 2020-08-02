ALTER TABLE grouper_config ADD COLUMN config_value_clob CLOB;

ALTER TABLE grouper_config ADD COLUMN config_value_bytes BIGINT;
	
update grouper_config set config_value_bytes = length(config_value);
commit;

CREATE TABLE grouper_pit_config
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    config_file_name VARCHAR(100) NOT NULL,
    config_key VARCHAR(400) NOT NULL,
    config_value VARCHAR(4000),
    config_comment VARCHAR(4000),
    config_file_hierarchy VARCHAR(50) NOT NULL,
    config_encrypted VARCHAR(1) NOT NULL,
    config_sequence BIGINT NOT NULL,
    config_version_index BIGINT,
    last_updated BIGINT NOT NULL,
    config_value_clob CLOB,
    config_value_bytes BIGINT,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40),
    hibernate_version_number BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX pit_config_source_id_idx ON grouper_pit_config (source_id);

CREATE INDEX pit_config_context_idx ON grouper_pit_config (context_id);

CREATE UNIQUE INDEX pit_config_start_idx ON grouper_pit_config (start_time, source_id);

CREATE INDEX pit_config_end_idx ON grouper_pit_config (end_time);

		
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
    prev_config_value VARCHAR(4000),
    prev_config_value_clob CLOB,
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

CREATE TABLE grouper_file
(
    id VARCHAR(40) NOT NULL,
    system_name VARCHAR(100) NOT NULL,
    file_name VARCHAR(100) NOT NULL,
    file_path VARCHAR(400) NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    context_id VARCHAR(40),
    file_contents_varchar VARCHAR(4000),
    file_contents_clob CLOB,
    file_contents_bytes BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grpfile_unique_idx ON grouper_file (file_path);

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V34, ' || history) from 1 for 3500), db_version = 34 where object_name = 'Grouper';

commit;
		
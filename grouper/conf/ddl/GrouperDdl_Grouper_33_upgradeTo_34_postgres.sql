ALTER TABLE grouper_config ADD COLUMN config_value_clob varchar(10000000);

ALTER TABLE grouper_config ADD COLUMN config_value_bytes BIGINT;
	
COMMENT ON COLUMN grouper_config.config_value_clob IS 'config value for large data';

COMMENT ON COLUMN grouper_config.config_value_bytes IS 'size of config value in bytes';

UPDATE grouper_config set config_value_bytes = length(config_value);
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
    config_value_clob VARCHAR(10000000),
    config_value_bytes BIGINT,
    prev_config_value VARCHAR(4000),
    prev_config_value_clob VARCHAR(10000000),
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40),
    hibernate_version_number BIGINT NOT NULL,
    PRIMARY KEY (id)
);

COMMENT ON TABLE grouper_pit_config IS 'keeps track of grouper config.  Records are never deleted from this table';

COMMENT ON COLUMN grouper_pit_config.id IS 'uuid of record is unique for all records in table and primary key';

COMMENT ON COLUMN grouper_pit_config.source_id IS 'source_id: id of the grouper_config table';

COMMENT ON COLUMN grouper_pit_config.config_file_name IS 'Config file name of the config this record relates to, e.g. grouper.config.properties';

COMMENT ON COLUMN grouper_pit_config.config_key IS 'key of the config, not including elConfig';

COMMENT ON COLUMN grouper_pit_config.config_value IS 'Value of the config';

COMMENT ON COLUMN grouper_pit_config.config_comment IS 'documentation of the config value';

COMMENT ON COLUMN grouper_pit_config.config_file_hierarchy IS 'config file hierarchy, e.g. base, institution, or env';

COMMENT ON COLUMN grouper_pit_config.config_encrypted IS 'if the value is encrypted';

COMMENT ON COLUMN grouper_pit_config.config_sequence IS 'if there is more data than fits in the column this is the 0 indexed order';

COMMENT ON COLUMN grouper_pit_config.config_version_index IS 'for built in configs, this is the index that will identify if the database configs should be replaced from the java code';

COMMENT ON COLUMN grouper_pit_config.last_updated IS 'when this record was inserted or last updated';

COMMENT ON COLUMN grouper_pit_config.config_value_clob IS 'config value for large data';

COMMENT ON COLUMN grouper_pit_config.config_value_bytes IS 'size of config value in bytes';

COMMENT ON COLUMN grouper_pit_config.prev_config_value IS 'previous config value';

COMMENT ON COLUMN grouper_pit_config.prev_config_value_clob IS 'previous config value clob';

COMMENT ON COLUMN grouper_pit_config.active IS 'T or F if this is an active record based on start and end dates';

COMMENT ON COLUMN grouper_pit_config.start_time IS 'millis from 1970 when this record was inserted';

COMMENT ON COLUMN grouper_pit_config.end_time IS 'millis from 1970 when this record was deleted';

COMMENT ON COLUMN grouper_pit_config.context_id IS 'Context id links together audit entry with the row';

COMMENT ON COLUMN grouper_pit_config.hibernate_version_number IS 'hibernate uses this to version rows';

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
    file_contents_clob VARCHAR(10000000),
    file_contents_bytes BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grpfile_unique_idx ON grouper_file (file_path);

COMMENT ON TABLE grouper_file IS 'table to store files for grouper. eg: workflow, reports';
    
COMMENT ON COLUMN grouper_file.id IS 'uuid of record is unique for all records in table and primary key';

COMMENT ON COLUMN grouper_file.system_name IS 'System name this file belongs to eg: workflow';

COMMENT ON COLUMN grouper_file.file_name IS 'Name of the file';

COMMENT ON COLUMN grouper_file.file_path IS 'Unique path of the file';

COMMENT ON COLUMN grouper_file.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_file.context_id IS 'Context id links together audit entry with the row';

COMMENT ON COLUMN grouper_file.file_contents_varchar IS 'contents of the file if can fit into 4000 bytes';

COMMENT ON COLUMN grouper_file.file_contents_clob IS 'large contents of the file';

COMMENT ON COLUMN grouper_file.file_contents_bytes IS 'size of file contents in bytes';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V34, ' || history) from 1 for 3500), db_version = 34 where object_name = 'Grouper';
commit;

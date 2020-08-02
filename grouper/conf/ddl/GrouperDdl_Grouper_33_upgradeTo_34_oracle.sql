ALTER TABLE grouper_config ADD COLUMN config_value_clob clob;

ALTER TABLE grouper_config ADD COLUMN config_value_bytes integer;
	
COMMENT ON COLUMN grouper_config.config_value_clob IS 'config value for large data';

COMMENT ON COLUMN grouper_config.config_value_bytes IS 'size of config value in bytes';

UPDATE grouper_config set config_value_bytes = length(config_value);
commit;

CREATE TABLE grouper_pit_config
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    config_file_name VARCHAR2(100) NOT NULL,
    config_key VARCHAR2(400) NOT NULL,
    config_value VARCHAR2(4000),
    config_comment VARCHAR2(4000),
    config_file_hierarchy VARCHAR2(50) NOT NULL,
    config_encrypted VARCHAR2(1) NOT NULL,
    config_sequence NUMBER(38) NOT NULL,
    config_version_index NUMBER(38),
    last_updated NUMBER(38) NOT NULL,
    config_value_clob CLOB,
    config_value_bytes INTEGER,
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38) NOT NULL,
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

COMMENT ON COLUMN grouper_pit_config.active IS 'T or F if this is an active record based on start and end dates';

COMMENT ON COLUMN grouper_pit_config.start_time IS 'millis from 1970 when this record was inserted';

COMMENT ON COLUMN grouper_pit_config.end_time IS 'millis from 1970 when this record was deleted';

COMMENT ON COLUMN grouper_pit_config.context_id IS 'Context id links together audit entry with the row';

COMMENT ON COLUMN grouper_pit_config.hibernate_version_number IS 'hibernate uses this to version rows';


CREATE INDEX pit_config_source_id_idx ON grouper_pit_config (source_id);

CREATE INDEX pit_config_context_idx ON grouper_pit_config (context_id);

CREATE UNIQUE INDEX pit_config_start_idx ON grouper_pit_config (start_time, source_id);

CREATE INDEX pit_config_end_idx ON grouper_pit_config (end_time);
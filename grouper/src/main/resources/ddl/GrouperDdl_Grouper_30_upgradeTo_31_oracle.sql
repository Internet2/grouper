CREATE TABLE grouper_config
(
    id VARCHAR2(40) NOT NULL,
    config_file_name VARCHAR2(100) NOT NULL,
    config_key VARCHAR2(400) NOT NULL,
    config_value VARCHAR2(4000),
    config_comment VARCHAR2(4000),
    config_file_hierarchy VARCHAR2(50) NOT NULL,
    config_encrypted VARCHAR2(1) NOT NULL,
    config_sequence NUMBER(38) NOT NULL,
    config_version_index NUMBER(38),
    last_updated NUMBER(38) NOT NULL,
    hibernate_version_number NUMBER(38) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grpconfig_config_file_idx ON grouper_config (config_file_name, last_updated);

CREATE INDEX grpconfig_config_key_idx ON grouper_config (config_key, config_file_name);

CREATE INDEX grpconfig_last_updated_idx ON grouper_config (last_updated);

CREATE UNIQUE INDEX grpconfig_unique_idx ON grouper_config (config_file_name, config_file_hierarchy, config_key, config_sequence);

CREATE INDEX change_log_temp_created_on_idx ON GROUPER_CHANGE_LOG_ENTRY_TEMP (CREATED_ON);

CREATE INDEX member_subjidentifier0_idx ON GROUPER_MEMBERS (SUBJECT_IDENTIFIER0);

CREATE INDEX pit_member_subjidentifier0_idx ON GROUPER_PIT_MEMBERS (SUBJECT_IDENTIFIER0);

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V31, ' || history), 1, 3500), db_version = 31 where object_name = 'Grouper';
commit;
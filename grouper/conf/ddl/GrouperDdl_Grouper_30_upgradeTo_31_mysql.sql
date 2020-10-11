CREATE TABLE grouper_config
(
    id VARCHAR(40) NOT NULL,
    config_file_name VARCHAR(100) NOT NULL,
    config_key VARCHAR(400) NOT NULL,
    config_value text NULL,
    config_comment text NULL,
    config_file_hierarchy VARCHAR(50) NOT NULL,
    config_encrypted VARCHAR(1) NOT NULL,
    config_sequence BIGINT NOT NULL,
    config_version_index BIGINT,
    last_updated BIGINT NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grpconfig_config_file_idx ON grouper_config (config_file_name, last_updated);

CREATE INDEX grpconfig_last_updated_idx ON grouper_config (last_updated);

CREATE INDEX change_log_temp_created_on_idx ON grouper_change_log_entry_temp (created_on);

CREATE INDEX member_subjidentifier0_idx ON grouper_members (subject_identifier0);

CREATE INDEX pit_member_subjidentifier0_idx ON grouper_pit_members (subject_identifier0);

CREATE INDEX grpconfig_config_key_idx ON grouper_config (config_key(100), config_file_name(50));

CREATE unique INDEX grpconfig_unique_idx ON grouper_config (config_file_name(20), config_file_hierarchy(20), config_key(100), config_sequence);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V31, ', history), 1, 3500), db_version = 31 where object_name = 'Grouper';
commit;


ALTER TABLE grouper_config
    ADD COLUMN config_value_clob_bytes BIGINT;

ALTER TABLE grouper_config ADD COLUMN config_value_clob mediumtext;

CREATE TABLE grouper_prov_duo_user
(
    config_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(40) NOT NULL,
    aliases VARCHAR(4000) NULL,
    phones VARCHAR(4000) NULL,
    is_push_enabled VARCHAR(1) NULL,
    email VARCHAR(200) NULL,
    first_name VARCHAR(256) NULL,
    last_name VARCHAR(256) NULL,
    is_enrolled VARCHAR(1) NULL,
    last_directory_sync BIGINT NULL,
    notes VARCHAR(4000) NULL,
    real_name VARCHAR(256) NULL,
    status VARCHAR(40) NULL,
    user_name VARCHAR(256) NOT NULL,
    created_at BIGINT NOT NULL,
    last_login_time BIGINT NULL,
    PRIMARY KEY (user_id)
);

CREATE INDEX grouper_duo_user_config_id_idx ON grouper_prov_duo_user (config_id);

CREATE UNIQUE INDEX grouper_duo_user_user_name_idx ON grouper_prov_duo_user (user_name(100), config_id);

CREATE UNIQUE INDEX grouper_duo_user_id_idx ON grouper_prov_duo_user (user_id, config_id);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V41, ', history), 1, 3500), db_version = 41 where object_name = 'Grouper';
commit;
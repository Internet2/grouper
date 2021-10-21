ALTER TABLE grouper_password ADD COLUMN expires_millis BIGINT;
ALTER TABLE grouper_password ADD COLUMN created_millis BIGINT;
ALTER TABLE grouper_password ADD COLUMN member_id_who_set_password VARCHAR(40);

ALTER TABLE grouper_password_recently_used ADD COLUMN attempt_millis BIGINT NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN ip_address VARCHAR(20) NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN status CHAR(1) NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN hibernate_version_number BIGINT NOT NULL;

ALTER TABLE grouper_password_recently_used MODIFY jwt_jti VARCHAR(100);
ALTER TABLE grouper_password_recently_used MODIFY jwt_iat INTEGER;

ALTER TABLE grouper_password DROP COLUMN recent_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_logins;

CREATE TABLE grouper_prov_zoom_user
(
    config_id VARCHAR(50) NOT NULL,
    member_id VARCHAR(40) NULL,
    id VARCHAR(40) NOT NULL,
    email VARCHAR(256) NOT NULL,
    first_name VARCHAR(256) NULL,
    last_name VARCHAR(256) NULL,
    type BIGINT,
    pmi BIGINT,
    timezone VARCHAR(100) NULL,
    verified BIGINT,
    created_at BIGINT,
    last_login_time BIGINT,
    language VARCHAR(100) NULL,
    status BIGINT,
    role_id BIGINT,
    PRIMARY KEY (email)
);

CREATE INDEX grouper_zoom_user_config_id_idx ON grouper_prov_zoom_user (config_id);

CREATE UNIQUE INDEX grouper_zoom_user_email_idx ON grouper_prov_zoom_user (email(100), config_id);

CREATE UNIQUE INDEX grouper_zoom_user_id_idx ON grouper_prov_zoom_user (id, config_id);

CREATE INDEX grouper_zoom_user_member_id_idx ON grouper_prov_zoom_user (member_id, config_id);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V38, ', history), 1, 3500), db_version = 38 where object_name = 'Grouper';
commit;


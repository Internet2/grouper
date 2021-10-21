ALTER TABLE GROUPER_PASSWORD ADD COLUMN expires_millis BIGINT;
ALTER TABLE GROUPER_PASSWORD ADD COLUMN created_millis BIGINT;
ALTER TABLE GROUPER_PASSWORD ADD COLUMN member_id_who_set_password VARCHAR(40);

ALTER TABLE grouper_password_recently_used ADD COLUMN attempt_millis BIGINT NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN ip_address VARCHAR(20) NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN status CHAR(1) NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN hibernate_version_number BIGINT NOT NULL;

ALTER TABLE grouper_password_recently_used ALTER COLUMN jwt_jti VARCHAR(100);
ALTER TABLE grouper_password_recently_used ALTER COLUMN jwt_iat VARCHAR(40);


ALTER TABLE grouper_password DROP COLUMN recent_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_logins;

CREATE TABLE grouper_prov_zoom_user
(
    config_id VARCHAR(50) NOT NULL,
    member_id VARCHAR(40),
    id VARCHAR(40) NOT NULL,
    email VARCHAR(200) NOT NULL,
    first_name VARCHAR(256),
    last_name VARCHAR(256),
    type BIGINT,
    pmi BIGINT,
    timezone VARCHAR(100),
    verified BIGINT,
    created_at BIGINT,
    last_login_time BIGINT,
    language VARCHAR(100),
    status BIGINT,
    role_id BIGINT,
    PRIMARY KEY (email)
);

CREATE INDEX grouper_zoom_user_config_id_idx ON grouper_prov_zoom_user (config_id);

CREATE UNIQUE INDEX grouper_zoom_user_email_idx ON grouper_prov_zoom_user (email, config_id);

CREATE UNIQUE INDEX grouper_zoom_user_id_idx ON grouper_prov_zoom_user (id, config_id);

CREATE INDEX grouper_zoom_user_member_id_idx ON grouper_prov_zoom_user (member_id, config_id);

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history) from 1 for 3500), db_version = 38 where object_name = 'Grouper';
commit;


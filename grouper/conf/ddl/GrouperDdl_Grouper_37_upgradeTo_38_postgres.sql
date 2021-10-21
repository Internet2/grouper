ALTER TABLE GROUPER_PASSWORD ADD COLUMN expires_millis BIGINT;
COMMENT ON COLUMN grouper_password.expires_millis IS 'millis since 1970 this password is going to expire';
ALTER TABLE GROUPER_PASSWORD ADD COLUMN created_millis BIGINT;
COMMENT ON COLUMN grouper_password.created_millis IS 'millis since 1970 this password was created';
ALTER TABLE GROUPER_PASSWORD ADD COLUMN member_id_who_set_password VARCHAR(40);
COMMENT ON COLUMN grouper_password.member_id_who_set_password IS 'member id who set this password';

ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD COLUMN attempt_millis BIGINT NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.attempt_millis IS 'millis since 1970 this password was attempted';

ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD COLUMN ip_address VARCHAR(20) NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.ip_address IS 'ip address from where the password was attempted';

ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD COLUMN status CHAR(1) NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.status IS 'status of the attempt. S/F/E etc';

ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD COLUMN hibernate_version_number BIGINT NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.hibernate_version_number IS 'hibernate version number';

ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ALTER COLUMN jwt_jti TYPE VARCHAR(100);
ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ALTER COLUMN jwt_iat TYPE INTEGER;

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



COMMENT ON TABLE grouper_prov_zoom_user IS 'table to load zoom users into a sql for reporting and deprovisioning';

COMMENT ON COLUMN grouper_prov_zoom_user.config_id IS 'zoom config id identifies which zoom external system is being loaded';

COMMENT ON COLUMN grouper_prov_zoom_user.member_id IS 'If the zoom user is mapped to a Grouper subject, this is the member uuid of the subject';

COMMENT ON COLUMN grouper_prov_zoom_user.id IS 'Zoom internal ID for this user (used in web services)';

COMMENT ON COLUMN grouper_prov_zoom_user.email IS 'Zoom friendly unique id for the user, also their email address';

COMMENT ON COLUMN grouper_prov_zoom_user.first_name IS 'First name of user';

COMMENT ON COLUMN grouper_prov_zoom_user.last_name IS 'Last name of user';

COMMENT ON COLUMN grouper_prov_zoom_user.type IS 'User type is 1 for basic, 2 for licensed, and 3 for on prem, 99 for none, see Zoom docs';

COMMENT ON COLUMN grouper_prov_zoom_user.pmi IS 'Zoom pmi, see zoom docs';

COMMENT ON COLUMN grouper_prov_zoom_user.timezone IS 'Timezone of users in zoom';

COMMENT ON COLUMN grouper_prov_zoom_user.verified IS 'If the user has been verified by zoom';

COMMENT ON COLUMN grouper_prov_zoom_user.created_at IS 'When the user was created in zoom';

COMMENT ON COLUMN grouper_prov_zoom_user.last_login_time IS 'When the user last logged in to zoom';

COMMENT ON COLUMN grouper_prov_zoom_user.language IS 'Language the user uses in zoom';

COMMENT ON COLUMN grouper_prov_zoom_user.status IS 'Status in zoom see docs';

COMMENT ON COLUMN grouper_prov_zoom_user.role_id IS 'Role ID in zoom see docs';


update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history) from 1 for 3500), db_version = 38 where object_name = 'Grouper';
commit;

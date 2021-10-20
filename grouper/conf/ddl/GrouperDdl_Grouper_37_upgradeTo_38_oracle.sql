ALTER TABLE GROUPER_PASSWORD ADD expires_millis NUMBER(38);
COMMENT ON COLUMN grouper_password.expires_millis IS 'millis since 1970 this password is going to expire';
ALTER TABLE GROUPER_PASSWORD ADD created_millis NUMBER(38);
COMMENT ON COLUMN grouper_password.created_millis IS 'millis since 1970 this password was created';
ALTER TABLE GROUPER_PASSWORD ADD member_id_who_set_password VARCHAR2(40);
COMMENT ON COLUMN grouper_password.member_id_who_set_password IS 'member id who set this password';

ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD attempt_millis NUMBER(38) NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.attempt_millis IS 'millis since 1970 this password was attempted';
ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD ip_address VARCHAR2(20) NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.ip_address IS 'ip address from where the password was attempted';
ALTER TABLE GROUPER_PASSWORD_RECENTLY_USED ADD status CHAR(1) NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.status IS 'status of the attempt. S/F/E etc';

ALTER TABLE grouper_password_recently_used ADD hibernate_version_number NUMBER(38) NOT NULL;
COMMENT ON COLUMN grouper_password_recently_used.hibernate_version_number IS 'hibernate version number';

ALTER TABLE grouper_password_recently_used MODIFY (jwt_jti VARCHAR2(100));
ALTER TABLE grouper_password_recently_used MODIFY (jwt_iat INTEGER);

ALTER TABLE grouper_password DROP COLUMN recent_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_logins;

CREATE TABLE grouper_prod_zoom_user
(
    config_id VARCHAR2(50) NOT NULL,
    member_id VARCHAR2(40),
    id VARCHAR2(40) NOT NULL,
    email VARCHAR2(256) NOT NULL,
    first_name VARCHAR2(256),
    last_name VARCHAR2(256),
    type NUMBER(38),
    pmi NUMBER(38),
    timezone VARCHAR2(100),
    verified NUMBER(38),
    created_at NUMBER(38),
    last_login_time NUMBER(38),
    language VARCHAR2(100),
    status NUMBER(38),
    role_id NUMBER(38),
    PRIMARY KEY (email)
);

CREATE INDEX grouper_zoom_us_config_id_idx ON grouper_prod_zoom_user (config_id);

CREATE UNIQUE INDEX grouper_zoom_user_email_idx ON grouper_prod_zoom_user (email, config_id);

CREATE UNIQUE INDEX grouper_zoom_user_id_idx ON grouper_prod_zoom_user (id, config_id);

CREATE INDEX grouper_zoom_us_member_id_idx ON grouper_prod_zoom_user (member_id, config_id);

COMMENT ON TABLE grouper_prod_zoom_user IS 'table to load zoom users into a sql for reporting and deprovisioning';

COMMENT ON COLUMN grouper_prod_zoom_user.config_id IS 'zoom config id identifies which zoom external system is being loaded';

COMMENT ON COLUMN grouper_prod_zoom_user.member_id IS 'If the zoom user is mapped to a Grouper subject, this is the member uuid of the subject';

COMMENT ON COLUMN grouper_prod_zoom_user.id IS 'Zoom internal ID for this user (used in web services)';

COMMENT ON COLUMN grouper_prod_zoom_user.email IS 'Zoom friendly unique id for the user, also their email address';

COMMENT ON COLUMN grouper_prod_zoom_user.first_name IS 'First name of user';

COMMENT ON COLUMN grouper_prod_zoom_user.last_name IS 'Last name of user';

COMMENT ON COLUMN grouper_prod_zoom_user.type IS 'User type is 1 for basic, 2 for licensed, and 3 for on prem, 99 for none, see Zoom docs';

COMMENT ON COLUMN grouper_prod_zoom_user.pmi IS 'Zoom pmi, see zoom docs';

COMMENT ON COLUMN grouper_prod_zoom_user.timezone IS 'Timezone of users in zoom';

COMMENT ON COLUMN grouper_prod_zoom_user.verified IS 'If the user has been verified by zoom';

COMMENT ON COLUMN grouper_prod_zoom_user.created_at IS 'When the user was created in zoom';

COMMENT ON COLUMN grouper_prod_zoom_user.last_login_time IS 'When the user last logged in to zoom';

COMMENT ON COLUMN grouper_prod_zoom_user.language IS 'Language the user uses in zoom';

COMMENT ON COLUMN grouper_prod_zoom_user.status IS 'Status in zoom see docs';

COMMENT ON COLUMN grouper_prod_zoom_user.role_id IS 'Role ID in zoom see docs';


update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history), 1, 3500), db_version = 38 where object_name = 'Grouper';
commit;

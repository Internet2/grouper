
CREATE TABLE grouper_prov_duo_user
(
    config_id VARCHAR2(50) NOT NULL,
    user_id VARCHAR2(40) NOT NULL,
    aliases VARCHAR2(4000),
    phones VARCHAR2(4000),
    is_push_enabled VARCHAR2(1),
    email VARCHAR2(200),
    first_name VARCHAR2(256),
    last_name VARCHAR2(256),
    is_enrolled VARCHAR2(1),
    last_directory_sync NUMBER(38),
    notes VARCHAR2(4000),
    real_name VARCHAR2(256),
    status VARCHAR2(40),
    user_name VARCHAR2(256) NOT NULL,
    created_at NUMBER(38) NOT NULL,
    last_login_time NUMBER(38),
    PRIMARY KEY (user_id)
);

CREATE INDEX grouper_duo_user_config_id_idx ON grouper_prov_duo_user (config_id);

CREATE UNIQUE INDEX grouper_duo_user_user_name_idx ON grouper_prov_duo_user (user_name, config_id);

CREATE UNIQUE INDEX grouper_duo_user_id_idx ON grouper_prov_duo_user (user_id, config_id);

COMMENT ON TABLE grouper_prov_duo_user IS 'table to load duo users into a sql for reporting and deprovisioning';

COMMENT ON COLUMN grouper_prov_duo_user.config_id IS 'duo config id identifies which duo external system is being loaded';

COMMENT ON COLUMN grouper_prov_duo_user.user_id IS 'duo internal ID for this user (used in web services)';

COMMENT ON COLUMN grouper_prov_duo_user.aliases IS 'comma separated list of aliases for the user';

COMMENT ON COLUMN grouper_prov_duo_user.phones IS 'comma separated list of phones for the user';

COMMENT ON COLUMN grouper_prov_duo_user.is_push_enabled IS 'is push enabled for one of the registered phones for the user';

COMMENT ON COLUMN grouper_prov_duo_user.email IS 'email address of the user';

COMMENT ON COLUMN grouper_prov_duo_user.first_name IS 'First name of user';

COMMENT ON COLUMN grouper_prov_duo_user.last_name IS 'Last name of user';

COMMENT ON COLUMN grouper_prov_duo_user.is_enrolled IS 'is user enrolled';

COMMENT ON COLUMN grouper_prov_duo_user.last_directory_sync IS 'last directory sync timestamp';

COMMENT ON COLUMN grouper_prov_duo_user.notes IS 'notes for the user';

COMMENT ON COLUMN grouper_prov_duo_user.real_name IS 'real name of user';

COMMENT ON COLUMN grouper_prov_duo_user.status IS 'status of the user. One of active, bypass, disabled, locked out, pending deletion';

COMMENT ON COLUMN grouper_prov_duo_user.user_name IS 'user name of the user';

COMMENT ON COLUMN grouper_prov_duo_user.created_at IS 'When the user was created in duo';

COMMENT ON COLUMN grouper_prov_duo_user.last_login_time IS 'When the user last logged in to duo';

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V41, ' || history), 1, 3500), db_version = 41 where object_name = 'Grouper';
commit;

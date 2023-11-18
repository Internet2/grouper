CREATE TABLE grouper_prov_duo_user
(
    config_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(40) NOT NULL,
    aliases VARCHAR(4000),
    phones VARCHAR(4000),
    is_push_enabled VARCHAR(1),
    email VARCHAR(200),
    first_name VARCHAR(256),
    last_name VARCHAR(256),
    is_enrolled VARCHAR(1),
    last_directory_sync BIGINT,
    notes VARCHAR(4000),
    real_name VARCHAR(256),
    status VARCHAR(40),
    user_name VARCHAR(256) NOT NULL,
    created_at BIGINT NOT NULL,
    last_login_time BIGINT,
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

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V41, ' || history) from 1 for 3500), db_version = 41 where object_name = 'Grouper';

commit;



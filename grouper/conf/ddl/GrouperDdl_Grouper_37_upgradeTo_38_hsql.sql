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

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history) from 1 for 3500), db_version = 38 where object_name = 'Grouper';
commit;


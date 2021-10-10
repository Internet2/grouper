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

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history), 1, 3500), db_version = 38 where object_name = 'Grouper';
commit;

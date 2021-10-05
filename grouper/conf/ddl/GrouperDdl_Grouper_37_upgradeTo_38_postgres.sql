ALTER TABLE GROUPER_PASSWORD ADD COLUMN expires_millis BIGINT COMMENT ON COLUMN grouper_password.expires_millis IS 'millis since 1970 this password is going to expire';
ALTER TABLE GROUPER_PASSWORD ADD COLUMN created_millis BIGINT COMMENT ON COLUMN grouper_password.create_millis IS 'millis since 1970 this password was created';
ALTER TABLE GROUPER_PASSWORD ADD COLUMN member_id_who_set_password VARCHAR(40) COMMENT ON COLUMN grouper_password.member_id_who_set_password IS 'member id who set this password';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history) from 1 for 3500), db_version = 38 where object_name = 'Grouper';
commit;

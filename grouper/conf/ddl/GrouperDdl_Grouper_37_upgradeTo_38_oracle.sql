ALTER TABLE GROUPER_PASSWORD ADD expires_millis NUMBER(38);
COMMENT ON COLUMN grouper_password.expires_millis IS 'millis since 1970 this password is going to expire';
ALTER TABLE GROUPER_PASSWORD ADD created_millis NUMBER(38);
COMMENT ON COLUMN grouper_password.created_millis IS 'millis since 1970 this password was created';
ALTER TABLE GROUPER_PASSWORD ADD member_id_who_set_password VARCHAR2(40);
COMMENT ON COLUMN grouper_password.member_id_who_set_password IS 'member id who set this password';

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history), 1, 3500), db_version = 38 where object_name = 'Grouper';
commit;

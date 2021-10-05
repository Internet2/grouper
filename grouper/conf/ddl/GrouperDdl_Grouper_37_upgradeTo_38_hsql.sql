ALTER TABLE GROUPER_PASSWORD ADD COLUMN expires_millis BIGINT;
ALTER TABLE GROUPER_PASSWORD ADD COLUMN created_millis BIGINT;
ALTER TABLE GROUPER_PASSWORD ADD COLUMN member_id_who_set_password VARCHAR(40);

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V38, ' || history) from 1 for 3500), db_version = 38 where object_name = 'Grouper';
commit;


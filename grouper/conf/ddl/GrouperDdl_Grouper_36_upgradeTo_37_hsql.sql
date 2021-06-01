ALTER TABLE GROUPER_SYNC_GROUP ADD COLUMN metadata_json VARCHAR(4000);

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V37, ' || history) from 1 for 3500), db_version = 37 where object_name = 'Grouper';
commit;


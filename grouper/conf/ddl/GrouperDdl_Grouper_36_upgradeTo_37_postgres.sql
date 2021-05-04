ALTER TABLE grouper_sync_group ADD COLUMN metadata_json VARCHAR(4000);

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V37, ' || history) from 1 for 3500), db_version = 37 where object_name = 'Grouper';
commit;

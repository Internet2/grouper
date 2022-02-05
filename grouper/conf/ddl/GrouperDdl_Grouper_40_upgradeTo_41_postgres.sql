ALTER TABLE grouper_sync_member ADD COLUMN metadata_json VARCHAR(4000);
COMMENT ON COLUMN grouper_sync_member.metadata_json IS 'additional metadata for member';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V41, ' || history) from 1 for 3500), db_version = 41 where object_name = 'Grouper';
commit;

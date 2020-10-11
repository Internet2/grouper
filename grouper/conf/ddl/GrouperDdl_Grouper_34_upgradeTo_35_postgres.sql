ALTER TABLE grouper_sync_log ADD COLUMN description_clob varchar(10000000);

ALTER TABLE grouper_sync_log ADD COLUMN description_bytes BIGINT;
	
COMMENT ON COLUMN grouper_sync_log.description_clob IS 'description for large data';

COMMENT ON COLUMN grouper_sync_log.description_bytes IS 'size of description in bytes';

UPDATE grouper_sync_log set description_bytes = length(description);
commit;

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V35, ' || history) from 1 for 3500), db_version = 35 where object_name = 'Grouper';
commit;

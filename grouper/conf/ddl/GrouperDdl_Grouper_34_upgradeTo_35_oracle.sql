ALTER TABLE grouper_sync_log ADD description_clob clob;

ALTER TABLE grouper_sync_log ADD description_bytes integer;
	
COMMENT ON COLUMN grouper_sync_log.description_clob IS 'description for large data';

COMMENT ON COLUMN grouper_sync_log.description_bytes IS 'size of description in bytes';

UPDATE grouper_sync_log set description_bytes = length(description);
commit;

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V35, ' || history), 1, 3500), db_version = 35 where object_name = 'Grouper';
commit;
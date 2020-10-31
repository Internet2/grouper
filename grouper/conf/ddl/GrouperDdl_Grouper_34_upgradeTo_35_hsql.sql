ALTER TABLE grouper_sync_log ADD COLUMN description_clob CLOB;

ALTER TABLE grouper_sync_log ADD COLUMN description_bytes BIGINT;
	
update grouper_sync_log set description_bytes = length(description);
commit;

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V35, ' || history) from 1 for 3500), db_version = 35 where object_name = 'Grouper';
commit;
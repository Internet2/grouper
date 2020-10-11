ALTER TABLE grouper_sync_log ADD COLUMN description_clob MEDIUMTEXT;

ALTER TABLE grouper_sync_log ADD COLUMN description_bytes BIGINT;
	
UPDATE grouper_sync_log set description_bytes = length(description);
commit;

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V35, ', history), 1, 3500), db_version = 35 where object_name = 'Grouper';
commit;
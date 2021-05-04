ALTER TABLE grouper_sync_group ADD COLUMN metadata_json VARCHAR(4000);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V37, ', history), 1, 3500), db_version = 37 where object_name = 'Grouper';
commit;


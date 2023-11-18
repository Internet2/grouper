ALTER TABLE grouper_loader_log ADD COLUMN job_message_clob MEDIUMTEXT;
ALTER TABLE grouper_loader_log ADD COLUMN job_message_bytes BIGINT;
  
UPDATE grouper_loader_log set job_message_bytes = length(job_message);
commit;

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V42, ', history), 1, 3500), db_version = 42 where object_name = 'Grouper';
commit;
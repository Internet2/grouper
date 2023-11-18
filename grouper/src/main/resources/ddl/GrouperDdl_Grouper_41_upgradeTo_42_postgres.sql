ALTER TABLE grouper_loader_log ADD COLUMN job_message_clob varchar(10000000);

ALTER TABLE grouper_loader_log ADD COLUMN job_message_bytes BIGINT;

UPDATE grouper_loader_log set job_message_bytes = length(job_message);
commit;

COMMENT ON COLUMN grouper_loader_log.job_message_clob IS 'Could be a status or error message or stack (over 3800 bytes)';
COMMENT ON COLUMN grouper_loader_log.job_message_bytes IS 'Number of bytes in the job message';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V42, ' || history) from 1 for 3500), db_version = 42 where object_name = 'Grouper';
commit;

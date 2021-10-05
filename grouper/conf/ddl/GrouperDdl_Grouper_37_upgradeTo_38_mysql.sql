ALTER TABLE GROUPER_PASSWORD ADD COLUMN expires_millis BIGINT;
ALTER TABLE GROUPER_PASSWORD ADD COLUMN created_millis BIGINT;
ALTER TABLE GROUPER_PASSWORD ADD COLUMN member_id_who_set_password VARCHAR(40);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V38, ', history), 1, 3500), db_version = 38 where object_name = 'Grouper';
commit;


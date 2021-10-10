ALTER TABLE grouper_password ADD COLUMN expires_millis BIGINT;
ALTER TABLE grouper_password ADD COLUMN created_millis BIGINT;
ALTER TABLE grouper_password ADD COLUMN member_id_who_set_password VARCHAR(40);

ALTER TABLE grouper_password_recently_used ADD COLUMN attempt_millis BIGINT NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN ip_address VARCHAR(20) NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN status CHAR(1) NOT NULL;
ALTER TABLE grouper_password_recently_used ADD COLUMN hibernate_version_number BIGINT NOT NULL;

ALTER TABLE grouper_password_recently_used MODIFY jwt_jti VARCHAR(100);
ALTER TABLE grouper_password_recently_used MODIFY jwt_iat INTEGER;

ALTER TABLE grouper_password DROP COLUMN recent_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_source_addresses;
ALTER TABLE grouper_password DROP COLUMN failed_logins;

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V38, ', history), 1, 3500), db_version = 38 where object_name = 'Grouper';
commit;


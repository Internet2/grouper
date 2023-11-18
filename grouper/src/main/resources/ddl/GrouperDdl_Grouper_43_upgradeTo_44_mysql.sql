update grouper_attribute_assign set disallowed='F' where disallowed is null;
update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;
commit;

ALTER TABLE grouper_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;
ALTER TABLE grouper_pit_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V44, ', history), 1, 3500), db_version = 44 where object_name = 'Grouper';
commit;


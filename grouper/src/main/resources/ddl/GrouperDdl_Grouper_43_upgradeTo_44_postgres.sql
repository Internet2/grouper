update grouper_attribute_assign set disallowed='F' where disallowed is null;
update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;
commit;

ALTER TABLE grouper_attribute_assign ALTER COLUMN disallowed SET NOT NULL;
ALTER TABLE grouper_attribute_assign ALTER COLUMN disallowed SET DEFAULT 'F';
ALTER TABLE grouper_pit_attribute_assign ALTER COLUMN disallowed SET NOT NULL;
ALTER TABLE grouper_pit_attribute_assign ALTER COLUMN disallowed SET DEFAULT 'F';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V44, ' || history) from 1 for 3500), db_version = 44 where object_name = 'Grouper';
commit;



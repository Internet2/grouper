update grouper_attribute_assign set disallowed='F' where disallowed is null;
update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;
commit;

ALTER TABLE grouper_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;
ALTER TABLE grouper_pit_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V44, ' || history), 1, 3500), db_version = 44 where object_name = 'Grouper';
commit;

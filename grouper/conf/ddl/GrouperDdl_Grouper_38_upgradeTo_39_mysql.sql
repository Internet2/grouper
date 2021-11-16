ALTER TABLE grouper_prov_zoom_user MODIFY COLUMN status VARCHAR(40) NULL;

ALTER TABLE grouper_members ADD COLUMN subject_resolution_eligible VARCHAR(1) DEFAULT 'T' not null;

CREATE INDEX member_eligible_idx ON GROUPER_MEMBERS (subject_resolution_eligible);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V39, ', history), 1, 3500), db_version = 39 where object_name = 'Grouper';
commit;


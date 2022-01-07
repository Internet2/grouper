ALTER TABLE grouper_members ADD COLUMN subject_identifier1 VARCHAR(255) NULL;
ALTER TABLE grouper_members ADD COLUMN subject_identifier2 VARCHAR(255) NULL;
ALTER TABLE grouper_members ADD COLUMN email0 VARCHAR(255) NULL;

CREATE INDEX member_subjidentifier1_idx ON grouper_members (subject_identifier1);

CREATE INDEX member_subjidentifier2_idx ON grouper_members (subject_identifier2);

CREATE INDEX member_email0_idx ON grouper_members (email0);

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V40, ', history), 1, 3500), db_version = 40 where object_name = 'Grouper';
commit;


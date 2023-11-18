update grouper_members set subject_resolution_eligible='T' where subject_resolution_eligible is null;
commit;

ALTER TABLE grouper_members ALTER COLUMN subject_resolution_eligible SET NOT NULL;
ALTER TABLE grouper_members ALTER COLUMN subject_resolution_eligible SET DEFAULT 'T';

ALTER TABLE grouper_members ADD COLUMN subject_identifier1 VARCHAR(255);
ALTER TABLE grouper_members ADD COLUMN subject_identifier2 VARCHAR(255);
ALTER TABLE grouper_members ADD COLUMN email0 VARCHAR(255);

CREATE INDEX member_subjidentifier1_idx ON grouper_members (subject_identifier1);

CREATE INDEX member_subjidentifier2_idx ON grouper_members (subject_identifier2);

CREATE INDEX member_email0_idx ON grouper_members (email0);

COMMENT ON COLUMN grouper_members.subject_identifier1 IS 'subject identifier of the subject';

COMMENT ON COLUMN grouper_members.subject_identifier2 IS 'subject identifier of the subject';

COMMENT ON COLUMN grouper_members.email0 IS 'email of the subject';

ALTER TABLE grouper_sync_member ADD COLUMN metadata_json VARCHAR(4000);
COMMENT ON COLUMN grouper_sync_member.metadata_json IS 'additional metadata for member';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V40, ' || history) from 1 for 3500), db_version = 40 where object_name = 'Grouper';
commit;



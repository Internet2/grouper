ALTER TABLE GROUPER_MEMBERS ADD subject_identifier1 VARCHAR2(255);
ALTER TABLE GROUPER_MEMBERS ADD subject_identifier2 VARCHAR2(255);
ALTER TABLE GROUPER_MEMBERS ADD email0 VARCHAR2(255);

CREATE INDEX member_subjidentifier1_idx ON grouper_members (subject_identifier1);

CREATE INDEX member_subjidentifier2_idx ON grouper_members (subject_identifier2);

CREATE INDEX member_email0_idx ON grouper_members (email0);

COMMENT ON COLUMN grouper_members.subject_identifier1 IS 'subject identifier of the subject';

COMMENT ON COLUMN grouper_members.subject_identifier2 IS 'subject identifier of the subject';

COMMENT ON COLUMN grouper_members.email0 IS 'email of the subject';

ALTER TABLE GROUPER_SYNC_MEMBER ADD metadata_json VARCHAR2(4000);
COMMENT ON COLUMN grouper_sync_member.metadata_json IS 'additional metadata for member';

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V40, ' || history), 1, 3500), db_version = 40 where object_name = 'Grouper';
commit;

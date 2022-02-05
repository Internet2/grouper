ALTER TABLE GROUPER_SYNC_MEMBER ADD metadata_json VARCHAR2(4000);
COMMENT ON COLUMN grouper_sync_member.metadata_json IS 'additional metadata for member';

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V41, ' || history), 1, 3500), db_version = 41 where object_name = 'Grouper';
commit;

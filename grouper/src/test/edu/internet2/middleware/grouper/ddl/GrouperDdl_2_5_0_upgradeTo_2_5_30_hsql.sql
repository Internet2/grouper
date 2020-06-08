ALTER TABLE GROUPER_MEMBERS
    ADD COLUMN subject_resolution_resolvable VARCHAR(1) DEFAULT 'T';

ALTER TABLE GROUPER_MEMBERS
    ADD COLUMN subject_resolution_deleted VARCHAR(1) DEFAULT 'F' BEFORE subject_resolution_resolvable;

CREATE INDEX member_resolvable_idx ON GROUPER_MEMBERS (subject_resolution_resolvable);

CREATE INDEX member_deleted_idx ON GROUPER_MEMBERS (subject_resolution_deleted);

update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;
update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;
commit;

update grouper_ddl set db_version = 33, last_updated = '2020/06/03 08:21:37', 
history = '2020/06/03 08:21:37: upgrade Grouper from V32 to V33, 2020/04/16 14:59:06: upgrade Grouper from V0 to V32, ' where object_name = 'Grouper';
commit;


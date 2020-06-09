ALTER TABLE GROUPER_MEMBERS
    ADD subject_resolution_deleted VARCHAR2(1) DEFAULT 'F';

ALTER TABLE GROUPER_MEMBERS
    ADD subject_resolution_resolvable VARCHAR2(1) DEFAULT 'T';

CREATE INDEX member_resolvable_idx ON GROUPER_MEMBERS (subject_resolution_resolvable);

CREATE INDEX member_deleted_idx ON GROUPER_MEMBERS (subject_resolution_deleted);
update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;
update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;
commit;

ALTER TABLE grouper_members
    ADD COLUMN subject_resolution_deleted VARCHAR(1) DEFAULT 'F' NULL AFTER context_id;

ALTER TABLE grouper_members
    ADD COLUMN subject_resolution_resolvable VARCHAR(1) DEFAULT 'T' NULL AFTER subject_resolution_deleted;

CREATE INDEX member_resolvable_idx ON grouper_members (subject_resolution_resolvable);

CREATE INDEX member_deleted_idx ON grouper_members (subject_resolution_deleted);

update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;
update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;
commit;

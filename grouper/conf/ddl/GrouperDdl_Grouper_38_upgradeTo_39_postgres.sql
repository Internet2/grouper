ALTER TABLE grouper_prov_zoom_user ALTER COLUMN status TYPE varchar(40);

ALTER TABLE grouper_members ADD COLUMN subject_resolution_eligible VARCHAR(1);

CREATE INDEX member_eligible_idx ON grouper_members (subject_resolution_eligible);

update grouper_members set subject_resolution_eligible='T' where subject_resolution_eligible is null;
commit;

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V39, ' || history) from 1 for 3500), db_version = 39 where object_name = 'Grouper';
commit;

update grouper_members set subject_resolution_eligible='T' where subject_resolution_eligible is null;
commit;

ALTER TABLE grouper_members ALTER COLUMN subject_resolution_eligible SET NOT NULL;
ALTER TABLE grouper_members ALTER COLUMN subject_resolution_eligible SET DEFAULT 'T';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V40, ' || history) from 1 for 3500), db_version = 40 where object_name = 'Grouper';
commit;

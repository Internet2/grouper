ALTER TABLE GROUPER_PROV_ZOOM_USER MODIFY STATUS VARCHAR2(40);

alter table grouper_members add ( subject_resolution_eligible  VARCHAR2(1) default 'T' not null);

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V39, ' || history), 1, 3500), db_version = 39 where object_name = 'Grouper';
commit;

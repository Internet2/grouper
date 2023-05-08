ALTER TABLE grouper_fields ADD COLUMN internal_id BIGINT;

CREATE UNIQUE INDEX grouper_fie_internal_id_idx ON grouper_fields (internal_id);

ALTER TABLE grouper_fields ADD CONSTRAINT fields_internal_id_unique UNIQUE USING INDEX grouper_fie_internal_id_idx;

COMMENT ON COLUMN grouper_fields.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)';

CREATE TABLE grouper_sql_cache_group (
  internal_id bigint NOT NULL,
  group_internal_id bigint NOT NULL,
  field_internal_id bigint not NULL,
  membership_size bigint not null,
  membership_hst_size bigint NOT NULL,
  created_on timestamp NOT NULL,
  enabled_on timestamp NOT NULL,
  disabled_on timestamp NULL,
  PRIMARY KEY (internal_id)
);
CREATE unique INDEX grouper_sql_cache_group1_idx ON grouper_sql_cache_group (group_internal_id, field_internal_id);

ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id);

COMMENT ON TABLE grouper_sql_cache_group IS 'Holds groups that are cacheable in SQL';

COMMENT ON COLUMN grouper_sql_cache_group.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)';

COMMENT ON COLUMN grouper_sql_cache_group.group_internal_id IS 'internal integer id for gruops which are cacheable';

COMMENT ON COLUMN grouper_sql_cache_group.field_internal_id IS 'internal integer id for the field which is the members or privilege which is cached';

COMMENT ON COLUMN grouper_sql_cache_group.membership_size IS 'approximate number of members of this group, used primarily to optimize batching';

COMMENT ON COLUMN grouper_sql_cache_group.membership_size_hst IS 'approximate number of rows of HST data for this group, used primarily to optimize batching';

COMMENT ON COLUMN grouper_sql_cache_group.created_on IS 'when this row was created (i.e. when this group started to be cached)';

COMMENT ON COLUMN grouper_sql_cache_group.enabled_on IS 'when this cache will be ready to use (do not use it while it is being populated)';

COMMENT ON COLUMN grouper_sql_cache_group.disabled_on IS 'when this cache should stop being used';

CREATE TABLE grouper_sql_cache_mship (
  internal_id bigint NOT NULL,
  sql_cache_group_internal_id bigint NOT NULL,
  member_internal_id bigint not NULL,
  flattened_add_timestamp timestamp not null,
  created_on timestamp NOT NULL,
  PRIMARY KEY (internal_id)
);

ALTER TABLE grouper_sql_cache_mship ADD CONSTRAINT grouper_sql_cache_mship1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group(internal_id);

CREATE INDEX grouper_sql_cache_mship1_idx ON grouper_sql_cache_mship (sql_cache_group_internal_id, flattened_add_timestamp);
CREATE INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship (member_internal_id, sql_cache_group_internal_id);

ALTER TABLE grouper_sql_cache_mship ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group(internal_id);

COMMENT ON TABLE grouper_sql_cache_mship IS 'Cached memberships based on group and list';

COMMENT ON COLUMN grouper_sql_cache_mship.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)';

COMMENT ON COLUMN grouper_sql_cache_mship.created_on IS 'when this cache row was created';

COMMENT ON COLUMN grouper_sql_cache_mship.flattened_add_timestamp IS 'when this member was last added to this group after not being a member before.  How long this member has been in this group';

COMMENT ON COLUMN grouper_sql_cache_mship.member_internal_id IS 'internal id of the member in this group';

COMMENT ON COLUMN grouper_sql_cache_mship.sql_cache_group_internal_id IS 'internal id of the group/list that this member is in';

CREATE TABLE grouper_sql_cache_mship_hst (
  internal_id bigint NOT NULL,
  sql_cache_group_internal_id bigint NOT NULL,
  member_internal_id bigint not NULL,
  start_time timestamp not null,
  end_time timestamp not null,
  PRIMARY KEY (internal_id)
);

ALTER TABLE grouper_sql_cache_mship_hst ADD CONSTRAINT grouper_sql_cache_msh_hst1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group (internal_id);

CREATE UNIQUE INDEX grouper_sql_cache_mshhst1_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, end_time);
CREATE UNIQUE INDEX grouper_sql_cache_mshhst2_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, start_time);
CREATE UNIQUE INDEX grouper_sql_cache_mshhst3_idx ON grouper_sql_cache_mship_hst (internal_id, sql_cache_group_internal_id, end_time);

COMMENT ON TABLE grouper_sql_cache_mship_hst IS 'Flattened point in time cache table for memberships or privileges';

COMMENT ON COLUMN grouper_sql_cache_mship_hst.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)';

COMMENT ON COLUMN grouper_sql_cache_mship_hst.end_time IS 'flattened membership end time';

COMMENT ON COLUMN grouper_sql_cache_mship_hst.start_time IS 'flattened membership start time';

COMMENT ON COLUMN grouper_sql_cache_mship_hst.member_internal_id IS 'member internal id of who this membership refers to';

COMMENT ON COLUMN grouper_sql_cache_mship_hst.internal_id IS 'internal id of which group/field this membership refers to';

CREATE VIEW grouper_sql_cache_group_v (group_name, list_name, membership_size, group_id, field_id, group_internal_id, field_internal_id) AS create view grouper_sql_cache_group_v as select gg.name group_name, gf.name list_name, membership_size,  gg.id group_id, gf.id field_id, gg.internal_id group_internal_id, gf.internal_id field_internal_id  from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg  where gscg.group_internal_id = gg.internal_id and gscg.field_internal_id = gf.internal_id ;

COMMENT ON VIEW grouper_sql_cache_group_v IS 'SQL cache group view';

COMMENT ON COLUMN grouper_sql_cache_group_v.group_name IS 'group_name: name of group';

COMMENT ON COLUMN grouper_sql_cache_group_v.list_name IS 'list_name: name of list: members or the privilege like admins';

COMMENT ON COLUMN grouper_sql_cache_group_v.membership_size IS 'membership_size: approximate number of memberships in the group';

COMMENT ON COLUMN grouper_sql_cache_group_v.group_id IS 'group_id: uuid of the group';

COMMENT ON COLUMN grouper_sql_cache_group_v.field_id IS 'field_id: uuid of the field';

COMMENT ON COLUMN grouper_sql_cache_group_v.group_internal_id IS 'group_internal_id: group internal id';

COMMENT ON COLUMN grouper_sql_cache_group_v.field_internal_id IS 'field_internal_id: field internal id';

CREATE VIEW grouper_sql_cache_mship_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, flattened_add_timestamp, group_id, field_id, mship_hst_internal_id, member_internal_id, group_internal_id, field_internal_id) AS  CREATE OR REPLACE VIEW public.grouper_sql_cache_mship_v  AS SELECT gg.name AS group_name, gf.name AS list_name, gm.subject_id, gm.subject_identifier0,  gm.subject_identifier1, gm.subject_identifier2, gm.subject_source, gscm.flattened_add_timestamp,  gg.id AS group_id, gf.id AS field_id, gscm.internal_id AS mship_internal_id, gm.internal_id AS member_internal_id,  gg.internal_id AS group_internal_id, gf.internal_id AS field_internal_id  FROM grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_fields gf,  grouper_groups gg, grouper_members gm  WHERE gscg.group_internal_id = gg.internal_id AND gscg.field_internal_id = gf.internal_id  AND gscm.sql_cache_group_internal_id = gscg.internal_id AND gscm.member_internal_id = gm.internal_id ;

COMMENT ON VIEW grouper_sql_cache_mship_v IS 'SQL cache mship view';

COMMENT ON COLUMN grouper_sql_cache_mship_v.group_name IS 'group_name: name of group';

COMMENT ON COLUMN grouper_sql_cache_mship_v.list_name IS 'list_name: name of list e.g. members or admins';

COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_id IS 'subject_id: subject id';

COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier0 IS 'subject_identifier0: subject identifier0 from subject source and members table';

COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier1 IS 'subject_identifier1: subject identifier1 from subject source and members table';

COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier2 IS 'subject_identifier2: subject identifier2 from subject source and members table';

COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_source IS 'subject_source: subject source id';

COMMENT ON COLUMN grouper_sql_cache_mship_v.flattened_add_timestamp IS 'flattened_add_timestamp: when this membership started';

COMMENT ON COLUMN grouper_sql_cache_mship_v.group_id IS 'group_id: uuid of group';

COMMENT ON COLUMN grouper_sql_cache_mship_v.field_id IS 'field_id: uuid of field';

COMMENT ON COLUMN grouper_sql_cache_mship_v.mship_hst_internal_id IS 'mship_hst_internal_id: history internal id';

COMMENT ON COLUMN grouper_sql_cache_mship_v.member_internal_id IS 'member_internal_id: member internal id';

COMMENT ON COLUMN grouper_sql_cache_mship_v.group_internal_id IS 'group_internal_id: group internal id';

COMMENT ON COLUMN grouper_sql_cache_mship_v.field_internal_id IS 'field_internal_id: field internal id';

CREATE VIEW grouper_sql_cache_mship_hst_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, start_time, end_time, group_id, field_id, mship_hst_internal_id, member_internal_id, group_internal_id, field_internal_id) AS  create or replace view public.grouper_sql_cache_mship_hst_v as select  gg.name as group_name, gf.name as list_name, gm.subject_id, gm.subject_identifier0, gm.subject_identifier1,  gm.subject_identifier2, gm.subject_source, gscmh.start_time, gscmh.end_time, gg.id as group_id,  gf.id as field_id, gscmh.internal_id as mship_hst_internal_id, gm.internal_id as member_internal_id,  gg.internal_id as group_internal_id, gf.internal_id as field_internal_id from  grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf,  grouper_groups gg, grouper_members gm where gscg.group_internal_id = gg.internal_id  and gscg.field_internal_id = gf.internal_id and gscmh.sql_cache_group_internal_id = gscg.internal_id  and gscmh.member_internal_id = gm.internal_id ) ;

COMMENT ON VIEW grouper_sql_cache_mship_hst_v IS 'SQL cache mship history view';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.group_name IS 'group_name: name of group';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.list_name IS 'list_name: name of list e.g. members or admins';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_id IS 'subject_id: subject id';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_identifier0 IS 'subject_identifier0: subject identifier0 from subject source and members table';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_identifier1 IS 'subject_identifier1: subject identifier1 from subject source and members table';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_identifier2 IS 'subject_identifier2: subject identifier2 from subject source and members table';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_source IS 'subject_source: subject source id';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.start_time IS 'start_time: when this membership started';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.end_time IS 'end_time: when this membership ended';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.group_id IS 'group_id: uuid of group';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.field_id IS 'field_id: uuid of field';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.mship_hst_internal_id IS 'mship_hst_internal_id: history internal id';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.member_internal_id IS 'member_internal_id: member internal id';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.group_internal_id IS 'group_internal_id: group internal id';

COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.field_internal_id IS 'field_internal_id: field internal id';


update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V46, ' || history) from 1 for 3500), db_version = 46 where object_name = 'Grouper';
commit;



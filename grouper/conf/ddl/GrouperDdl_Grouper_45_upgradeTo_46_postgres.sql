ALTER TABLE grouper_fields ADD COLUMN internal_id BIGINT;

CREATE UNIQUE INDEX grouper_fie_internal_id_idx ON grouper_fields (internal_id);

ALTER TABLE grouper_fields ADD CONSTRAINT fields_internal_id_unique UNIQUE USING INDEX grouper_fie_internal_id_idx;


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

ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_unq UNIQUE USING INDEX grouper_sql_cache_group1_idx;

ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id);

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

CREATE TABLE grouper_sql_cache_mship_hst (
  internal_id bigint NOT NULL,
  sql_cache_group_internal_id bigint NOT NULL,
  member_internal_id bigint not NULL,
  start_time timestamp not null,
  end_time timestamp not null,
  PRIMARY KEY (internal_id)
);

ALTER TABLE grouper_sql_cache_mship_hst ADD CONSTRAINT grouper_sql_cache_mshhst1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group(internal_id);

CREATE INDEX grouper_sql_cache_mshhst1_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, end_time);
CREATE INDEX grouper_sql_cache_mshhst2_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, start_time);
CREATE INDEX grouper_sql_cache_mshhst3_idx ON grouper_sql_cache_mship_hst (member_internal_id, sql_cache_group_internal_id, end_time);


create view grouper_sql_cache_group_v as select gg.name group_name, gf.name list_name, membership_size, 
gg.id group_id, gf.id field_id, gg.internal_id group_internal_id, gf.internal_id field_internal_id
from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg
where gscg.group_internal_id = gg.internal_id and gscg.field_internal_id = gf.internal_id;



update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V46, ' || history) from 1 for 3500), db_version = 46 where object_name = 'Grouper';
commit;



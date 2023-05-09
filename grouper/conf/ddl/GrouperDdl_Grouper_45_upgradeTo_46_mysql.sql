ALTER TABLE grouper_fields ADD COLUMN internal_id BIGINT AFTER context_id;

CREATE UNIQUE INDEX grouper_fie_internal_id_idx ON grouper_fields (internal_id);

ALTER TABLE grouper_groups ADD COLUMN internal_id BIGINT AFTER id_index;

CREATE UNIQUE INDEX grouper_grp_internal_id_idx ON grouper_groups (internal_id);

CREATE TABLE grouper_sql_cache_group
(
    internal_id BIGINT NOT NULL,
    group_internal_id BIGINT NOT NULL,
    field_internal_id BIGINT NOT NULL,
    membership_size BIGINT NOT NULL,
    membership_size_hst BIGINT NOT NULL,
    created_on DATETIME NOT NULL,
    enabled_on DATETIME NOT NULL,
    disabled_on DATETIME NOT NULL,
    PRIMARY KEY (internal_id)
);

CREATE UNIQUE INDEX grouper_sql_cache_group1_idx ON grouper_sql_cache_group (group_internal_id, field_internal_id);

CREATE TABLE grouper_sql_cache_mship
(
    created_on DATETIME NOT NULL,
    flattened_add_timestamp DATETIME NOT NULL,
    internal_id BIGINT NOT NULL,
    member_internal_id BIGINT NOT NULL,
    sql_cache_group_internal_id BIGINT NOT NULL
);

CREATE INDEX grouper_sql_cache_mship1_idx ON grouper_sql_cache_mship (sql_cache_group_internal_id, flattened_add_timestamp);

CREATE INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship (member_internal_id, sql_cache_group_internal_id);

CREATE TABLE grouper_sql_cache_mship_hst
(
    internal_id BIGINT NOT NULL,
    end_time DATETIME NOT NULL,
    start_time DATETIME NOT NULL,
    sql_cache_group_internal_id BIGINT NOT NULL,
    member_internal_id BIGINT NOT NULL,
    PRIMARY KEY (internal_id, sql_cache_group_internal_id, member_internal_id)
);

CREATE UNIQUE INDEX grouper_sql_cache_msh_hst1_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, end_time);

CREATE UNIQUE INDEX grouper_sql_cache_msh_hst2_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, start_time);

CREATE UNIQUE INDEX grouper_sql_cache_msh_hst3_idx ON grouper_sql_cache_mship_hst (internal_id, sql_cache_group_internal_id, end_time);

ALTER TABLE grouper_sql_cache_group
    ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields (internal_id);

ALTER TABLE grouper_sql_cache_mship
    ADD CONSTRAINT grouper_sql_cache_mship1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group (internal_id);

ALTER TABLE grouper_sql_cache_mship_hst
    ADD CONSTRAINT grouper_sql_cache_msh_hst1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group (internal_id);

CREATE VIEW grouper_sql_cache_group_v (group_name, list_name, membership_size, group_id, field_id, group_internal_id, field_internal_id) AS select gg.name group_name, gf.name list_name, membership_size,  gg.id group_id, gf.id field_id, gg.internal_id group_internal_id, gf.internal_id field_internal_id  from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg  where gscg.group_internal_id = gg.internal_id and gscg.field_internal_id = gf.internal_id ;

CREATE VIEW grouper_sql_cache_mship_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, flattened_add_timestamp, group_id, field_id, mship_hst_internal_id, member_internal_id, group_internal_id, field_internal_id) AS SELECT gg.name AS group_name, gf.name AS list_name, gm.subject_id, gm.subject_identifier0,  gm.subject_identifier1, gm.subject_identifier2, gm.subject_source, gscm.flattened_add_timestamp,  gg.id AS group_id, gf.id AS field_id, gscm.internal_id AS mship_internal_id, gm.internal_id AS member_internal_id,  gg.internal_id AS group_internal_id, gf.internal_id AS field_internal_id  FROM grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_fields gf,  grouper_groups gg, grouper_members gm  WHERE gscg.group_internal_id = gg.internal_id AND gscg.field_internal_id = gf.internal_id  AND gscm.sql_cache_group_internal_id = gscg.internal_id AND gscm.member_internal_id = gm.internal_id ;

CREATE VIEW grouper_sql_cache_mship_hst_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, start_time, end_time, group_id, field_id, mship_hst_internal_id, member_internal_id, group_internal_id, field_internal_id) AS select  gg.name as group_name, gf.name as list_name, gm.subject_id, gm.subject_identifier0, gm.subject_identifier1,  gm.subject_identifier2, gm.subject_source, gscmh.start_time, gscmh.end_time, gg.id as group_id,  gf.id as field_id, gscmh.internal_id as mship_hst_internal_id, gm.internal_id as member_internal_id,  gg.internal_id as group_internal_id, gf.internal_id as field_internal_id from  grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf,  grouper_groups gg, grouper_members gm where gscg.group_internal_id = gg.internal_id  and gscg.field_internal_id = gf.internal_id and gscmh.sql_cache_group_internal_id = gscg.internal_id  and gscmh.member_internal_id = gm.internal_id ;



update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V46, ', history), 1, 3500), db_version = 46 where object_name = 'Grouper';
commit;



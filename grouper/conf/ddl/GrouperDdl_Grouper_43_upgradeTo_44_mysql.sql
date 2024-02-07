update grouper_attribute_assign set disallowed='F' where disallowed is null;
update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;
commit;

ALTER TABLE grouper_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;
ALTER TABLE grouper_pit_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;

CREATE TABLE grouper_sync_dep_group_user (
  id_index BIGINT NOT NULL,
  grouper_sync_id VARCHAR(40) NOT NULL,
  group_id VARCHAR(40) NOT NULL,
  field_id VARCHAR(40) NOT NULL,
  PRIMARY KEY (id_index)
);

CREATE INDEX grouper_sync_dep_grp_user_idx0 ON grouper_sync_dep_group_user (grouper_sync_id);

CREATE UNIQUE INDEX grouper_sync_dep_grp_user_idx1 ON grouper_sync_dep_group_user (grouper_sync_id,group_id,field_id);

CREATE TABLE grouper_sync_dep_group_group (
  id_index BIGINT NOT NULL,
  grouper_sync_id VARCHAR(40) NOT NULL,
  group_id VARCHAR(40) NOT NULL,
  field_id VARCHAR(40) NOT NULL,
  provisionable_group_id VARCHAR(40) NOT NULL,
  PRIMARY KEY (id_index)
);

CREATE INDEX grouper_sync_dep_grp_grp_idx0 ON grouper_sync_dep_group_group (grouper_sync_id);

CREATE UNIQUE INDEX grouper_sync_dep_grp_grp_idx1 ON grouper_sync_dep_group_group (grouper_sync_id,group_id,field_id,provisionable_group_id);

CREATE INDEX grouper_sync_dep_grp_grp_idx2 ON grouper_sync_dep_group_group (grouper_sync_id,provisionable_group_id);

CREATE INDEX grouper_sync_dep_grp_grp_idx3 ON grouper_sync_dep_group_group (grouper_sync_id,group_id,field_id);

alter table grouper_sync_dep_group_user
    add CONSTRAINT grouper_sync_dep_grp_user_fk_0 FOREIGN KEY (group_id) REFERENCES grouper_groups(id) ON DELETE CASCADE;

alter table grouper_sync_dep_group_user
    add CONSTRAINT grouper_sync_dep_grp_user_fk_1 FOREIGN KEY (field_id) REFERENCES grouper_fields(id) ON DELETE CASCADE;

alter table grouper_sync_dep_group_user
    add CONSTRAINT grouper_sync_dep_grp_user_fk_2 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id) ON DELETE CASCADE;

alter table grouper_sync_dep_group_group
    add CONSTRAINT grouper_sync_dep_grp_grp_fk_0 FOREIGN KEY (group_id) REFERENCES grouper_groups(id) ON DELETE CASCADE;
  
alter table grouper_sync_dep_group_group
    add CONSTRAINT grouper_sync_dep_grp_grp_fk_1 FOREIGN KEY (provisionable_group_id) REFERENCES grouper_groups(id) ON DELETE CASCADE;
  
alter table grouper_sync_dep_group_group
    add CONSTRAINT grouper_sync_dep_grp_grp_fk_2 FOREIGN KEY (field_id) REFERENCES grouper_fields(id) ON DELETE CASCADE;

alter table grouper_sync_dep_group_group
    add CONSTRAINT grouper_sync_dep_grp_grp_fk_3 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id) ON DELETE CASCADE;    
    

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V44, ', history), 1, 3500), db_version = 44 where object_name = 'Grouper';
commit;


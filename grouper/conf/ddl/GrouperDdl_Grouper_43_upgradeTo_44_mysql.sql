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
    add CONSTRAINT grouper_sync_dep_grp_user_fk_2 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id);

alter table grouper_sync_dep_group_group
    add CONSTRAINT grouper_sync_dep_grp_grp_fk_1 FOREIGN KEY (provisionable_group_id) REFERENCES grouper_groups(id);
  
alter table grouper_sync_dep_group_group
    add CONSTRAINT grouper_sync_dep_grp_grp_fk_3 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id);    
    
CREATE TABLE grouper_prov_scim_user
(
    config_id VARCHAR(50) NOT NULL,
    active VARCHAR(1) NULL,
    cost_center VARCHAR(256) NULL,
    department VARCHAR(256) NULL,
    display_name VARCHAR(256) NULL,
    division VARCHAR(256) NULL,
    email_type VARCHAR(256) NULL,
    email_value VARCHAR(256) NULL,
    email_type2 VARCHAR(256) NULL,
    email_value2 VARCHAR(256) NULL,
    employee_number VARCHAR(256) NULL,
    external_id VARCHAR(256) NULL,
    family_name VARCHAR(256) NULL,
    formatted_name VARCHAR(256) NULL,
    given_name VARCHAR(256) NULL,
    id VARCHAR(256) NOT NULL,
    middle_name VARCHAR(256) NULL,
    phone_number VARCHAR(256) NULL,
    phone_number_type VARCHAR(256) NULL,
    phone_number2 VARCHAR(256) NULL,
    phone_number_type2 VARCHAR(256) NULL,
    the_schemas VARCHAR(256) NULL,
    title VARCHAR(256) NULL,
    user_name VARCHAR(256) NULL,
    user_type VARCHAR(256) NULL,
    PRIMARY KEY (config_id, id(180))
);
 
CREATE INDEX grouper_prov_scim_user_idx1 ON grouper_prov_scim_user (email_value, config_id);
 
CREATE INDEX grouper_prov_scim_user_idx2 ON grouper_prov_scim_user (user_name, config_id);
 
CREATE TABLE grouper_prov_scim_user_attr
( 
    config_id VARCHAR(50) NOT NULL,
    id VARCHAR(256) NOT NULL,
    attribute_name VARCHAR(256) NULL,
    attribute_value VARCHAR(4000) NULL
);
 
ALTER TABLE  grouper_prov_scim_user_attr ADD CONSTRAINT grouper_prov_scim_usat_fk FOREIGN KEY (config_id, id) REFERENCES grouper_prov_scim_user(config_id, id) on delete cascade;
 
CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_name(100));
 
CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_value(100));
 
update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V44, ', history), 1, 3500), db_version = 44 where object_name = 'Grouper';
commit;


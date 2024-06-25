update grouper_attribute_assign set disallowed='F' where disallowed is null;
update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;
commit;

ALTER TABLE grouper_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;
ALTER TABLE grouper_pit_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;

CREATE TABLE grouper_sync_dep_group_user (
  id_index NUMBER(38) NOT NULL,
  grouper_sync_id VARCHAR2(40) NOT NULL,
  group_id VARCHAR2(40) NOT NULL,
  field_id VARCHAR2(40) NOT NULL,
  PRIMARY KEY (id_index)
);

CREATE INDEX grouper_sync_dep_grp_user_idx0 ON grouper_sync_dep_group_user (grouper_sync_id);

CREATE UNIQUE INDEX grouper_sync_dep_grp_user_idx1 ON grouper_sync_dep_group_user (grouper_sync_id,group_id,field_id);

CREATE TABLE grouper_sync_dep_group_group (
  id_index NUMBER(38) NOT NULL,
  grouper_sync_id VARCHAR2(40) NOT NULL,
  group_id VARCHAR2(40) NOT NULL,
  field_id VARCHAR2(40) NOT NULL,
  provisionable_group_id VARCHAR2(40) NOT NULL,
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

COMMENT ON TABLE grouper_sync_dep_group_user IS 'Groups are listed that are used in user translations.  Users will need to be recalced if there are changes (not membership recalc)';

COMMENT ON COLUMN grouper_sync_dep_group_user.id_index IS 'primary key';

COMMENT ON COLUMN grouper_sync_dep_group_user.grouper_sync_id IS 'provisioner';

COMMENT ON COLUMN grouper_sync_dep_group_user.group_id IS 'group uuid';

COMMENT ON COLUMN grouper_sync_dep_group_user.field_id IS 'field uuid';

COMMENT ON TABLE grouper_sync_dep_group_group IS 'Groups are listed that are used in group translations.  Provisionable groups will need to be recalced if there are changes (not membership recalc)';

COMMENT ON COLUMN grouper_sync_dep_group_group.id_index IS 'primary key';

COMMENT ON COLUMN grouper_sync_dep_group_group.grouper_sync_id IS 'provisioner';

COMMENT ON COLUMN grouper_sync_dep_group_group.group_id IS 'group uuid';

COMMENT ON COLUMN grouper_sync_dep_group_group.field_id IS 'field uuid';

COMMENT ON COLUMN grouper_sync_dep_group_group.provisionable_group_id IS 'group uuid of the provisionable group that uses this other group as a role';

CREATE TABLE grouper_prov_scim_user
(
    config_id VARCHAR2(50) NOT NULL,
    active VARCHAR2(1),
    cost_center VARCHAR2(256),
    department VARCHAR2(256),
    display_name VARCHAR2(256),
    division VARCHAR2(256),
    email_type VARCHAR2(256),
    email_value VARCHAR2(256),
    email_type2 VARCHAR2(256),
    email_value2 VARCHAR2(256),
    employee_number VARCHAR2(256),
    external_id VARCHAR2(256),
    family_name VARCHAR2(256),
    formatted_name VARCHAR2(256),
    given_name VARCHAR2(256),
    id VARCHAR2(256) NOT NULL,
    middle_name VARCHAR2(256),
    phone_number VARCHAR2(256),
    phone_number_type VARCHAR2(256),
    phone_number2 VARCHAR2(256),
    phone_number_type2 VARCHAR2(256),
    the_schemas VARCHAR2(256),
    title VARCHAR2(256),
    user_name VARCHAR2(256),
    user_type VARCHAR2(256),
    PRIMARY KEY (config_id, id)
);
 
CREATE INDEX grouper_prov_scim_user_idx1 ON grouper_prov_scim_user (email_value, config_id);
 
CREATE INDEX grouper_prov_scim_user_idx2 ON grouper_prov_scim_user (user_name, config_id);
 
CREATE TABLE grouper_prov_scim_user_attr
( 
    config_id VARCHAR2(50) NOT NULL,
    id VARCHAR2(256) NOT NULL,
    attribute_name VARCHAR2(256) NULL,
    attribute_value VARCHAR2(4000) NULL,
    PRIMARY KEY (config_id, id, attribute_name, attribute_value)
);
 
ALTER TABLE  grouper_prov_scim_user_attr ADD CONSTRAINT grouper_prov_scim_usat_fk FOREIGN KEY (config_id, id) REFERENCES grouper_prov_scim_user(config_id, id) on delete cascade;
 
CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id, config_id, attribute_name);
 
CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id, config_id, attribute_value);
 
COMMENT ON TABLE grouper_prov_scim_user IS 'table to load scim users into a sql for reporting, provisioning, and deprovisioning';
 
COMMENT ON COLUMN grouper_prov_scim_user.config_id IS 'scim config id identifies which scim external system is being loaded';
 
COMMENT ON COLUMN grouper_prov_scim_user.id IS 'scim internal ID for this user (used in web services)';
 
COMMENT ON COLUMN grouper_prov_scim_user.active IS 'Is user active';
 
COMMENT ON COLUMN grouper_prov_scim_user.cost_center IS 'cost center for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.department IS 'department for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.display_name IS 'display name for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.division IS 'divsion for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.email_type IS 'email type for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.email_value IS 'email value for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.email_type2 IS 'email type2 for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.email_value2 IS 'email value2 for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.employee_number IS 'employee number for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.external_id IS 'external id for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.family_name IS 'family name for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.formatted_name IS 'formatted name for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.given_name IS 'given name for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.id IS 'id for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.middle_name IS 'middle name for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.phone_number IS 'phone number for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.phone_number_type IS 'phone number type for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.phone_number2 IS 'phone number2 for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.phone_number_type2 IS 'phone number type2 for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.the_schemas IS 'schemas for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.title IS 'title for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.user_name IS 'user name for the user';
 
COMMENT ON COLUMN grouper_prov_scim_user.user_type IS 'user type for the user';
 
COMMENT ON TABLE grouper_prov_scim_user_attr IS 'table to load scim user attributes into a sql for reporting, provisioning, and deprovisioning';
 
COMMENT ON COLUMN grouper_prov_scim_user_attr.config_id IS 'scim config id identifies which scim external system is being loaded';
 
COMMENT ON COLUMN grouper_prov_scim_user_attr.id IS 'scim internal ID for this user (used in web services)';
 
COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_name IS 'scim user attribute name';
 
COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_value IS 'scim user attribute value';
 
 


update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V44, ' || history), 1, 3500), db_version = 44 where object_name = 'Grouper';
commit;

CREATE TABLE grouper_prov_adobe_user
(    
    config_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    email VARCHAR(256) NOT NULL,
    username VARCHAR(256) NOT NULL,
    status VARCHAR(30) NULL,
    "type" VARCHAR(30) NULL,
    firstname VARCHAR(100) NULL,
    lastname VARCHAR(100) NULL,
    domain VARCHAR(100) NULL,
    country VARCHAR(2) NULL,
    PRIMARY KEY (config_id, user_id)
);
  
CREATE INDEX grouper_prov_adobe_user_idx1 ON grouper_prov_adobe_user (email, config_id);
CREATE INDEX grouper_prov_adobe_user_idx2 ON grouper_prov_adobe_user (username, config_id);
 
COMMENT ON TABLE grouper_prov_adobe_user IS 'table to load adobe users into a sql for reporting, provisioning, and deprovisioning';
 
COMMENT ON COLUMN grouper_prov_adobe_user.config_id IS 'adobe config id identifies which adobe external system is being loaded';
 
COMMENT ON COLUMN grouper_prov_adobe_user.user_id IS 'adobe user id for this user (used in web services)';
 
COMMENT ON COLUMN grouper_prov_adobe_user.email IS 'email address';
 
COMMENT ON COLUMN grouper_prov_adobe_user.username IS 'username the user';
 
COMMENT ON COLUMN grouper_prov_adobe_user.status IS 'adobe status for the user';
 
COMMENT ON COLUMN grouper_prov_adobe_user."type" IS 'type for the user';
 
COMMENT ON COLUMN grouper_prov_adobe_user.firstname IS 'first name for the user';

COMMENT ON COLUMN grouper_prov_adobe_user.lastname IS 'last name for the user';

COMMENT ON COLUMN grouper_prov_adobe_user.domain IS 'domain for the user';

COMMENT ON COLUMN grouper_prov_adobe_user.country IS 'country for the user';

CREATE TABLE grouper_prov_adobe_group
(
    config_id VARCHAR(100) NOT NULL,
    group_id BIGINT NOT NULL,
    name VARCHAR(2000) NOT NULL,
    "type" VARCHAR(100) NULL,
    product_name VARCHAR(2000) NULL,
    member_count BIGINT NULL,
    license_quota BIGINT NULL,
    PRIMARY KEY (config_id, group_id)
);
 
CREATE INDEX grouper_prov_adobe_group_idx1 ON grouper_prov_adobe_group (name, config_id);

COMMENT ON TABLE grouper_prov_adobe_group IS 'table to load adobe groups into a sql for reporting, provisioning, and deprovisioning';
 
COMMENT ON COLUMN grouper_prov_adobe_group.config_id IS 'adobe config id identifies which adobe external system is being loaded';
 
COMMENT ON COLUMN grouper_prov_adobe_group.group_id IS 'adobe group id for this group (used in web services)';
 
COMMENT ON COLUMN grouper_prov_adobe_group.name IS 'group name';
 
COMMENT ON COLUMN grouper_prov_adobe_group."type" IS 'type for the user';
 
COMMENT ON COLUMN grouper_prov_adobe_group.product_name IS 'product name for the group';

COMMENT ON COLUMN grouper_prov_adobe_group.member_count IS 'member count for the group';

COMMENT ON COLUMN grouper_prov_adobe_group.license_quota IS 'license quota for the group';

CREATE TABLE grouper_prov_adobe_membership
(
    config_id VARCHAR(100) NOT NULL,
    group_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    PRIMARY KEY (config_id, group_id, user_id)
);

ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk1 FOREIGN KEY (config_id, group_id) REFERENCES grouper_prov_adobe_group(config_id, group_id) on delete cascade;
ALTER TABLE  grouper_prov_adobe_membership ADD CONSTRAINT grouper_prov_adobe_mship_fk2 FOREIGN KEY (config_id, user_id) REFERENCES grouper_prov_adobe_user(config_id, user_id) on delete cascade;

COMMENT ON TABLE grouper_prov_adobe_membership IS 'table to load adobe memberships into a sql for reporting, provisioning, and deprovisioning';
 
COMMENT ON COLUMN grouper_prov_adobe_membership.config_id IS 'adobe config id identifies which adobe external system is being loaded';
 
COMMENT ON COLUMN grouper_prov_adobe_membership.group_id IS 'adobe group id for this membership';
 
COMMENT ON COLUMN grouper_prov_adobe_membership.user_id IS 'adobe user id for this membership';

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V48, ', history), 1, 3500), db_version = 48 where object_name = 'Grouper';
commit;

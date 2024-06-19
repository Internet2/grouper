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
    schemas VARCHAR(256) NULL,
    title VARCHAR(256) NULL,
    user_name VARCHAR(256) NULL,
    user_type VARCHAR(256) NULL,
    PRIMARY KEY (config_id, id)
);

CREATE INDEX grouper_prov_scim_user_idx1 ON grouper_prov_scim_user (email_value, config_id);

CREATE INDEX grouper_prov_scim_user_idx2 ON grouper_prov_scim_user (user_name, config_id);

CREATE TABLE grouper_prov_scim_user_attr
( 
    config_id VARCHAR(50) NOT NULL
    id VARCHAR(256) NOT NULL,
    attribute_name VARCHAR(256) NULL,
    attribute_value VARCHAR(4000) NULL,
    PRIMARY KEY (config_id, id, attribute_name, attribute_value)
);

ALTER TABLE  grouper_prov_scim_user_attr ADD CONSTRAINT grouper_prov_scim_usat_fk FOREIGN KEY (config_id, id) REFERENCES grouper_prov_scim_user(config_id, id) on delete cascade;

CREATE INDEX grouper_prov_scim_usat_idx1 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_name(100));

CREATE INDEX grouper_prov_scim_usat_idx2 ON grouper_prov_scim_user_attr (id(100), config_id, attribute_value(100));

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V47, ', history), 1, 3500), db_version = 47 where object_name = 'Grouper';
commit;

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
    schemas VARCHAR2(256),
    title VARCHAR2(256),
    user_name VARCHAR2(256),
    user_type VARCHAR2(256),
    PRIMARY KEY (config_id, id)
);

CREATE INDEX grouper_prov_scim_user_idx1 ON grouper_prov_scim_user (email_value, config_id);

CREATE INDEX grouper_prov_scim_user_idx2 ON grouper_prov_scim_user (user_name, config_id);

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

COMMENT ON COLUMN grouper_prov_scim_user.schemas IS 'schemas for the user';

COMMENT ON COLUMN grouper_prov_scim_user.title IS 'title for the user';

COMMENT ON COLUMN grouper_prov_scim_user.user_name IS 'user name for the user';

COMMENT ON COLUMN grouper_prov_scim_user.user_type IS 'user type for the user';

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

COMMENT ON TABLE grouper_prov_scim_user_attr IS 'table to load scim user attributes into a sql for reporting, provisioning, and deprovisioning';

COMMENT ON COLUMN grouper_prov_scim_user_attr.config_id IS 'scim config id identifies which scim external system is being loaded';

COMMENT ON COLUMN grouper_prov_scim_user_attr.id IS 'scim internal ID for this user (used in web services)';

COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_name IS 'scim user attribute name';

COMMENT ON COLUMN grouper_prov_scim_user_attr.attribute_value IS 'scim user attribute value';

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V47, ' || history), 1, 3500), db_version = 47 where object_name = 'Grouper';
commit;



CREATE TABLE grouper_prov_azure_user
(
    config_id VARCHAR2(50) NOT NULL,
    account_enabled VARCHAR2(1) NULL,
    display_name VARCHAR2(256) NULL,
    id VARCHAR2(256) NOT NULL,
    mail_nickname VARCHAR2(256) NULL,
    on_premises_immutable_id VARCHAR2(256) NULL,
    user_principal_name VARCHAR2(256) NULL,
    PRIMARY KEY (config_id, id)
);
  
CREATE INDEX grouper_prov_azure_user_idx1 ON grouper_prov_azure_user (user_principal_name, config_id);
 
COMMENT ON TABLE grouper_prov_azure_user IS 'table to load azure users into a sql for reporting, provisioning, and deprovisioning';
 
COMMENT ON COLUMN grouper_prov_azure_user.config_id IS 'azure config id identifies which azure external system is being loaded';
 
COMMENT ON COLUMN grouper_prov_azure_user.id IS 'azure internal ID for this user (used in web services)';
 
COMMENT ON COLUMN grouper_prov_azure_user.account_enabled IS 'Is account enabled';
 
COMMENT ON COLUMN grouper_prov_azure_user.mail_nickname IS 'mail nickname for the user';
 
COMMENT ON COLUMN grouper_prov_azure_user.on_premises_immutable_id IS 'in premises immutable id for the user';
 
COMMENT ON COLUMN grouper_prov_azure_user.display_name IS 'display name for the user';
 
COMMENT ON COLUMN grouper_prov_azure_user.user_principal_name IS 'user principal name for the user';
 
update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V47, ' || history), 1, 3500), db_version = 47 where object_name = 'Grouper';
commit;

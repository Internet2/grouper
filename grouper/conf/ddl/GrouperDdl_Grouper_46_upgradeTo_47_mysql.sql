CREATE TABLE grouper_prov_azure_user
(
    config_id VARCHAR(50) NOT NULL,
    account_enabled VARCHAR(1) NULL,
    display_name VARCHAR(256) NULL,
    id VARCHAR(180) NOT NULL,
    mail_nickname VARCHAR(256) NULL,
    on_premises_immutable_id VARCHAR(256) NULL,
    user_principal_name VARCHAR(256) NULL,
    PRIMARY KEY (config_id, id)
);
 
CREATE INDEX grouper_prov_azure_user_idx1 ON grouper_prov_azure_user (user_principal_name, config_id);
 
update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V47, ', history), 1, 3500), db_version = 47 where object_name = 'Grouper';
commit;
CREATE TABLE grouper_ddl_worker
(
    id VARCHAR(40) NOT NULL,
    grouper VARCHAR(40) NOT NULL,
    worker_uuid VARCHAR(40) NOT NULL,
    heartbeat TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_ddl_worker_grp_idx ON grouper_ddl_worker (grouper);



DROP VIEW grouper_groups_v IF EXISTS cascade;

DROP VIEW grouper_roles_v IF EXISTS cascade;
ALTER TABLE GROUPER_GROUPS
    ADD COLUMN disabled_timestamp BIGINT;

ALTER TABLE GROUPER_GROUPS
    ADD COLUMN enabled_timestamp BIGINT BEFORE disabled_timestamp;

ALTER TABLE GROUPER_GROUPS
    ADD COLUMN enabled VARCHAR(1) DEFAULT 'T' BEFORE enabled_timestamp;

CREATE TABLE grouper_config
(
    id VARCHAR(40) NOT NULL,
    config_file_name VARCHAR(100) NOT NULL,
    config_key VARCHAR(400) NOT NULL,
    config_value VARCHAR(4000),
    config_comment VARCHAR(4000),
    config_file_hierarchy VARCHAR(50) NOT NULL,
    config_encrypted VARCHAR(1) NOT NULL,
    config_sequence BIGINT NOT NULL,
    config_version_index BIGINT,
    last_updated BIGINT NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grpconfig_config_file_idx ON grouper_config (config_file_name, last_updated);

CREATE INDEX grpconfig_config_key_idx ON grouper_config (config_key, config_file_name);

CREATE INDEX grpconfig_last_updated_idx ON grouper_config (last_updated);

CREATE UNIQUE INDEX grpconfig_unique_idx ON grouper_config (config_file_name, config_file_hierarchy, config_key, config_sequence);

CREATE TABLE grouper_password
(
    id VARCHAR(40) NOT NULL,
    username VARCHAR(255) NOT NULL,
    member_id VARCHAR(40),
    entity_type VARCHAR(20),
    is_hashed VARCHAR(1) NOT NULL,
    encryption_type VARCHAR(20) NOT NULL,
    the_salt VARCHAR(255),
    the_password VARCHAR(4000),
    application VARCHAR(20) NOT NULL,
    allowed_from_cidrs VARCHAR(4000),
    recent_source_addresses VARCHAR(4000),
    failed_source_addresses VARCHAR(4000),
    last_authenticated BIGINT,
    last_edited BIGINT NOT NULL,
    failed_logins VARCHAR(4000),
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grppassword_username_idx ON grouper_password (username, application);

CREATE TABLE grouper_password_recently_used
(
    id VARCHAR(40) NOT NULL,
    grouper_password_id VARCHAR(40) NOT NULL,
    jwt_jti VARCHAR(100) NOT NULL,
    jwt_iat INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE grouper_sync
(
    id VARCHAR(40) NOT NULL,
    sync_engine VARCHAR(50),
    provisioner_name VARCHAR(100) NOT NULL,
    group_count INTEGER,
    user_count INTEGER,
    records_count INTEGER,
    incremental_index BIGINT,
    incremental_timestamp TIMESTAMP,
    last_incremental_sync_run TIMESTAMP,
    last_full_sync_run TIMESTAMP,
    last_full_metadata_sync_run TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_eng_idx ON grouper_sync (sync_engine, provisioner_name);

CREATE UNIQUE INDEX grouper_sync_eng_prov_idx ON grouper_sync (provisioner_name);

CREATE TABLE grouper_sync_job
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    sync_type VARCHAR(50) NOT NULL,
    job_state VARCHAR(50),
    last_sync_index BIGINT,
    last_sync_timestamp TIMESTAMP,
    last_time_work_was_done TIMESTAMP,
    heartbeat TIMESTAMP,
    quartz_job_name VARCHAR(400),
    percent_complete INTEGER,
    last_updated TIMESTAMP NOT NULL,
    error_message VARCHAR(4000),
    error_timestamp TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_st_ty_idx ON grouper_sync_job (grouper_sync_id, sync_type);

CREATE TABLE grouper_sync_group
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    group_id VARCHAR(40) NOT NULL,
    group_name VARCHAR(1024),
    group_id_index BIGINT,
    provisionable VARCHAR(1),
    in_target VARCHAR(1),
    in_target_insert_or_exists VARCHAR(1),
    in_target_start TIMESTAMP,
    in_target_end TIMESTAMP,
    provisionable_start TIMESTAMP,
    provisionable_end TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    last_group_sync TIMESTAMP,
    last_group_metadata_sync TIMESTAMP,
    group_from_id2 VARCHAR(4000),
    group_from_id3 VARCHAR(4000),
    group_to_id2 VARCHAR(4000),
    group_to_id3 VARCHAR(4000),
    metadata_updated TIMESTAMP,
    error_message VARCHAR(4000),
    error_timestamp TIMESTAMP,
    last_time_work_was_done TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_gr_sync_id_idx ON grouper_sync_group (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_gr_group_id_idx ON grouper_sync_group (group_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_gr_sy_gr_idx ON grouper_sync_group (grouper_sync_id, group_id);

CREATE INDEX grouper_sync_gr_f2_idx ON grouper_sync_group (grouper_sync_id, group_from_id2);

CREATE INDEX grouper_sync_gr_f3_idx ON grouper_sync_group (grouper_sync_id, group_from_id3);

CREATE INDEX grouper_sync_gr_t2_idx ON grouper_sync_group (grouper_sync_id, group_to_id2);

CREATE INDEX grouper_sync_gr_t3_idx ON grouper_sync_group (grouper_sync_id, group_to_id3);

CREATE INDEX grouper_sync_gr_er_idx ON grouper_sync_group (grouper_sync_id, error_timestamp);

CREATE TABLE grouper_sync_member
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    member_id VARCHAR(128) NOT NULL,
    source_id VARCHAR(255),
    subject_id VARCHAR(255),
    subject_identifier VARCHAR(255),
    in_target VARCHAR(1),
    in_target_insert_or_exists VARCHAR(1),
    in_target_start TIMESTAMP,
    in_target_end TIMESTAMP,
    provisionable VARCHAR(1),
    provisionable_start TIMESTAMP,
    provisionable_end TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    last_user_sync TIMESTAMP,
    last_user_metadata_sync TIMESTAMP,
    member_from_id2 VARCHAR(4000),
    member_from_id3 VARCHAR(4000),
    member_to_id2 VARCHAR(4000),
    member_to_id3 VARCHAR(4000),
    metadata_updated TIMESTAMP,
    last_time_work_was_done TIMESTAMP,
    error_message VARCHAR(4000),
    error_timestamp TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_us_sync_id_idx ON grouper_sync_member (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_us_mem_id_idx ON grouper_sync_member (member_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_us_sm_idx ON grouper_sync_member (grouper_sync_id, member_id);

CREATE INDEX grouper_sync_us_f2_idx ON grouper_sync_member (grouper_sync_id, member_from_id2);

CREATE INDEX grouper_sync_us_f3_idx ON grouper_sync_member (grouper_sync_id, member_from_id3);

CREATE INDEX grouper_sync_us_t2_idx ON grouper_sync_member (grouper_sync_id, member_to_id2);

CREATE INDEX grouper_sync_us_t3_idx ON grouper_sync_member (grouper_sync_id, member_to_id3);

CREATE INDEX grouper_sync_us_er_idx ON grouper_sync_member (grouper_sync_id, error_timestamp);

CREATE INDEX grouper_sync_us_st_gr_idx ON grouper_sync_member (grouper_sync_id, source_id, subject_id);

CREATE TABLE grouper_sync_membership
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    grouper_sync_group_id VARCHAR(40) NOT NULL,
    grouper_sync_member_id VARCHAR(40) NOT NULL,
    in_target VARCHAR(1),
    in_target_insert_or_exists VARCHAR(1),
    in_target_start TIMESTAMP,
    in_target_end TIMESTAMP,
    last_updated TIMESTAMP NOT NULL,
    membership_id VARCHAR(4000),
    membership_id2 VARCHAR(4000),
    metadata_updated TIMESTAMP,
    error_message VARCHAR(4000),
    error_timestamp TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_group_id, grouper_sync_member_id);

CREATE INDEX grouper_sync_mship_me_idx ON grouper_sync_membership (grouper_sync_group_id, last_updated);

CREATE INDEX grouper_sync_mship_sy_idx ON grouper_sync_membership (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_mship_er_idx ON grouper_sync_membership (grouper_sync_id, error_timestamp);

CREATE INDEX grouper_sync_mship_f1_idx ON grouper_sync_membership (grouper_sync_id, membership_id);

CREATE INDEX grouper_sync_mship_f2_idx ON grouper_sync_membership (grouper_sync_id, membership_id2);

CREATE TABLE grouper_sync_log
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_owner_id VARCHAR(40),
    grouper_sync_id VARCHAR(40),
    status VARCHAR(20),
    sync_timestamp TIMESTAMP,
    description VARCHAR(4000),
    records_processed INTEGER,
    records_changed INTEGER,
    job_took_millis INTEGER,
    server VARCHAR(200),
    last_updated TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_log_sy_idx ON grouper_sync_log (grouper_sync_id, sync_timestamp);

CREATE INDEX grouper_sync_log_ow_idx ON grouper_sync_log (grouper_sync_owner_id, sync_timestamp);

CREATE INDEX attr_asgn_type_idx ON GROUPER_ATTRIBUTE_ASSIGN (ATTRIBUTE_ASSIGN_TYPE);

CREATE INDEX change_log_temp_created_on_idx ON GROUPER_CHANGE_LOG_ENTRY_TEMP (CREATED_ON);

CREATE INDEX composite_type_idx ON GROUPER_COMPOSITES (TYPE);

CREATE INDEX group_enabled_idx ON GROUPER_GROUPS (enabled);

CREATE INDEX group_enabled_time_idx ON GROUPER_GROUPS (enabled_timestamp);

CREATE INDEX group_disabled_time_idx ON GROUPER_GROUPS (disabled_timestamp);

CREATE INDEX member_subjidentifier0_idx ON GROUPER_MEMBERS (SUBJECT_IDENTIFIER0);

CREATE INDEX pit_member_subjidentifier0_idx ON GROUPER_PIT_MEMBERS (SUBJECT_IDENTIFIER0);

ALTER TABLE grouper_password_recently_used
    ADD CONSTRAINT fk_grouper_password_id FOREIGN KEY (grouper_password_id) REFERENCES grouper_password (id);

ALTER TABLE grouper_sync_job
    ADD CONSTRAINT grouper_sync_job_id_fk FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync (id);

ALTER TABLE grouper_sync_group
    ADD CONSTRAINT grouper_sync_gr_id_fk FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync (id);

ALTER TABLE grouper_sync_member
    ADD CONSTRAINT grouper_sync_us_id_fk FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync (id);

ALTER TABLE grouper_sync_membership
    ADD CONSTRAINT grouper_sync_me_gid_fk FOREIGN KEY (grouper_sync_group_id) REFERENCES grouper_sync_group (id);

ALTER TABLE grouper_sync_membership
    ADD CONSTRAINT grouper_sync_me_uid_fk FOREIGN KEY (grouper_sync_member_id) REFERENCES grouper_sync_member (id);

ALTER TABLE grouper_sync_membership
    ADD CONSTRAINT grouper_sync_me_id_fk FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync (id);

ALTER TABLE grouper_sync_log
    ADD CONSTRAINT grouper_sync_log_sy_fk FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync (id);
update grouper_groups set enabled='T' where enabled is null;
commit;

CREATE VIEW grouper_groups_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, TYPE_OF_GROUP, GROUP_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.type_of_group, gg.id as group_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ;

CREATE VIEW grouper_roles_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, ROLE_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.id as role_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and type_of_group = 'role' ;

ALTER TABLE grouper_message ALTER COLUMN from_member_id VARCHAR(40);



update grouper_ddl set db_version = 32, last_updated = '2020/03/31 14:17:44', 
history = '2020/03/31 14:17:44: upgrade Grouper from V30 to V32, 2020/03/31 14:13:49: upgrade Grouper from V29 to V30, 2020/03/31 14:13:49: upgrade Grouper from V28 to V29, 2020/03/31 14:13:49: upgrade Grouper from V27 to V28, 2020/03/31 14:13:49: upgrade Grouper from V26 to V27, 2020/03/31 14:13:49: upgrade Grouper from V25 to V26, 2020/03/31 14:13:49: upgrade Grouper from V24 to V25, 2020/03/31 14:13:49: upgrade Grouper from V23 to V24, 2020/03/31 14:13:49: upgrade Grouper from V22 to V23, 2020/03/31 14:13:49: upgrade Grouper from V21 to V22, 2020/03/31 14:13:49: upgrade Grouper from V20 to V21, 2020/03/31 14:13:49: upgrade Grouper from V19 to V20, 2020/03/31 14:13:48: upgrade Grouper from V18 to V19, 2020/03/31 14:13:48: upgrade Grouper from V17 to V18, 2020/03/31 14:13:48: upgrade Grouper from V16 to V17, 2020/03/31 14:13:48: upgrade Grouper from V15 to V16, 2020/03/31 14:13:48: upgrade Grouper from V14 to V15, 2020/03/31 14:13:48: upgrade Grouper from V13 to V14, 2020/03/31 14:13:48: upgrade Grouper from V12 to V13, 2020/03/31 14:13:48: upgrade Grouper from V11 to V12, 2020/03/31 14:13:48: upgrade Grouper from V10 to V11, 2020/03/31 14:13:48: upgrade Grouper from V9 to V10, 2020/03/31 14:13:48: upgrade Grouper from V8 to V9, 2020/03/31 14:13:48: upgrade Grouper from V7 to V8, 2020/03/31 14:13:48: upgrade Grouper from V6 to V7, 2020/03/31 14:13:48: upgrade Grouper from V5 to V6, 2020/03/31 14:13:48: upgrade Grouper from V4 to V5, 2020/03/31 14:13:48: upgrade Grouper from V3 to V4, 2020/03/31 14:13:48: upgrade Grouper from V2 to V3, 2020/03/31 14:13:48: upgrade Grouper from V1 to V2, 2020/03/31 14:13:48: upgrade Grouper from V0 to V1, ' where object_name = 'Grouper';
commit;


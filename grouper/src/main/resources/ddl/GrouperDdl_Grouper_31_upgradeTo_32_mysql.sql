DROP INDEX grouper_ext_subj_idfr_idx ON grouper_ext_subj;

ALTER TABLE grouper_groups
    ADD COLUMN enabled VARCHAR(1) DEFAULT 'T' NULL AFTER id_index;

ALTER TABLE grouper_groups
    ADD COLUMN enabled_timestamp BIGINT AFTER enabled;

ALTER TABLE grouper_groups
    ADD COLUMN disabled_timestamp BIGINT AFTER enabled_timestamp;

CREATE TABLE grouper_password
(
    id VARCHAR(40) NOT NULL,
    username VARCHAR(255) NOT NULL,
    member_id VARCHAR(40) NULL,
    entity_type VARCHAR(20) NULL,
    is_hashed VARCHAR(1) NOT NULL,
    encryption_type VARCHAR(20) NOT NULL,
    the_salt VARCHAR(255) NULL,
    the_password text NULL,
    application VARCHAR(20) NOT NULL,
    allowed_from_cidrs text NULL,
    recent_source_addresses text NULL,
    failed_source_addresses text NULL,
    last_authenticated BIGINT,
    last_edited BIGINT NOT NULL,
    failed_logins text NULL,
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
    sync_engine VARCHAR(50) NULL,
    provisioner_name VARCHAR(100) NOT NULL,
    group_count INTEGER,
    user_count INTEGER,
    records_count INTEGER,
    incremental_index BIGINT,
    incremental_timestamp DATETIME,
    last_incremental_sync_run DATETIME,
    last_full_sync_run DATETIME,
    last_full_metadata_sync_run DATETIME,
    last_updated DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_eng_idx ON grouper_sync (sync_engine, provisioner_name);

CREATE UNIQUE INDEX grouper_sync_eng_prov_idx ON grouper_sync (provisioner_name);

CREATE TABLE grouper_sync_job
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    sync_type VARCHAR(50) NOT NULL,
    job_state VARCHAR(50) NULL,
    last_sync_index BIGINT,
    last_sync_timestamp DATETIME,
    last_time_work_was_done DATETIME,
    heartbeat DATETIME,
    quartz_job_name VARCHAR(400) NULL,
    percent_complete INTEGER,
    last_updated DATETIME NOT NULL,
    error_message text NULL,
    error_timestamp DATETIME,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_st_ty_idx ON grouper_sync_job (grouper_sync_id, sync_type);

CREATE TABLE grouper_sync_group
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    group_id VARCHAR(40) NOT NULL,
    group_name VARCHAR(1024) NULL,
    group_id_index BIGINT,
    provisionable VARCHAR(1) NULL,
    in_target VARCHAR(1) NULL,
    in_target_insert_or_exists VARCHAR(1) NULL,
    in_target_start DATETIME,
    in_target_end DATETIME,
    provisionable_start DATETIME,
    provisionable_end DATETIME,
    last_updated DATETIME NOT NULL,
    last_group_sync DATETIME,
    last_group_metadata_sync DATETIME,
    group_from_id2 text NULL,
    group_from_id3 text NULL,
    group_to_id2 text NULL,
    group_to_id3 text NULL,
    metadata_updated DATETIME,
    error_message text NULL,
    error_timestamp DATETIME,
    last_time_work_was_done DATETIME,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_gr_sync_id_idx ON grouper_sync_group (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_gr_group_id_idx ON grouper_sync_group (group_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_gr_sy_gr_idx ON grouper_sync_group (grouper_sync_id, group_id);

CREATE INDEX grouper_sync_gr_er_idx ON grouper_sync_group (grouper_sync_id, error_timestamp);

CREATE TABLE grouper_sync_member
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    member_id VARCHAR(128) NOT NULL,
    source_id VARCHAR(255) NULL,
    subject_id VARCHAR(255) NULL,
    subject_identifier VARCHAR(255) NULL,
    in_target VARCHAR(1) NULL,
    in_target_insert_or_exists VARCHAR(1) NULL,
    in_target_start DATETIME,
    in_target_end DATETIME,
    provisionable VARCHAR(1) NULL,
    provisionable_start DATETIME,
    provisionable_end DATETIME,
    last_updated DATETIME NOT NULL,
    last_user_sync DATETIME,
    last_user_metadata_sync DATETIME,
    member_from_id2 text NULL,
    member_from_id3 text NULL,
    member_to_id2 text NULL,
    member_to_id3 text NULL,
    metadata_updated DATETIME,
    last_time_work_was_done DATETIME,
    error_message text NULL,
    error_timestamp DATETIME,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_us_sync_id_idx ON grouper_sync_member (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_us_mem_id_idx ON grouper_sync_member (member_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_us_sm_idx ON grouper_sync_member (grouper_sync_id, member_id);

CREATE INDEX grouper_sync_us_er_idx ON grouper_sync_member (grouper_sync_id, error_timestamp);

CREATE INDEX grouper_sync_us_st_gr_idx ON grouper_sync_member (grouper_sync_id, source_id, subject_id);

CREATE TABLE grouper_sync_membership
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_id VARCHAR(40) NOT NULL,
    grouper_sync_group_id VARCHAR(40) NOT NULL,
    grouper_sync_member_id VARCHAR(40) NOT NULL,
    in_target VARCHAR(1) NULL,
    in_target_insert_or_exists VARCHAR(1) NULL,
    in_target_start DATETIME,
    in_target_end DATETIME,
    last_updated DATETIME NOT NULL,
    membership_id text NULL,
    membership_id2 text NULL,
    metadata_updated DATETIME,
    error_message text NULL,
    error_timestamp DATETIME,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_group_id, grouper_sync_member_id);

CREATE INDEX grouper_sync_mship_me_idx ON grouper_sync_membership (grouper_sync_group_id, last_updated);

CREATE INDEX grouper_sync_mship_sy_idx ON grouper_sync_membership (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_mship_er_idx ON grouper_sync_membership (grouper_sync_id, error_timestamp);

CREATE TABLE grouper_sync_log
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_owner_id VARCHAR(40) NULL,
    grouper_sync_id VARCHAR(40) NULL,
    status VARCHAR(20) NULL,
    sync_timestamp DATETIME,
    description text NULL,
    records_processed INTEGER,
    records_changed INTEGER,
    job_took_millis INTEGER,
    server VARCHAR(200) NULL,
    last_updated DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_log_sy_idx ON grouper_sync_log (grouper_sync_id, sync_timestamp);

CREATE INDEX grouper_sync_log_ow_idx ON grouper_sync_log (grouper_sync_owner_id, sync_timestamp);

CREATE INDEX attr_asgn_type_idx ON grouper_attribute_assign (attribute_assign_type);

CREATE INDEX composite_type_idx ON grouper_composites (type);

CREATE INDEX group_enabled_idx ON grouper_groups (enabled);

CREATE INDEX group_enabled_time_idx ON grouper_groups (enabled_timestamp);

CREATE INDEX group_disabled_time_idx ON grouper_groups (disabled_timestamp);

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

CREATE OR REPLACE VIEW grouper_groups_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, TYPE_OF_GROUP, GROUP_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.type_of_group, gg.id as group_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ;

CREATE OR REPLACE VIEW grouper_roles_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, ROLE_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.id as role_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and type_of_group = 'role' ;

SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE grouper_message MODIFY from_member_id VARCHAR(40);
SET FOREIGN_KEY_CHECKS=1;

CREATE INDEX grouper_sync_gr_f2_idx ON grouper_sync_group (grouper_sync_id, group_from_id2(255));

CREATE INDEX grouper_sync_gr_f3_idx ON grouper_sync_group (grouper_sync_id, group_from_id3(255));

CREATE INDEX grouper_sync_gr_t2_idx ON grouper_sync_group (grouper_sync_id, group_to_id2(255));

CREATE INDEX grouper_sync_gr_t3_idx ON grouper_sync_group (grouper_sync_id, group_to_id3(255));

CREATE INDEX grouper_sync_us_f2_idx ON grouper_sync_member (grouper_sync_id, member_from_id2(255));

CREATE INDEX grouper_sync_us_f3_idx ON grouper_sync_member (grouper_sync_id, member_from_id3(255));

CREATE INDEX grouper_sync_us_t2_idx ON grouper_sync_member (grouper_sync_id, member_to_id2(255));

CREATE INDEX grouper_sync_us_t3_idx ON grouper_sync_member (grouper_sync_id, member_to_id3(255));

CREATE INDEX grouper_sync_mship_f1_idx ON grouper_sync_membership (grouper_sync_id, membership_id(255));

CREATE INDEX grouper_sync_mship_f2_idx ON grouper_sync_membership (grouper_sync_id, membership_id2(255));

CREATE unique INDEX grouper_ext_subj_idfr_idx ON grouper_ext_subj (identifier(255));

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V32, ', history), 1, 3500), db_version = 32 where object_name = 'Grouper';
commit;

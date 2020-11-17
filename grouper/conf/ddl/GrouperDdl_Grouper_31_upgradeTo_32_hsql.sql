
DROP VIEW grouper_groups_v IF EXISTS cascade;

DROP VIEW grouper_roles_v IF EXISTS cascade;

ALTER TABLE GROUPER_GROUPS
    ADD COLUMN disabled_timestamp BIGINT;

ALTER TABLE GROUPER_GROUPS
    ADD COLUMN enabled_timestamp BIGINT BEFORE disabled_timestamp;

ALTER TABLE GROUPER_GROUPS
    ADD COLUMN enabled VARCHAR(1) DEFAULT 'T' BEFORE enabled_timestamp;

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

CREATE INDEX composite_type_idx ON GROUPER_COMPOSITES (TYPE);

CREATE INDEX group_enabled_idx ON GROUPER_GROUPS (enabled);

CREATE INDEX group_enabled_time_idx ON GROUPER_GROUPS (enabled_timestamp);

CREATE INDEX group_disabled_time_idx ON GROUPER_GROUPS (disabled_timestamp);

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

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V32, ' || history) from 1 for 3500), db_version = 32 where object_name = 'Grouper';

commit;

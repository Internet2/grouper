
DROP VIEW IF EXISTS grouper_groups_v cascade;

DROP VIEW IF EXISTS grouper_roles_v cascade;
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

CREATE INDEX attr_asgn_type_idx ON grouper_attribute_assign (attribute_assign_type);

CREATE INDEX composite_type_idx ON grouper_composites (type);

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
ALTER TABLE grouper_groups ADD COLUMN enabled VARCHAR(1);
ALTER TABLE grouper_groups ADD COLUMN enabled_timestamp BIGINT;
ALTER TABLE grouper_groups ADD COLUMN disabled_timestamp BIGINT;
CREATE INDEX group_enabled_idx ON grouper_groups (enabled);
CREATE INDEX group_enabled_time_idx ON grouper_groups (enabled_timestamp);
CREATE INDEX group_disabled_time_idx ON grouper_groups (disabled_timestamp);
update grouper_groups set enabled='T' where enabled is null;
commit;
ALTER TABLE grouper_groups ALTER COLUMN enabled SET NOT NULL;
ALTER TABLE grouper_groups ALTER COLUMN enabled SET DEFAULT 'T';

CREATE OR REPLACE VIEW grouper_groups_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, TYPE_OF_GROUP, GROUP_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.type_of_group, gg.id as group_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ;

COMMENT ON VIEW grouper_groups_v IS 'Contains one record for each group, with friendly names for some attributes and some more information';

COMMENT ON COLUMN grouper_groups_v.EXTENSION IS 'EXTENSION: part of group name not including path information, e.g. theGroup';

COMMENT ON COLUMN grouper_groups_v.NAME IS 'NAME: name of the group, e.g. school:stem1:theGroup';

COMMENT ON COLUMN grouper_groups_v.DISPLAY_EXTENSION IS 'DISPLAY_EXTENSION: name for display of the group, e.g. My school:The stem 1:The group';

COMMENT ON COLUMN grouper_groups_v.DISPLAY_NAME IS 'DISPLAY_NAME: name for display of the group without any path information, e.g. The group';

COMMENT ON COLUMN grouper_groups_v.DESCRIPTION IS 'DESCRIPTION: contains user entered information about the group e.g. why it exists';

COMMENT ON COLUMN grouper_groups_v.PARENT_STEM_NAME IS 'PARENT_STEM_NAME: name of the stem this group is in, e.g. school:stem1';

COMMENT ON COLUMN grouper_groups_v.TYPE_OF_GROUP IS 'TYPE_OF_GROUP: group if it is a group, role if it is a role';

COMMENT ON COLUMN grouper_groups_v.GROUP_ID IS 'GROUP_ID: uuid unique id of the group';

COMMENT ON COLUMN grouper_groups_v.PARENT_STEM_ID IS 'PARENT_STEM_ID: uuid unique id of the stem this group is in';

COMMENT ON COLUMN grouper_groups_v.ENABLED IS 'ENABLED: T or F to indicate if this group is enabled';

COMMENT ON COLUMN grouper_groups_v.ENABLED_TIMESTAMP IS 'ENABLED_TIMESTAMP: when the group will be enabled if the time is in the future';

COMMENT ON COLUMN grouper_groups_v.DISABLED_TIMESTAMP IS 'DISABLED_TIMESTAMP: when the group will be disabled if the time is in the future';

COMMENT ON COLUMN grouper_groups_v.MODIFIER_SOURCE IS 'MODIFIER_SOURCE: source name of the subject who last modified this group, e.g. schoolPersonSource';

COMMENT ON COLUMN grouper_groups_v.MODIFIER_SUBJECT_ID IS 'MODIFIER_SUBJECT_ID: subject id of the subject who last modified this group, e.g. 12345';

COMMENT ON COLUMN grouper_groups_v.CREATOR_SOURCE IS 'CREATOR_SOURCE: source name of the subject who created this group, e.g. schoolPersonSource';

COMMENT ON COLUMN grouper_groups_v.CREATOR_SUBJECT_ID IS 'CREATOR_SUBJECT_ID: subject id of the subject who created this group, e.g. 12345';

COMMENT ON COLUMN grouper_groups_v.IS_COMPOSITE_OWNER IS 'IS_COMPOSITE_OWNER: T if this is a result of a composite operation (union, intersection, complement), or blank if not';

COMMENT ON COLUMN grouper_groups_v.IS_COMPOSITE_FACTOR IS 'IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemeneted';

COMMENT ON COLUMN grouper_groups_v.CREATOR_ID IS 'CREATOR_ID: member id of the subject who created this group, foreign key to grouper_members';

COMMENT ON COLUMN grouper_groups_v.CREATE_TIME IS 'CREATE_TIME: number of millis since 1970 since this group was created';

COMMENT ON COLUMN grouper_groups_v.MODIFIER_ID IS 'MODIFIER_ID: member id of the subject who last modified this group, foreign key to grouper_members';

COMMENT ON COLUMN grouper_groups_v.MODIFY_TIME IS 'MODIFY_TIME: number of millis since 1970 since this group was last changed';

COMMENT ON COLUMN grouper_groups_v.HIBERNATE_VERSION_NUMBER IS 'HIBERNATE_VERSION_NUMBER: increments by 1 for each update';

COMMENT ON COLUMN grouper_groups_v.CONTEXT_ID IS 'Context id links together multiple operations into one high level action';

CREATE OR REPLACE VIEW grouper_roles_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, ROLE_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.id as role_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and type_of_group = 'role' ;

COMMENT ON VIEW grouper_roles_v IS 'Contains one record for each role, with friendly names for some attributes and some more information';

COMMENT ON COLUMN grouper_roles_v.EXTENSION IS 'EXTENSION: part of role name not including path information, e.g. theRole';

COMMENT ON COLUMN grouper_roles_v.NAME IS 'NAME: name of the role, e.g. school:stem1:theRole';

COMMENT ON COLUMN grouper_roles_v.DISPLAY_EXTENSION IS 'DISPLAY_EXTENSION: name for display of the role, e.g. My school:The stem 1:The role';

COMMENT ON COLUMN grouper_roles_v.DISPLAY_NAME IS 'DISPLAY_NAME: name for display of the role without any path information, e.g. The role';

COMMENT ON COLUMN grouper_roles_v.DESCRIPTION IS 'DESCRIPTION: contains user entered information about the group e.g. why it exists';

COMMENT ON COLUMN grouper_roles_v.PARENT_STEM_NAME IS 'PARENT_STEM_NAME: name of the stem this role is in, e.g. school:stem1';

COMMENT ON COLUMN grouper_roles_v.ROLE_ID IS 'ROLE_ID: uuid unique id of the role';

COMMENT ON COLUMN grouper_roles_v.PARENT_STEM_ID IS 'PARENT_STEM_ID: uuid unique id of the stem this role is in';

COMMENT ON COLUMN grouper_roles_v.ENABLED IS 'ENABLED: T or F to indicate if this role is enabled';

COMMENT ON COLUMN grouper_roles_v.ENABLED_TIMESTAMP IS 'ENABLED_TIMESTAMP: when the role will be enabled if the time is in the future';

COMMENT ON COLUMN grouper_roles_v.DISABLED_TIMESTAMP IS 'DISABLED_TIMESTAMP: when the role will be disabled if the time is in the future';

COMMENT ON COLUMN grouper_roles_v.MODIFIER_SOURCE IS 'MODIFIER_SOURCE: source name of the subject who last modified this role, e.g. schoolPersonSource';

COMMENT ON COLUMN grouper_roles_v.MODIFIER_SUBJECT_ID IS 'MODIFIER_SUBJECT_ID: subject id of the subject who last modified this role, e.g. 12345';

COMMENT ON COLUMN grouper_roles_v.CREATOR_SOURCE IS 'CREATOR_SOURCE: source name of the subject who created this role, e.g. schoolPersonSource';

COMMENT ON COLUMN grouper_roles_v.CREATOR_SUBJECT_ID IS 'CREATOR_SUBJECT_ID: subject id of the subject who created this role, e.g. 12345';

COMMENT ON COLUMN grouper_roles_v.IS_COMPOSITE_OWNER IS 'IS_COMPOSITE_OWNER: T if this is a result of a composite operation (union, intersection, complement), or blank if not';

COMMENT ON COLUMN grouper_roles_v.IS_COMPOSITE_FACTOR IS 'IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemeneted';

COMMENT ON COLUMN grouper_roles_v.CREATOR_ID IS 'CREATOR_ID: member id of the subject who created this role, foreign key to grouper_members';

COMMENT ON COLUMN grouper_roles_v.CREATE_TIME IS 'CREATE_TIME: number of millis since 1970 since this role was created';

COMMENT ON COLUMN grouper_roles_v.MODIFIER_ID IS 'MODIFIER_ID: member id of the subject who last modified this role, foreign key to grouper_members';

COMMENT ON COLUMN grouper_roles_v.MODIFY_TIME IS 'MODIFY_TIME: number of millis since 1970 since this role was last changed';

COMMENT ON COLUMN grouper_roles_v.HIBERNATE_VERSION_NUMBER IS 'HIBERNATE_VERSION_NUMBER: increments by 1 for each update';

COMMENT ON COLUMN grouper_roles_v.CONTEXT_ID IS 'Context id links together multiple operations into one high level action';

ALTER TABLE grouper_message ALTER COLUMN from_member_id TYPE VARCHAR(40);
COMMIT;

COMMENT ON TABLE grouper_password IS 'entries for grouper usernames passwords';

COMMENT ON COLUMN grouper_password.id IS 'uuid of this entry (one user could have ui and ws credential)';

COMMENT ON COLUMN grouper_password.username IS 'username or local entity system name';

COMMENT ON COLUMN grouper_password.member_id IS 'this is a reference to the grouper members table';

COMMENT ON COLUMN grouper_password.entity_type IS 'username or localEntity';

COMMENT ON COLUMN grouper_password.is_hashed IS 'T for is hashed, F for is public key';

COMMENT ON COLUMN grouper_password.encryption_type IS 'key type. eg: SHA-256 or RS-256';

COMMENT ON COLUMN grouper_password.the_salt IS 'secure random prepended to hashed pass';

COMMENT ON COLUMN grouper_password.the_password IS 'encrypted public key or encrypted hashed salted password';

COMMENT ON COLUMN grouper_password.application IS 'ws (includes scim) or ui';

COMMENT ON COLUMN grouper_password.allowed_from_cidrs IS 'network cidrs where credential is allowed from';

COMMENT ON COLUMN grouper_password.recent_source_addresses IS 'json with timestamps';

COMMENT ON COLUMN grouper_password.failed_source_addresses IS 'if restricted by cidr, this was failed IPs (json with timestamp)';

COMMENT ON COLUMN grouper_password.last_authenticated IS 'when last authenticated';

COMMENT ON COLUMN grouper_password.last_edited IS 'when last edited';

COMMENT ON COLUMN grouper_password.failed_logins IS 'json of failed attempts';

COMMENT ON COLUMN grouper_password.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON TABLE grouper_password_recently_used IS 'recently used jwt tokens so they arent re-used';

COMMENT ON COLUMN grouper_password_recently_used.id IS 'uuid of this entry';

COMMENT ON COLUMN grouper_password_recently_used.grouper_password_id IS 'password uuid for this jwt';

COMMENT ON COLUMN grouper_password_recently_used.jwt_jti IS 'unique identifier of the login';

COMMENT ON COLUMN grouper_password_recently_used.jwt_iat IS 'timestamp of this entry';

COMMENT ON TABLE grouper_sync IS 'One record for every provisioner (not different records for full and real time)';

COMMENT ON COLUMN grouper_sync.id IS 'uuid of this record in this table';

COMMENT ON COLUMN grouper_sync.sync_engine IS 'e.g. for syncing sql, it sqlTableSync';

COMMENT ON COLUMN grouper_sync.provisioner_name IS 'name of provisioner must be unique.  this is the config key generally';

COMMENT ON COLUMN grouper_sync.group_count IS 'if group this is the number of groups';

COMMENT ON COLUMN grouper_sync.user_count IS 'if has users, this is the number of users';

COMMENT ON COLUMN grouper_sync.records_count IS 'number of records including users, groups, etc';

COMMENT ON COLUMN grouper_sync.incremental_index IS 'int of last record processed';

COMMENT ON COLUMN grouper_sync.incremental_timestamp IS 'timestamp of last record processed';

COMMENT ON COLUMN grouper_sync.last_incremental_sync_run IS 'when incremental sync ran';

COMMENT ON COLUMN grouper_sync.last_full_sync_run IS 'when last full sync ran';

COMMENT ON COLUMN grouper_sync.last_full_metadata_sync_run IS 'when last full metadata sync ran.  this needs to run when groups get renamed';

COMMENT ON COLUMN grouper_sync.last_updated IS 'when this record was last updated';

COMMENT ON TABLE grouper_sync_job IS 'Status of all jobs for the sync.  one record for full, one for incremental, etc';

COMMENT ON COLUMN grouper_sync_job.id IS 'uuid of this record in this table';

COMMENT ON COLUMN grouper_sync_job.grouper_sync_id IS 'uuid of the job in grouper_sync table';

COMMENT ON COLUMN grouper_sync_job.sync_type IS 'type of sync, e.g. for sql sync this is the job subtype';

COMMENT ON COLUMN grouper_sync_job.job_state IS 'running, pending (if waiting for another job to finish), notRunning';

COMMENT ON COLUMN grouper_sync_job.last_sync_index IS 'either an int of last record checked, or an int of millis since 1970 of last record processed';

COMMENT ON COLUMN grouper_sync_job.last_sync_timestamp IS 'when last record processed if timestamp and not integer';

COMMENT ON COLUMN grouper_sync_job.heartbeat IS 'when a job is running this must be updated every 60 seconds in a thread or the job will be deemed not running by other jobs';

COMMENT ON COLUMN grouper_sync_job.last_time_work_was_done IS 'last time a record was processed';

COMMENT ON COLUMN grouper_sync_job.quartz_job_name IS 'name of quartz job if applicable';

COMMENT ON COLUMN grouper_sync_job.percent_complete IS '0-100 percent complete of this job';

COMMENT ON COLUMN grouper_sync_job.last_updated IS 'when this record was last updated';

COMMENT ON COLUMN grouper_sync_job.error_message IS 'if there was an error when syncing this group, this is the message';

COMMENT ON COLUMN grouper_sync_job.error_timestamp IS 'timestamp of error if there was an error when syncing this group';

COMMENT ON COLUMN grouper_sync_group.id IS 'uuid of this record';

COMMENT ON COLUMN grouper_sync_group.grouper_sync_id IS 'foreign key back to the sync table';

COMMENT ON COLUMN grouper_sync_group.group_id IS 'if this is groups, then this is the uuid of the group, though not a real foreign key';

COMMENT ON COLUMN grouper_sync_group.group_name IS 'if this is groups, then this is the system name of the group';

COMMENT ON COLUMN grouper_sync_group.group_id_index IS 'if this is groups, then this is the id index of the group';

COMMENT ON COLUMN grouper_sync_group.provisionable IS 'T if provisionable and F is not';

COMMENT ON COLUMN grouper_sync_group.in_target IS 'T if exists in target/destination and F is not.  blank if not sure';

COMMENT ON COLUMN grouper_sync_group.in_target_insert_or_exists IS 'T if inserted on the in_target_start date, or F if it existed then and not sure when inserted';

COMMENT ON COLUMN grouper_sync_group.in_target_start IS 'when this was put in target';

COMMENT ON COLUMN grouper_sync_group.in_target_end IS 'when this was taken out of target';

COMMENT ON COLUMN grouper_sync_group.provisionable_start IS 'when this group started to be provisionable';

COMMENT ON COLUMN grouper_sync_group.provisionable_end IS 'when this group ended being provisionable';

COMMENT ON COLUMN grouper_sync_group.last_updated IS 'when this record was last updated';

COMMENT ON COLUMN grouper_sync_group.last_group_sync IS 'when this group was last synced';

COMMENT ON COLUMN grouper_sync_group.last_group_metadata_sync IS 'when this groups name and description and metadata was synced';

COMMENT ON COLUMN grouper_sync_group.group_from_id2 IS 'other metadata on groups';

COMMENT ON COLUMN grouper_sync_group.group_from_id3 IS 'other metadata on groups';

COMMENT ON COLUMN grouper_sync_group.group_to_id2 IS 'other metadata on groups';

COMMENT ON COLUMN grouper_sync_group.group_to_id3 IS 'other metadata on groups';

COMMENT ON COLUMN grouper_sync_group.last_time_work_was_done IS 'last time a record was processed';

COMMENT ON COLUMN grouper_sync_group.metadata_updated IS 'when the metadata was last updated (if it times out)';

COMMENT ON COLUMN grouper_sync_group.error_message IS 'if there was an error when syncing this object, this is the message';

COMMENT ON COLUMN grouper_sync_group.error_timestamp IS 'timestamp of error if there was an error when syncing this object';

COMMENT ON TABLE grouper_sync_member IS 'user metadata for sync';

COMMENT ON COLUMN grouper_sync_member.id IS 'uuid of this record in this table';

COMMENT ON COLUMN grouper_sync_member.grouper_sync_id IS 'foreign key to grouper_sync table';

COMMENT ON COLUMN grouper_sync_member.member_id IS 'foreign key to the members table, though not a real foreign key';

COMMENT ON COLUMN grouper_sync_member.source_id IS 'subject source id';

COMMENT ON COLUMN grouper_sync_member.subject_id IS 'subject id';

COMMENT ON COLUMN grouper_sync_member.subject_identifier IS 'netId or eppn or whatever';

COMMENT ON COLUMN grouper_sync_member.in_target IS 'T if exists in target/destination and F is not.  blank if not sure';

COMMENT ON COLUMN grouper_sync_member.in_target_insert_or_exists IS 'T if inserted on the in_target_start date, or F if it existed then and not sure when inserted';

COMMENT ON COLUMN grouper_sync_member.in_target_start IS 'when the user was put in the target';

COMMENT ON COLUMN grouper_sync_member.in_target_end IS 'when the user was taken out of the target';

COMMENT ON COLUMN grouper_sync_member.provisionable IS 'T if provisionable and F is not';

COMMENT ON COLUMN grouper_sync_member.provisionable_start IS 'when this user started to be provisionable';

COMMENT ON COLUMN grouper_sync_member.provisionable_end IS 'when this user ended being provisionable';

COMMENT ON COLUMN grouper_sync_member.last_updated IS 'when this record was last updated';

COMMENT ON COLUMN grouper_sync_member.last_user_sync IS 'when this user was last synced, includes metadata and memberships';

COMMENT ON COLUMN grouper_sync_member.last_user_metadata_sync IS 'when this users name and description and metadata was synced';

COMMENT ON COLUMN grouper_sync_member.member_from_id2 IS 'for users this is the user idIndex';

COMMENT ON COLUMN grouper_sync_member.member_from_id3 IS 'other metadata on users';

COMMENT ON COLUMN grouper_sync_member.member_to_id2 IS 'other metadat on users';

COMMENT ON COLUMN grouper_sync_member.member_to_id3 IS 'other metadata on users';

COMMENT ON COLUMN grouper_sync_member.last_time_work_was_done IS 'last time a record was processed';

COMMENT ON COLUMN grouper_sync_member.metadata_updated IS 'when the metadata was last updated (if it times out)';

COMMENT ON COLUMN grouper_sync_member.error_message IS 'if there was an error when syncing this object, this is the message';

COMMENT ON COLUMN grouper_sync_member.error_timestamp IS 'timestamp of error if there was an error when syncing this object';

COMMENT ON TABLE grouper_sync_membership IS 'record of a sync_group and a sync_member represents a sync^ed membership';

COMMENT ON COLUMN grouper_sync_membership.id IS 'uuid of this record';

COMMENT ON COLUMN grouper_sync_membership.grouper_sync_id IS 'foreign key back to sync table';

COMMENT ON COLUMN grouper_sync_membership.grouper_sync_group_id IS 'foreign key back to sync group table';

COMMENT ON COLUMN grouper_sync_membership.grouper_sync_member_id IS 'foreign key back to sync member table';

COMMENT ON COLUMN grouper_sync_membership.in_target IS 'T if exists in target/destination and F is not.  blank if not sure';

COMMENT ON COLUMN grouper_sync_membership.in_target_insert_or_exists IS 'T if inserted on the in_target_start date, or F if it existed then and not sure when inserted';

COMMENT ON COLUMN grouper_sync_membership.in_target_start IS 'when this was put in target';

COMMENT ON COLUMN grouper_sync_membership.in_target_end IS 'when this was taken out of target';

COMMENT ON COLUMN grouper_sync_membership.last_updated IS 'when this record was last updated';

COMMENT ON COLUMN grouper_sync_membership.membership_id IS 'other metadata on membership';

COMMENT ON COLUMN grouper_sync_membership.membership_id2 IS 'other metadata on membership';

COMMENT ON COLUMN grouper_sync_membership.metadata_updated IS 'when the metadata was last updated (if it times out)';

COMMENT ON COLUMN grouper_sync_membership.error_message IS 'if there was an error when syncing this object, this is the message';

COMMENT ON COLUMN grouper_sync_membership.error_timestamp IS 'timestamp of error if there was an error when syncing this object';

COMMENT ON TABLE grouper_sync_log IS 'last log for this sync that affected this group or member etc';

COMMENT ON COLUMN grouper_sync_log.id IS 'uuid of this record in this table';

COMMENT ON COLUMN grouper_sync_log.grouper_sync_owner_id IS 'either the grouper_sync_membership_id or the grouper_sync_member_id or the grouper_sync_group_id or grouper_sync_job_id (if log for job wide)';

COMMENT ON COLUMN grouper_sync_log.grouper_sync_id IS 'foreign key to grouper_sync table';

COMMENT ON COLUMN grouper_sync_log.status IS 'SUCCESS, ERROR, WARNING, CONFIG_ERROR';

COMMENT ON COLUMN grouper_sync_log.sync_timestamp IS 'when the last sync started';

COMMENT ON COLUMN grouper_sync_log.description IS 'description of last sync';

COMMENT ON COLUMN grouper_sync_log.records_processed IS 'how many records were processed the last time this sync ran';

COMMENT ON COLUMN grouper_sync_log.records_changed IS 'how many records were changed the last time this sync ran';

COMMENT ON COLUMN grouper_sync_log.job_took_millis IS 'how many millis it took to run this job';

COMMENT ON COLUMN grouper_sync_log.server IS 'which server this occurred on';

COMMENT ON COLUMN grouper_sync_log.last_updated IS 'when this record was last updated';

COMMENT ON TABLE grouper_config IS 'database configuration for config files which allowe database overrides';

COMMENT ON COLUMN grouper_config.id IS 'uuid of record is unique for all records in table and primary key';

COMMENT ON COLUMN grouper_config.config_file_name IS 'Config file name of the config this record relates to, e.g. grouper.config.properties';

COMMENT ON COLUMN grouper_config.config_key IS 'key of the config, not including elConfig';

COMMENT ON COLUMN grouper_config.config_value IS 'Value of the config';

COMMENT ON COLUMN grouper_config.config_comment IS 'documentation of the config value';

COMMENT ON COLUMN grouper_config.config_file_hierarchy IS 'config file hierarchy, e.g. base, institution, or env';

COMMENT ON COLUMN grouper_config.config_encrypted IS 'if the value is encrypted';

COMMENT ON COLUMN grouper_config.config_sequence IS 'if there is more data than fits in the column this is the 0 indexed order';

COMMENT ON COLUMN grouper_config.config_version_index IS 'for built in configs, this is the index that will identify if the database configs should be replaced from the java code';

COMMENT ON COLUMN grouper_config.last_updated IS 'when this record was inserted or last updated';

COMMENT ON COLUMN grouper_config.hibernate_version_number IS 'hibernate version for optimistic locking';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V32, ' || history) from 1 for 3500), db_version = 32 where object_name = 'Grouper';
commit;


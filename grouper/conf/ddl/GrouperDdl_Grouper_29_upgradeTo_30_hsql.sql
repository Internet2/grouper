
CREATE VIEW grouper_perms_all_v IF EXISTS cascade;

CREATE VIEW grouper_pit_perms_all_v IF EXISTS cascade;

DROP VIEW grouper_aval_asn_efmship_v IF EXISTS cascade;

ALTER TABLE GROUPER_MEMBERS
    ADD COLUMN subject_identifier0 VARCHAR(255);

ALTER TABLE GROUPER_PIT_MEMBERS
    ADD COLUMN subject_identifier0 VARCHAR(255);

CREATE TABLE grouper_message
(
    id VARCHAR(40) NOT NULL,
    sent_time_micros BIGINT NOT NULL,
    get_attempt_time_millis BIGINT NOT NULL,
    get_attempt_count BIGINT NOT NULL,
    state VARCHAR(20) NOT NULL,
    get_time_millis BIGINT,
    from_member_id VARCHAR(100) NOT NULL,
    queue_name VARCHAR(100) NOT NULL,
    message_body VARCHAR(4000),
    hibernate_version_number BIGINT NOT NULL,
    attempt_time_expires_millis BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX grpmessage_sent_time_idx ON grouper_message (sent_time_micros);

CREATE INDEX grpmessage_state_idx ON grouper_message (state);

CREATE INDEX grpmessage_queue_name_idx ON grouper_message (queue_name);

CREATE INDEX grpmessage_from_mem_id_idx ON grouper_message (from_member_id);

CREATE INDEX grpmessage_attempt_exp_idx ON grouper_message (attempt_time_expires_millis);

CREATE UNIQUE INDEX grpmessage_query_idx ON grouper_message (queue_name, state, sent_time_micros, id);

CREATE TABLE grouper_QZ_JOB_DETAILS
(
    sched_name VARCHAR(120) NOT NULL,
    job_name VARCHAR(200) NOT NULL,
    job_group VARCHAR(200) NOT NULL,
    description VARCHAR(250),
    job_class_name VARCHAR(250) NOT NULL,
    is_durable BOOLEAN NOT NULL,
    is_nonconcurrent BOOLEAN NOT NULL,
    is_update_data BOOLEAN NOT NULL,
    requests_recovery BOOLEAN NOT NULL,
    job_data LONGVARBINARY,
    PRIMARY KEY (sched_name, job_name, job_group)
);

CREATE INDEX idx_qrtz_j_req_recovery ON grouper_QZ_JOB_DETAILS (sched_name, requests_recovery);

CREATE INDEX idx_qrtz_j_grp ON grouper_QZ_JOB_DETAILS (sched_name, job_group);

CREATE TABLE grouper_QZ_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    job_name VARCHAR(200) NOT NULL,
    job_group VARCHAR(200) NOT NULL,
    description VARCHAR(250),
    next_fire_time BIGINT,
    prev_fire_time BIGINT,
    priority BIGINT,
    trigger_state VARCHAR(16) NOT NULL,
    trigger_type VARCHAR(8) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    calendar_name VARCHAR(200),
    misfire_instr BIGINT,
    job_data LONGVARBINARY,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE INDEX idx_qrtz_t_j ON grouper_QZ_TRIGGERS (sched_name, job_name, job_group);

CREATE INDEX idx_qrtz_t_jg ON grouper_QZ_TRIGGERS (sched_name, job_group);

CREATE INDEX idx_qrtz_t_c ON grouper_QZ_TRIGGERS (sched_name, calendar_name);

CREATE INDEX idx_qrtz_t_g ON grouper_QZ_TRIGGERS (sched_name, trigger_group);

CREATE INDEX idx_qrtz_t_state ON grouper_QZ_TRIGGERS (sched_name, trigger_state);

CREATE INDEX idx_qrtz_t_n_state ON grouper_QZ_TRIGGERS (sched_name, trigger_name, trigger_group, trigger_state);

CREATE INDEX idx_qrtz_t_n_g_state ON grouper_QZ_TRIGGERS (sched_name, trigger_group, trigger_state);

CREATE INDEX idx_qrtz_t_next_fire_time ON grouper_QZ_TRIGGERS (sched_name, next_fire_time);

CREATE INDEX idx_qrtz_t_nft_st ON grouper_QZ_TRIGGERS (sched_name, trigger_state, next_fire_time);

CREATE INDEX idx_qrtz_t_nft_misfire ON grouper_QZ_TRIGGERS (sched_name, misfire_instr, next_fire_time);

CREATE INDEX idx_qrtz_t_nft_st_misfire ON grouper_QZ_TRIGGERS (sched_name, misfire_instr, next_fire_time, trigger_state);

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON grouper_QZ_TRIGGERS (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

CREATE TABLE grouper_QZ_SIMPLE_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    repeat_count BIGINT NOT NULL,
    repeat_interval BIGINT NOT NULL,
    times_triggered BIGINT NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_CRON_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    cron_expression VARCHAR(120) NOT NULL,
    time_zone_id VARCHAR(80),
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_SIMPROP_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    str_prop_1 VARCHAR(512),
    str_prop_2 VARCHAR(512),
    str_prop_3 VARCHAR(512),
    int_prop_1 BIGINT,
    int_prop_2 BIGINT,
    long_prop_1 BIGINT,
    long_prop_2 BIGINT,
    dec_prop_1 DOUBLE,
    dec_prop_2 DOUBLE,
    bool_prop_1 BOOLEAN,
    bool_prop_2 BOOLEAN,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_BLOB_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    blob_data LONGVARBINARY,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_CALENDARS
(
    sched_name VARCHAR(120) NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    calendar LONGVARBINARY NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

CREATE TABLE grouper_QZ_PAUSED_TRIGGER_GRPS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

CREATE TABLE grouper_QZ_FIRED_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    entry_id VARCHAR(95) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    instance_name VARCHAR(200) NOT NULL,
    fired_time BIGINT NOT NULL,
    sched_time BIGINT NOT NULL,
    priority BIGINT NOT NULL,
    state VARCHAR(16) NOT NULL,
    job_name VARCHAR(200),
    job_group VARCHAR(200),
    is_nonconcurrent BOOLEAN,
    requests_recovery BOOLEAN,
    PRIMARY KEY (sched_name, entry_id)
);

CREATE INDEX idx_qrtz_ft_trig_inst_name ON grouper_QZ_FIRED_TRIGGERS (sched_name, instance_name);

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON grouper_QZ_FIRED_TRIGGERS (sched_name, instance_name, requests_recovery);

CREATE INDEX idx_qrtz_ft_j_g ON grouper_QZ_FIRED_TRIGGERS (sched_name, job_name, job_group);

CREATE INDEX idx_qrtz_ft_jg ON grouper_QZ_FIRED_TRIGGERS (sched_name, job_group);

CREATE INDEX idx_qrtz_ft_t_g ON grouper_QZ_FIRED_TRIGGERS (sched_name, trigger_name, trigger_group);

CREATE INDEX idx_qrtz_ft_tg ON grouper_QZ_FIRED_TRIGGERS (sched_name, trigger_group);

CREATE TABLE grouper_QZ_SCHEDULER_STATE
(
    sched_name VARCHAR(120) NOT NULL,
    instance_name VARCHAR(200) NOT NULL,
    last_checkin_time BIGINT NOT NULL,
    checkin_interval BIGINT NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

CREATE TABLE grouper_QZ_LOCKS
(
    sched_name VARCHAR(120) NOT NULL,
    lock_name VARCHAR(40) NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);
update grouper_fields set read_privilege='stemAdmin' where read_privilege='stem';
update grouper_fields set write_privilege='stemAdmin' where write_privilege='stem';
update grouper_fields set name='stemAdmins' where name='stemmers';
commit;
update grouper_pit_fields set name='stemAdmins' where name='stemmers';
commit;

ALTER TABLE grouper_message
    ADD CONSTRAINT fk_message_from_member_id FOREIGN KEY (from_member_id) REFERENCES GROUPER_MEMBERS (ID);

ALTER TABLE grouper_QZ_TRIGGERS
    ADD CONSTRAINT qrtz_trigger_to_jobs_fk FOREIGN KEY (sched_name, job_name, job_group) REFERENCES grouper_QZ_JOB_DETAILS (sched_name, job_name, job_group);

ALTER TABLE grouper_QZ_SIMPLE_TRIGGERS
    ADD CONSTRAINT qrtz_simple_trig_to_trig_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES grouper_QZ_TRIGGERS (sched_name, trigger_name, trigger_group);

ALTER TABLE grouper_QZ_CRON_TRIGGERS
    ADD CONSTRAINT qrtz_cron_trig_to_trig_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES grouper_QZ_TRIGGERS (sched_name, trigger_name, trigger_group);

ALTER TABLE grouper_QZ_SIMPROP_TRIGGERS
    ADD CONSTRAINT qrtz_simprop_trig_to_trig_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES grouper_QZ_TRIGGERS (sched_name, trigger_name, trigger_group);

ALTER TABLE grouper_QZ_BLOB_TRIGGERS
    ADD CONSTRAINT qrtz_blob_trig_to_trig_fk FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES grouper_QZ_TRIGGERS (sched_name, trigger_name, trigger_group);
    
CREATE VIEW grouper_aval_asn_efmship_v (group_name, subject_source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, list_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, member_id, action_id, attribute_assign_value_id) AS select distinct gg.name as group_name, gm.subject_source as subject_source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gf.name as list_name, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gm.id as member_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_group_id = gmav.owner_group_id and gaa.owner_member_id = gmav.member_id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gmav.immediate_mship_enabled = 'T' and gmav.owner_group_id = gg.id and gmav.field_id = gf.id and gf.type = 'list' and gmav.member_id = gm.id and gaa.owner_member_id is not null and gaa.owner_group_id is not null and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_perms_all_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  attribute_def_name_disp_name,  role_display_name,  attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed from grouper_perms_role_v  union  select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  attribute_def_name_disp_name,  role_display_name,  attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed from grouper_perms_role_subject_v  ;

CREATE VIEW grouper_pit_perms_all_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id from grouper_pit_perms_role_v  union  select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id from grouper_pit_perms_role_subj_v  ;

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V30, ' || history) from 1 for 3500), db_version = 30 where object_name = 'Grouper';

commit;

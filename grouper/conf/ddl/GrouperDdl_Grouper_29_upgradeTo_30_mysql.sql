
DROP VIEW grouper_aval_asn_efmship_v;



ALTER TABLE grouper_members
    ADD COLUMN subject_identifier0 VARCHAR(255) NULL AFTER context_id;

ALTER TABLE grouper_pit_members
    ADD COLUMN subject_identifier0 VARCHAR(255) NULL AFTER hibernate_version_number;

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
    message_body text NULL,
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
    description VARCHAR(250) NULL,
    job_class_name VARCHAR(250) NOT NULL,
    is_durable TINYINT(1) NOT NULL,
    is_nonconcurrent TINYINT(1) NOT NULL,
    is_update_data TINYINT(1) NOT NULL,
    requests_recovery TINYINT(1) NOT NULL,
    job_data LONGBLOB NULL,
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
    description VARCHAR(250) NULL,
    next_fire_time BIGINT,
    prev_fire_time BIGINT,
    priority BIGINT,
    trigger_state VARCHAR(16) NOT NULL,
    trigger_type VARCHAR(8) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    calendar_name VARCHAR(200) NULL,
    misfire_instr BIGINT,
    job_data LONGBLOB NULL,
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
    time_zone_id VARCHAR(80) NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_SIMPROP_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    str_prop_1 VARCHAR(512) NULL,
    str_prop_2 VARCHAR(512) NULL,
    str_prop_3 VARCHAR(512) NULL,
    int_prop_1 BIGINT,
    int_prop_2 BIGINT,
    long_prop_1 BIGINT,
    long_prop_2 BIGINT,
    dec_prop_1 DOUBLE,
    dec_prop_2 DOUBLE,
    bool_prop_1 TINYINT(1),
    bool_prop_2 TINYINT(1),
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_BLOB_TRIGGERS
(
    sched_name VARCHAR(120) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    blob_data LONGBLOB NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_CALENDARS
(
    sched_name VARCHAR(120) NOT NULL,
    calendar_name VARCHAR(200) NOT NULL,
    calendar LONGBLOB NOT NULL,
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
    job_name VARCHAR(200) NULL,
    job_group VARCHAR(200) NULL,
    is_nonconcurrent TINYINT(1),
    requests_recovery TINYINT(1),
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
    ADD CONSTRAINT fk_message_from_member_id FOREIGN KEY (from_member_id) REFERENCES grouper_members (id);

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

update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V30, ', history), 1, 3500), db_version = 30 where object_name = 'Grouper';
commit;
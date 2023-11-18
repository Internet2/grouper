DROP VIEW grouper_aval_asn_efmship_v;

ALTER TABLE GROUPER_MEMBERS
    ADD subject_identifier0 VARCHAR2(255);

ALTER TABLE GROUPER_PIT_MEMBERS
    ADD subject_identifier0 VARCHAR2(255);

CREATE TABLE grouper_message
(
    id VARCHAR2(40) NOT NULL,
    sent_time_micros NUMBER(38) NOT NULL,
    get_attempt_time_millis NUMBER(38) NOT NULL,
    get_attempt_count NUMBER(38) NOT NULL,
    state VARCHAR2(20) NOT NULL,
    get_time_millis NUMBER(38),
    from_member_id VARCHAR2(100) NOT NULL,
    queue_name VARCHAR2(100) NOT NULL,
    message_body VARCHAR2(4000),
    hibernate_version_number NUMBER(38) NOT NULL,
    attempt_time_expires_millis NUMBER(38),
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
    sched_name VARCHAR2(120) NOT NULL,
    job_name VARCHAR2(200) NOT NULL,
    job_group VARCHAR2(200) NOT NULL,
    description VARCHAR2(250),
    job_class_name VARCHAR2(250) NOT NULL,
    is_durable NUMBER(1) NOT NULL,
    is_nonconcurrent NUMBER(1) NOT NULL,
    is_update_data NUMBER(1) NOT NULL,
    requests_recovery NUMBER(1) NOT NULL,
    job_data BLOB,
    PRIMARY KEY (sched_name, job_name, job_group)
);

CREATE INDEX idx_qrtz_j_req_recovery ON grouper_QZ_JOB_DETAILS (sched_name, requests_recovery);

CREATE INDEX idx_qrtz_j_grp ON grouper_QZ_JOB_DETAILS (sched_name, job_group);

CREATE TABLE grouper_QZ_TRIGGERS
(
    sched_name VARCHAR2(120) NOT NULL,
    trigger_name VARCHAR2(200) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    job_name VARCHAR2(200) NOT NULL,
    job_group VARCHAR2(200) NOT NULL,
    description VARCHAR2(250),
    next_fire_time NUMBER(38),
    prev_fire_time NUMBER(38),
    priority NUMBER(38),
    trigger_state VARCHAR2(16) NOT NULL,
    trigger_type VARCHAR2(8) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    calendar_name VARCHAR2(200),
    misfire_instr NUMBER(38),
    job_data BLOB,
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
    sched_name VARCHAR2(120) NOT NULL,
    trigger_name VARCHAR2(200) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    repeat_count NUMBER(38) NOT NULL,
    repeat_interval NUMBER(38) NOT NULL,
    times_triggered NUMBER(38) NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_CRON_TRIGGERS
(
    sched_name VARCHAR2(120) NOT NULL,
    trigger_name VARCHAR2(200) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    cron_expression VARCHAR2(120) NOT NULL,
    time_zone_id VARCHAR2(80),
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_SIMPROP_TRIGGERS
(
    sched_name VARCHAR2(120) NOT NULL,
    trigger_name VARCHAR2(200) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    str_prop_1 VARCHAR2(512),
    str_prop_2 VARCHAR2(512),
    str_prop_3 VARCHAR2(512),
    int_prop_1 NUMBER(38),
    int_prop_2 NUMBER(38),
    long_prop_1 NUMBER(38),
    long_prop_2 NUMBER(38),
    dec_prop_1 DOUBLE PRECISION,
    dec_prop_2 DOUBLE PRECISION,
    bool_prop_1 NUMBER(1),
    bool_prop_2 NUMBER(1),
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_BLOB_TRIGGERS
(
    sched_name VARCHAR2(120) NOT NULL,
    trigger_name VARCHAR2(200) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    blob_data BLOB,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE TABLE grouper_QZ_CALENDARS
(
    sched_name VARCHAR2(120) NOT NULL,
    calendar_name VARCHAR2(200) NOT NULL,
    calendar BLOB NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);

CREATE TABLE grouper_QZ_PAUSED_TRIGGER_GRPS
(
    sched_name VARCHAR2(120) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

CREATE TABLE grouper_QZ_FIRED_TRIGGERS
(
    sched_name VARCHAR2(120) NOT NULL,
    entry_id VARCHAR2(95) NOT NULL,
    trigger_name VARCHAR2(200) NOT NULL,
    trigger_group VARCHAR2(200) NOT NULL,
    instance_name VARCHAR2(200) NOT NULL,
    fired_time NUMBER(38) NOT NULL,
    sched_time NUMBER(38) NOT NULL,
    priority NUMBER(38) NOT NULL,
    state VARCHAR2(16) NOT NULL,
    job_name VARCHAR2(200),
    job_group VARCHAR2(200),
    is_nonconcurrent NUMBER(1),
    requests_recovery NUMBER(1),
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
    sched_name VARCHAR2(120) NOT NULL,
    instance_name VARCHAR2(200) NOT NULL,
    last_checkin_time NUMBER(38) NOT NULL,
    checkin_interval NUMBER(38) NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

CREATE TABLE grouper_QZ_LOCKS
(
    sched_name VARCHAR2(120) NOT NULL,
    lock_name VARCHAR2(40) NOT NULL,
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

COMMENT ON TABLE grouper_message IS 'If using the default internal messaging with Grouper, this is the table that holds the messages and state of messages';

COMMENT ON COLUMN grouper_message.from_member_id IS 'member id of user who sent the message';

COMMENT ON COLUMN grouper_message.get_attempt_count IS 'how many times this message has been attempted to be retrieved';

COMMENT ON COLUMN grouper_message.get_attempt_time_millis IS 'milliseconds since 1970 that the message was attempted to be received';

COMMENT ON COLUMN grouper_message.get_time_millis IS 'millis since 1970 that this message was successfully received';

COMMENT ON COLUMN grouper_message.attempt_time_expires_millis IS 'millis since 1970 that this message attempt expires if not sent successfully';

COMMENT ON COLUMN grouper_message.hibernate_version_number IS 'hibernate version, optimistic locking so multiple processes dont update the same record at the same time';

COMMENT ON COLUMN grouper_message.id IS 'db uuid for this row';

COMMENT ON COLUMN grouper_message.message_body IS 'message body';

COMMENT ON COLUMN grouper_message.queue_name IS 'queue name for the message';

COMMENT ON COLUMN grouper_message.sent_time_micros IS 'microseconds since 1970 this message was sent (note this is probably unique, but not necessarily)';

COMMENT ON COLUMN grouper_message.state IS 'state of this message: IN_QUEUE, GET_ATTEMPTED, PROCESSED';





COMMENT ON COLUMN grouper_members.subject_identifier0 IS 'subject identifier of the subject';

COMMENT ON COLUMN grouper_pit_members.subject_identifier0 IS 'subject identifier of the subject';




CREATE VIEW grouper_aval_asn_efmship_v (group_name, subject_source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, list_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, member_id, action_id, attribute_assign_value_id) AS select distinct gg.name as group_name, gm.subject_source as subject_source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gf.name as list_name, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gm.id as member_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_group_id = gmav.owner_group_id and gaa.owner_member_id = gmav.member_id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gmav.immediate_mship_enabled = 'T' and gmav.owner_group_id = gg.id and gmav.field_id = gf.id and gf.type = 'list' and gmav.member_id = gm.id and gaa.owner_member_id is not null and gaa.owner_group_id is not null and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_aval_asn_efmship_v IS 'grouper_aval_asn_efmship_v: attribute assigned to an effective membership and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.group_name IS 'group_name: name of group assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.subject_source_id IS 'subject_source_id: source id of the subject being assigned';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.subject_id IS 'subject_id: subject id of the subject being assigned';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.group_display_name IS 'group_display_name: display name of the group assigned an attribute';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.list_name IS 'list_name: name of the membership list for this effective membership';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.group_id IS 'group_id: group id of the group assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.member_id IS 'member_id: id of the member assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.action_id IS 'action_id: attribute assign action id';

COMMENT ON COLUMN grouper_aval_asn_efmship_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V30, ' || history), 1, 3500), db_version = 30 where object_name = 'Grouper';
commit;
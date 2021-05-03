CREATE TABLE grouper_ddl
(
    id VARCHAR(40) NOT NULL,
    object_name VARCHAR(128) NULL,
    db_version INTEGER,
    last_updated VARCHAR(50) NULL,
    history text NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_ddl_object_name_idx ON grouper_ddl (object_name);

CREATE TABLE grouper_composites
(
    id VARCHAR(40) NOT NULL,
    owner VARCHAR(40) NOT NULL,
    left_factor VARCHAR(40) NOT NULL,
    right_factor VARCHAR(40) NOT NULL,
    type VARCHAR(32) NOT NULL,
    creator_id VARCHAR(40) NOT NULL,
    create_time BIGINT NOT NULL,
    hibernate_version_number BIGINT,
    context_id VARCHAR(40) NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX composite_composite_idx ON grouper_composites (owner);

CREATE INDEX composite_createtime_idx ON grouper_composites (create_time);

CREATE INDEX composite_creator_idx ON grouper_composites (creator_id);

CREATE INDEX composite_factor_idx ON grouper_composites (left_factor, right_factor);

CREATE INDEX composite_left_factor_idx ON grouper_composites (left_factor);

CREATE INDEX composite_right_factor_idx ON grouper_composites (right_factor);

CREATE INDEX composite_type_idx ON grouper_composites (type);

CREATE INDEX composite_context_idx ON grouper_composites (context_id);

CREATE TABLE grouper_fields
(
    id VARCHAR(40) NOT NULL,
    name VARCHAR(32) NOT NULL,
    read_privilege VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    write_privilege VARCHAR(32) NOT NULL,
    hibernate_version_number BIGINT,
    context_id VARCHAR(40) NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX name_and_type ON grouper_fields (name, type);

CREATE INDEX fields_context_idx ON grouper_fields (context_id);

CREATE INDEX grouper_fields_type_idx ON grouper_fields (type);

CREATE TABLE grouper_groups
(
    id VARCHAR(40) NOT NULL,
    parent_stem VARCHAR(40) NOT NULL,
    creator_id VARCHAR(40) NOT NULL,
    create_time BIGINT NOT NULL,
    modifier_id VARCHAR(40) NULL,
    modify_time BIGINT,
    last_membership_change BIGINT,
    last_imm_membership_change BIGINT,
    alternate_name VARCHAR(1024) NULL,
    enabled VARCHAR(1) DEFAULT 'T' NOT NULL,
    enabled_timestamp BIGINT,
    disabled_timestamp BIGINT,
    hibernate_version_number BIGINT,
    name VARCHAR(1024) NULL,
    display_name VARCHAR(1024) NULL,
    extension VARCHAR(255) NULL,
    display_extension VARCHAR(255) NULL,
    description VARCHAR(1024) NULL,
    context_id VARCHAR(40) NULL,
    type_of_group VARCHAR(10) DEFAULT 'group' NOT NULL,
    id_index BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX group_enabled_idx ON grouper_groups (enabled);

CREATE INDEX group_enabled_time_idx ON grouper_groups (enabled_timestamp);

CREATE INDEX group_disabled_time_idx ON grouper_groups (disabled_timestamp);

CREATE INDEX group_last_membership_idx ON grouper_groups (last_membership_change);

CREATE INDEX group_last_imm_membership_idx ON grouper_groups (last_imm_membership_change);

CREATE INDEX group_creator_idx ON grouper_groups (creator_id);

CREATE INDEX group_createtime_idx ON grouper_groups (create_time);

CREATE INDEX group_modifier_idx ON grouper_groups (modifier_id);

CREATE INDEX group_modifytime_idx ON grouper_groups (modify_time);

CREATE INDEX group_context_idx ON grouper_groups (context_id);

CREATE INDEX group_type_of_group_idx ON grouper_groups (type_of_group);

CREATE UNIQUE INDEX group_id_index_idx ON grouper_groups (id_index);

CREATE UNIQUE INDEX group_parent_idx ON grouper_groups (parent_stem, extension);

CREATE INDEX group_parent_display_idx ON grouper_groups (parent_stem, display_extension);

CREATE TABLE grouper_members
(
    id VARCHAR(40) NOT NULL,
    subject_id VARCHAR(255) NOT NULL,
    subject_source VARCHAR(255) NOT NULL,
    subject_type VARCHAR(255) NOT NULL,
    hibernate_version_number BIGINT,
    subject_identifier0 VARCHAR(255) NULL,
    sort_string0 VARCHAR(50) NULL,
    sort_string1 VARCHAR(50) NULL,
    sort_string2 VARCHAR(50) NULL,
    sort_string3 VARCHAR(50) NULL,
    sort_string4 VARCHAR(50) NULL,
    search_string0 VARCHAR(2048) NULL,
    search_string1 VARCHAR(2048) NULL,
    search_string2 VARCHAR(2048) NULL,
    search_string3 VARCHAR(2048) NULL,
    search_string4 VARCHAR(2048) NULL,
    name VARCHAR(2048) NULL,
    description VARCHAR(2048) NULL,
    context_id VARCHAR(40) NULL,
    subject_resolution_deleted VARCHAR(1) DEFAULT 'F' NOT NULL,
    subject_resolution_resolvable VARCHAR(1) DEFAULT 'T' NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX member_subjectsourcetype_idx ON grouper_members (subject_id, subject_source, subject_type);

CREATE INDEX member_subjectsource_idx ON grouper_members (subject_source);

CREATE INDEX member_subjectid_idx ON grouper_members (subject_id);

CREATE INDEX member_subjecttype_idx ON grouper_members (subject_type);

CREATE INDEX member_sort_string0_idx ON grouper_members (sort_string0);

CREATE INDEX member_sort_string1_idx ON grouper_members (sort_string1);

CREATE INDEX member_sort_string2_idx ON grouper_members (sort_string2);

CREATE INDEX member_sort_string3_idx ON grouper_members (sort_string3);

CREATE INDEX member_sort_string4_idx ON grouper_members (sort_string4);

CREATE INDEX member_context_idx ON grouper_members (context_id);

CREATE INDEX member_subjidentifier0_idx ON grouper_members (subject_identifier0);

CREATE INDEX member_resolvable_idx ON grouper_members (subject_resolution_resolvable);

CREATE INDEX member_deleted_idx ON grouper_members (subject_resolution_deleted);

CREATE TABLE grouper_memberships
(
    id VARCHAR(40) NOT NULL,
    member_id VARCHAR(40) NOT NULL,
    owner_id VARCHAR(40) NOT NULL,
    field_id VARCHAR(40) NOT NULL,
    owner_group_id VARCHAR(40) NULL,
    owner_stem_id VARCHAR(40) NULL,
    owner_attr_def_id VARCHAR(40) NULL,
    via_composite_id VARCHAR(40) NULL,
    enabled VARCHAR(1) DEFAULT 'T' NOT NULL,
    enabled_timestamp BIGINT,
    disabled_timestamp BIGINT,
    mship_type VARCHAR(32) NOT NULL,
    creator_id VARCHAR(40) NULL,
    create_time BIGINT NOT NULL,
    hibernate_version_number BIGINT,
    context_id VARCHAR(40) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX membership_owner_group_idx ON grouper_memberships (owner_group_id);

CREATE INDEX membership_owner_stem_idx ON grouper_memberships (owner_stem_id);

CREATE INDEX membership_owner_attr_idx ON grouper_memberships (owner_attr_def_id);

CREATE INDEX membership_via_composite_idx ON grouper_memberships (via_composite_id);

CREATE INDEX membership_member_cvia_idx ON grouper_memberships (member_id, via_composite_id);

CREATE INDEX membership_gowner_member_idx ON grouper_memberships (owner_group_id, member_id, field_id);

CREATE INDEX membership_sowner_member_idx ON grouper_memberships (owner_stem_id, member_id, field_id);

CREATE INDEX membership_aowner_member_idx ON grouper_memberships (owner_attr_def_id, member_id, field_id);

CREATE INDEX membership_enabled_idx ON grouper_memberships (enabled);

CREATE INDEX membership_enabled_time_idx ON grouper_memberships (enabled_timestamp);

CREATE INDEX membership_disabled_time_idx ON grouper_memberships (disabled_timestamp);

CREATE INDEX membership_createtime_idx ON grouper_memberships (create_time);

CREATE INDEX membership_creator_idx ON grouper_memberships (creator_id);

CREATE INDEX membership_member_idx ON grouper_memberships (member_id);

CREATE INDEX membership_member_list_idx ON grouper_memberships (member_id, field_id);

CREATE INDEX membership_gowner_field_type_idx ON grouper_memberships (owner_group_id, field_id, mship_type);

CREATE INDEX membership_sowner_field_type_idx ON grouper_memberships (owner_stem_id, field_id, mship_type);

CREATE INDEX membership_type_idx ON grouper_memberships (mship_type);

CREATE INDEX membership_context_idx ON grouper_memberships (context_id);

CREATE UNIQUE INDEX membership_uniq_idx ON grouper_memberships (owner_id, member_id, field_id);

CREATE INDEX groupmem_ownid_fieldid_idx ON grouper_memberships (owner_id, field_id);

CREATE TABLE grouper_group_set
(
    id VARCHAR(40) NOT NULL,
    owner_attr_def_id VARCHAR(40) NULL,
    owner_attr_def_id_null VARCHAR(40) DEFAULT '<NULL>' NOT NULL,
    owner_group_id VARCHAR(40) NULL,
    owner_group_id_null VARCHAR(40) DEFAULT '<NULL>' NOT NULL,
    owner_stem_id VARCHAR(40) NULL,
    owner_stem_id_null VARCHAR(40) DEFAULT '<NULL>' NOT NULL,
    member_attr_def_id VARCHAR(40) NULL,
    member_group_id VARCHAR(40) NULL,
    member_stem_id VARCHAR(40) NULL,
    member_id VARCHAR(40) NOT NULL,
    field_id VARCHAR(40) NOT NULL,
    member_field_id VARCHAR(40) NOT NULL,
    owner_id VARCHAR(40) NOT NULL,
    mship_type VARCHAR(16) NOT NULL,
    depth INTEGER NOT NULL,
    via_group_id VARCHAR(40) NULL,
    parent_id VARCHAR(40) NULL,
    creator_id VARCHAR(40) NOT NULL,
    create_time BIGINT NOT NULL,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX group_set_owner_field_idx ON grouper_group_set (owner_id, field_id);

CREATE UNIQUE INDEX group_set_uniq_idx ON grouper_group_set (member_id, field_id, owner_id, parent_id, mship_type);

CREATE INDEX group_set_creator_idx ON grouper_group_set (creator_id);

CREATE INDEX group_set_parent_idx ON grouper_group_set (parent_id);

CREATE INDEX group_set_via_group_idx ON grouper_group_set (via_group_id);

CREATE INDEX group_set_context_idx ON grouper_group_set (context_id);

CREATE INDEX group_set_gmember_idx ON grouper_group_set (member_group_id);

CREATE INDEX group_set_smember_idx ON grouper_group_set (member_stem_id);

CREATE INDEX group_set_amember_idx ON grouper_group_set (member_attr_def_id);

CREATE INDEX group_set_gowner_field_idx ON grouper_group_set (owner_group_id, field_id);

CREATE INDEX group_set_sowner_field_idx ON grouper_group_set (owner_stem_id, field_id);

CREATE INDEX group_set_aowner_field_idx ON grouper_group_set (owner_attr_def_id, field_id);

CREATE INDEX group_set_gowner_member_idx ON grouper_group_set (owner_group_id, member_group_id, field_id, depth);

CREATE INDEX group_set_sowner_member_idx ON grouper_group_set (owner_stem_id, member_stem_id, field_id, depth);

CREATE INDEX group_set_aowner_member_idx ON grouper_group_set (owner_attr_def_id, member_attr_def_id, field_id, depth);

CREATE TABLE grouper_stems
(
    id VARCHAR(40) NOT NULL,
    parent_stem VARCHAR(40) NULL,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    creator_id VARCHAR(40) NOT NULL,
    create_time BIGINT NOT NULL,
    modifier_id VARCHAR(40) NULL,
    modify_time BIGINT,
    display_extension VARCHAR(255) NOT NULL,
    extension VARCHAR(255) NOT NULL,
    description VARCHAR(1024) NULL,
    last_membership_change BIGINT,
    alternate_name VARCHAR(255) NULL,
    hibernate_version_number BIGINT,
    context_id VARCHAR(40) NULL,
    id_index BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX stem_alternate_name_idx ON grouper_stems (alternate_name);

CREATE INDEX stem_last_membership_idx ON grouper_stems (last_membership_change);

CREATE INDEX stem_createtime_idx ON grouper_stems (create_time);

CREATE INDEX stem_creator_idx ON grouper_stems (creator_id);

CREATE INDEX stem_dislpayextn_idx ON grouper_stems (display_extension);

CREATE INDEX stem_displayname_idx ON grouper_stems (display_name);

CREATE INDEX stem_extn_idx ON grouper_stems (extension);

CREATE INDEX stem_modifier_idx ON grouper_stems (modifier_id);

CREATE INDEX stem_modifytime_idx ON grouper_stems (modify_time);

CREATE UNIQUE INDEX stem_name_idx ON grouper_stems (name);

CREATE INDEX stem_parent_idx ON grouper_stems (parent_stem);

CREATE INDEX stem_context_idx ON grouper_stems (context_id);

CREATE UNIQUE INDEX stem_id_index_idx ON grouper_stems (id_index);

CREATE TABLE grouper_audit_type
(
    action_name VARCHAR(50) NULL,
    audit_category VARCHAR(50) NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    label_int01 VARCHAR(50) NULL,
    label_int02 VARCHAR(50) NULL,
    label_int03 VARCHAR(50) NULL,
    label_int04 VARCHAR(50) NULL,
    label_int05 VARCHAR(50) NULL,
    label_string01 VARCHAR(50) NULL,
    label_string02 VARCHAR(50) NULL,
    label_string03 VARCHAR(50) NULL,
    label_string04 VARCHAR(50) NULL,
    label_string05 VARCHAR(50) NULL,
    label_string06 VARCHAR(50) NULL,
    label_string07 VARCHAR(50) NULL,
    label_string08 VARCHAR(50) NULL,
    last_updated BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX audit_type_category_type_idx ON grouper_audit_type (audit_category, action_name);

CREATE TABLE grouper_audit_entry
(
    act_as_member_id VARCHAR(40) NULL,
    audit_type_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    description text NULL,
    env_name VARCHAR(50) NULL,
    grouper_engine VARCHAR(50) NULL,
    grouper_version VARCHAR(20) NULL,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    int01 BIGINT,
    int02 BIGINT,
    int03 BIGINT,
    int04 BIGINT,
    int05 BIGINT,
    last_updated BIGINT,
    logged_in_member_id VARCHAR(40) NULL,
    server_host VARCHAR(50) NULL,
    string01 text NULL,
    string02 text NULL,
    string03 text NULL,
    string04 text NULL,
    string05 text NULL,
    string06 text NULL,
    string07 text NULL,
    string08 text NULL,
    user_ip_address VARCHAR(50) NULL,
    duration_microseconds BIGINT,
    query_count INTEGER,
    server_user_name VARCHAR(50) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX audit_entry_act_as_idx ON grouper_audit_entry (act_as_member_id);

CREATE INDEX audit_entry_act_as_created_idx ON grouper_audit_entry (act_as_member_id, created_on);

CREATE INDEX audit_entry_type_idx ON grouper_audit_entry (audit_type_id);

CREATE INDEX audit_entry_context_idx ON grouper_audit_entry (context_id);

CREATE INDEX audit_entry_logged_in_idx ON grouper_audit_entry (logged_in_member_id);

CREATE TABLE grouper_change_log_type
(
    action_name VARCHAR(50) NULL,
    change_log_category VARCHAR(50) NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    label_string01 VARCHAR(50) NULL,
    label_string02 VARCHAR(50) NULL,
    label_string03 VARCHAR(50) NULL,
    label_string04 VARCHAR(50) NULL,
    label_string05 VARCHAR(50) NULL,
    label_string06 VARCHAR(50) NULL,
    label_string07 VARCHAR(50) NULL,
    label_string08 VARCHAR(50) NULL,
    label_string09 VARCHAR(50) NULL,
    label_string10 VARCHAR(50) NULL,
    label_string11 VARCHAR(50) NULL,
    label_string12 VARCHAR(50) NULL,
    last_updated BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX change_log_type_cat_type_idx ON grouper_change_log_type (change_log_category, action_name);

CREATE TABLE grouper_change_log_consumer
(
    name VARCHAR(100) NOT NULL,
    last_sequence_processed BIGINT,
    last_updated BIGINT,
    created_on BIGINT,
    id VARCHAR(40) NOT NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX change_log_consumer_name_idx ON grouper_change_log_consumer (name);

CREATE TABLE grouper_change_log_entry_temp
(
    id VARCHAR(40) NOT NULL,
    change_log_type_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT NOT NULL,
    string01 text NULL,
    string02 text NULL,
    string03 text NULL,
    string04 text NULL,
    string05 text NULL,
    string06 text NULL,
    string07 text NULL,
    string08 text NULL,
    string09 text NULL,
    string10 text NULL,
    string11 text NULL,
    string12 text NULL,
    PRIMARY KEY (id)
);

CREATE INDEX change_log_temp_created_on_idx ON grouper_change_log_entry_temp (created_on);

CREATE TABLE grouper_change_log_entry
(
    change_log_type_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    sequence_number BIGINT,
    string01 text NULL,
    string02 text NULL,
    string03 text NULL,
    string04 text NULL,
    string05 text NULL,
    string06 text NULL,
    string07 text NULL,
    string08 text NULL,
    string09 text NULL,
    string10 text NULL,
    string11 text NULL,
    string12 text NULL,
    PRIMARY KEY (sequence_number)
);

CREATE INDEX change_log_sequence_number_idx ON grouper_change_log_entry (sequence_number, created_on);

CREATE INDEX change_log_context_id_idx ON grouper_change_log_entry (context_id);

CREATE INDEX change_log_created_on_idx ON grouper_change_log_entry (created_on);

CREATE TABLE grouper_attribute_def
(
    attribute_def_public VARCHAR(1) DEFAULT 'F' NOT NULL,
    attribute_def_type VARCHAR(32) DEFAULT 'attr' NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    creator_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    last_updated BIGINT,
    id VARCHAR(40) NOT NULL,
    description VARCHAR(1024) NULL,
    extension VARCHAR(255) NOT NULL,
    name VARCHAR(1024) NOT NULL,
    multi_assignable VARCHAR(1) DEFAULT 'F' NOT NULL,
    multi_valued VARCHAR(1) DEFAULT 'F' NOT NULL,
    stem_id VARCHAR(40) NOT NULL,
    value_type VARCHAR(32) DEFAULT 'marker' NOT NULL,
    assign_to_attribute_def VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_attribute_def_assn VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_eff_membership VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_eff_membership_assn VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_group VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_group_assn VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_imm_membership VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_imm_membership_assn VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_member VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_member_assn VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_stem VARCHAR(1) DEFAULT 'F' NOT NULL,
    assign_to_stem_assn VARCHAR(1) DEFAULT 'F' NOT NULL,
    id_index BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attribute_def_type_idx ON grouper_attribute_def (attribute_def_type);

CREATE UNIQUE INDEX attribute_def_id_index_idx ON grouper_attribute_def (id_index);

CREATE TABLE grouper_attribute_def_name
(
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    last_updated BIGINT,
    id VARCHAR(40) NOT NULL,
    description VARCHAR(1024) NULL,
    extension VARCHAR(255) NOT NULL,
    name VARCHAR(1024) NOT NULL,
    stem_id VARCHAR(40) NOT NULL,
    attribute_def_id VARCHAR(40) NOT NULL,
    display_extension VARCHAR(128) NOT NULL,
    display_name VARCHAR(1024) NOT NULL,
    id_index BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX attr_def_name_id_index_idx ON grouper_attribute_def_name (id_index);

CREATE TABLE grouper_attribute_assign
(
    attribute_assign_action_id VARCHAR(40) NOT NULL,
    attribute_def_name_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    disabled_time BIGINT,
    enabled VARCHAR(1) DEFAULT 'T' NOT NULL,
    enabled_time BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    notes VARCHAR(1024) NULL,
    attribute_assign_delegatable VARCHAR(15) NOT NULL,
    attribute_assign_type VARCHAR(15) NOT NULL,
    owner_attribute_assign_id VARCHAR(40) NULL,
    owner_attribute_def_id VARCHAR(40) NULL,
    owner_group_id VARCHAR(40) NULL,
    owner_member_id VARCHAR(40) NULL,
    owner_membership_id VARCHAR(40) NULL,
    owner_stem_id VARCHAR(40) NULL,
    disallowed VARCHAR(1) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attribute_asgn_attr_name_idx ON grouper_attribute_assign (attribute_def_name_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_own_asgn_idx ON grouper_attribute_assign (owner_attribute_assign_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_own_def_idx ON grouper_attribute_assign (owner_attribute_def_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_own_group_idx ON grouper_attribute_assign (owner_group_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_own_mem_idx ON grouper_attribute_assign (owner_member_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_own_mship_idx ON grouper_attribute_assign (owner_membership_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_own_stem_idx ON grouper_attribute_assign (owner_stem_id, attribute_assign_action_id);

CREATE INDEX attr_asgn_type_idx ON grouper_attribute_assign (attribute_assign_type);

CREATE TABLE grouper_attribute_assign_value
(
    attribute_assign_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    value_integer BIGINT,
    value_floating DOUBLE,
    value_string text NULL,
    value_member_id VARCHAR(40) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attribute_val_assign_idx ON grouper_attribute_assign_value (attribute_assign_id);

CREATE INDEX attribute_val_integer_idx ON grouper_attribute_assign_value (value_integer);

CREATE INDEX attribute_val_member_id_idx ON grouper_attribute_assign_value (value_member_id);

CREATE TABLE grouper_attribute_def_scope
(
    attribute_def_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    attribute_def_scope_type VARCHAR(32) NULL,
    scope_string VARCHAR(1024) NULL,
    scope_string2 VARCHAR(1024) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attribute_def_scope_atdef_idx ON grouper_attribute_def_scope (attribute_def_id);

CREATE TABLE grouper_attribute_def_name_set
(
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    depth BIGINT NOT NULL,
    if_has_attribute_def_name_id VARCHAR(40) NOT NULL,
    then_has_attribute_def_name_id VARCHAR(40) NOT NULL,
    parent_attr_def_name_set_id VARCHAR(40) NULL,
    type VARCHAR(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attr_def_name_set_ifhas_idx ON grouper_attribute_def_name_set (if_has_attribute_def_name_id);

CREATE INDEX attr_def_name_set_then_idx ON grouper_attribute_def_name_set (then_has_attribute_def_name_id);

CREATE UNIQUE INDEX attr_def_name_set_unq_idx ON grouper_attribute_def_name_set (parent_attr_def_name_set_id, if_has_attribute_def_name_id, then_has_attribute_def_name_id);

CREATE TABLE grouper_attr_assign_action
(
    attribute_def_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    name VARCHAR(40) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attr_assn_act_def_id_idx ON grouper_attr_assign_action (attribute_def_id);

CREATE TABLE grouper_attr_assign_action_set
(
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    depth BIGINT NOT NULL,
    if_has_attr_assn_action_id VARCHAR(40) NOT NULL,
    then_has_attr_assn_action_id VARCHAR(40) NOT NULL,
    parent_attr_assn_action_id VARCHAR(40) NULL,
    type VARCHAR(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX action_set_ifhas_idx ON grouper_attr_assign_action_set (if_has_attr_assn_action_id);

CREATE INDEX action_set_then_idx ON grouper_attr_assign_action_set (then_has_attr_assn_action_id);

CREATE UNIQUE INDEX action_set_unq_idx ON grouper_attr_assign_action_set (parent_attr_assn_action_id, if_has_attr_assn_action_id, then_has_attr_assn_action_id);

CREATE TABLE grouper_role_set
(
    context_id VARCHAR(40) NULL,
    created_on BIGINT,
    hibernate_version_number BIGINT,
    id VARCHAR(40) NOT NULL,
    last_updated BIGINT,
    depth BIGINT NOT NULL,
    if_has_role_id VARCHAR(40) NOT NULL,
    then_has_role_id VARCHAR(40) NOT NULL,
    parent_role_set_id VARCHAR(40) NULL,
    type VARCHAR(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX role_set_ifhas_idx ON grouper_role_set (if_has_role_id);

CREATE INDEX role_set_then_idx ON grouper_role_set (then_has_role_id);

CREATE UNIQUE INDEX role_set_unq_idx ON grouper_role_set (parent_role_set_id, if_has_role_id, then_has_role_id);

CREATE TABLE grouper_pit_members
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    subject_id VARCHAR(255) NOT NULL,
    subject_source VARCHAR(255) NOT NULL,
    subject_type VARCHAR(255) NOT NULL,
    subject_identifier0 VARCHAR(255) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_member_source_id_idx ON grouper_pit_members (source_id);

CREATE INDEX pit_member_subject_id_idx ON grouper_pit_members (subject_id);

CREATE INDEX pit_member_context_idx ON grouper_pit_members (context_id);

CREATE UNIQUE INDEX pit_member_start_idx ON grouper_pit_members (start_time, source_id);

CREATE INDEX pit_member_end_idx ON grouper_pit_members (end_time);

CREATE INDEX pit_member_subjidentifier0_idx ON grouper_pit_members (subject_identifier0);

CREATE TABLE grouper_pit_fields
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    name VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_field_source_id_idx ON grouper_pit_fields (source_id);

CREATE INDEX pit_field_name_idx ON grouper_pit_fields (name);

CREATE INDEX pit_field_context_idx ON grouper_pit_fields (context_id);

CREATE UNIQUE INDEX pit_field_start_idx ON grouper_pit_fields (start_time, source_id);

CREATE INDEX pit_field_end_idx ON grouper_pit_fields (end_time);

CREATE TABLE grouper_pit_groups
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    name VARCHAR(1024) NOT NULL,
    stem_id VARCHAR(40) NOT NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_group_source_id_idx ON grouper_pit_groups (source_id);

CREATE INDEX pit_group_parent_idx ON grouper_pit_groups (stem_id);

CREATE INDEX pit_group_context_idx ON grouper_pit_groups (context_id);

CREATE UNIQUE INDEX pit_group_start_idx ON grouper_pit_groups (start_time, source_id);

CREATE INDEX pit_group_end_idx ON grouper_pit_groups (end_time);

CREATE TABLE grouper_pit_stems
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    name VARCHAR(1024) NOT NULL,
    parent_stem_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_stem_source_id_idx ON grouper_pit_stems (source_id);

CREATE INDEX pit_stem_parent_idx ON grouper_pit_stems (parent_stem_id);

CREATE INDEX pit_stem_context_idx ON grouper_pit_stems (context_id);

CREATE UNIQUE INDEX pit_stem_start_idx ON grouper_pit_stems (start_time, source_id);

CREATE INDEX pit_stem_end_idx ON grouper_pit_stems (end_time);

CREATE TABLE grouper_pit_attribute_def
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    name VARCHAR(1024) NOT NULL,
    stem_id VARCHAR(40) NOT NULL,
    attribute_def_type VARCHAR(32) NOT NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_def_source_id_idx ON grouper_pit_attribute_def (source_id);

CREATE INDEX pit_attribute_def_parent_idx ON grouper_pit_attribute_def (stem_id);

CREATE INDEX pit_attribute_def_context_idx ON grouper_pit_attribute_def (context_id);

CREATE INDEX pit_attribute_def_type_idx ON grouper_pit_attribute_def (attribute_def_type);

CREATE UNIQUE INDEX pit_attribute_def_start_idx ON grouper_pit_attribute_def (start_time, source_id);

CREATE INDEX pit_attribute_def_end_idx ON grouper_pit_attribute_def (end_time);

CREATE TABLE grouper_pit_memberships
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    owner_id VARCHAR(40) NOT NULL,
    owner_attr_def_id VARCHAR(40) NULL,
    owner_group_id VARCHAR(40) NULL,
    owner_stem_id VARCHAR(40) NULL,
    member_id VARCHAR(40) NOT NULL,
    field_id VARCHAR(40) NOT NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_ms_source_id_idx ON grouper_pit_memberships (source_id);

CREATE INDEX pit_ms_context_idx ON grouper_pit_memberships (context_id);

CREATE INDEX pit_ms_owner_attr_def_idx ON grouper_pit_memberships (owner_attr_def_id);

CREATE INDEX pit_ms_owner_stem_idx ON grouper_pit_memberships (owner_stem_id);

CREATE INDEX pit_ms_owner_group_idx ON grouper_pit_memberships (owner_group_id);

CREATE INDEX pit_ms_member_idx ON grouper_pit_memberships (member_id);

CREATE INDEX pit_ms_field_idx ON grouper_pit_memberships (field_id);

CREATE INDEX pit_ms_owner_field_idx ON grouper_pit_memberships (owner_id, field_id);

CREATE INDEX pit_ms_owner_member_field_idx ON grouper_pit_memberships (owner_id, member_id, field_id);

CREATE UNIQUE INDEX pit_ms_start_idx ON grouper_pit_memberships (start_time, source_id);

CREATE INDEX pit_ms_end_idx ON grouper_pit_memberships (end_time);

CREATE TABLE grouper_pit_group_set
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    owner_id VARCHAR(40) NOT NULL,
    owner_attr_def_id VARCHAR(40) NULL,
    owner_group_id VARCHAR(40) NULL,
    owner_stem_id VARCHAR(40) NULL,
    member_id VARCHAR(40) NOT NULL,
    member_attr_def_id VARCHAR(40) NULL,
    member_group_id VARCHAR(40) NULL,
    member_stem_id VARCHAR(40) NULL,
    field_id VARCHAR(40) NOT NULL,
    member_field_id VARCHAR(40) NOT NULL,
    depth INTEGER NOT NULL,
    parent_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_gs_source_id_idx ON grouper_pit_group_set (source_id);

CREATE INDEX pit_gs_context_idx ON grouper_pit_group_set (context_id);

CREATE INDEX pit_gs_owner_attr_def_idx ON grouper_pit_group_set (owner_attr_def_id);

CREATE INDEX pit_gs_owner_group_idx ON grouper_pit_group_set (owner_group_id);

CREATE INDEX pit_gs_owner_stem_idx ON grouper_pit_group_set (owner_stem_id);

CREATE INDEX pit_gs_member_idx ON grouper_pit_group_set (member_id);

CREATE INDEX pit_gs_member_attr_def_idx ON grouper_pit_group_set (member_attr_def_id);

CREATE INDEX pit_gs_member_group_idx ON grouper_pit_group_set (member_group_id);

CREATE INDEX pit_gs_member_stem_idx ON grouper_pit_group_set (member_stem_id);

CREATE INDEX pit_gs_field_idx ON grouper_pit_group_set (field_id);

CREATE INDEX pit_gs_member_field_idx ON grouper_pit_group_set (member_field_id);

CREATE INDEX pit_gs_parent_idx ON grouper_pit_group_set (parent_id);

CREATE INDEX pit_gs_member_member_field_idx ON grouper_pit_group_set (member_id, member_field_id);

CREATE INDEX pit_gs_group_field_member_idx ON grouper_pit_group_set (owner_group_id, field_id, member_id);

CREATE INDEX pit_gs_owner_field_idx ON grouper_pit_group_set (owner_id, field_id);

CREATE INDEX pit_gs_owner_member_field_idx ON grouper_pit_group_set (owner_id, member_id, field_id);

CREATE UNIQUE INDEX pit_gs_start_idx ON grouper_pit_group_set (start_time, source_id);

CREATE INDEX pit_gs_end_idx ON grouper_pit_group_set (end_time);

CREATE TABLE grouper_pit_attribute_assign
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    attribute_def_name_id VARCHAR(40) NOT NULL,
    attribute_assign_action_id VARCHAR(40) NOT NULL,
    attribute_assign_type VARCHAR(15) NOT NULL,
    owner_attribute_assign_id VARCHAR(40) NULL,
    owner_attribute_def_id VARCHAR(40) NULL,
    owner_group_id VARCHAR(40) NULL,
    owner_member_id VARCHAR(40) NULL,
    owner_membership_id VARCHAR(40) NULL,
    owner_stem_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    disallowed VARCHAR(1) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_assn_source_id_idx ON grouper_pit_attribute_assign (source_id);

CREATE INDEX pit_attr_assn_action_idx ON grouper_pit_attribute_assign (attribute_assign_action_id);

CREATE INDEX pit_attr_assn_type_idx ON grouper_pit_attribute_assign (attribute_assign_type);

CREATE INDEX pit_attr_assn_def_name_idx ON grouper_pit_attribute_assign (attribute_def_name_id, attribute_assign_action_id);

CREATE INDEX pit_attr_assn_own_assn_idx ON grouper_pit_attribute_assign (owner_attribute_assign_id, attribute_assign_action_id);

CREATE INDEX pit_attr_assn_own_def_idx ON grouper_pit_attribute_assign (owner_attribute_def_id, attribute_assign_action_id);

CREATE INDEX pit_attr_assn_own_group_idx ON grouper_pit_attribute_assign (owner_group_id, attribute_assign_action_id);

CREATE INDEX pit_attr_assn_own_mem_idx ON grouper_pit_attribute_assign (owner_member_id, attribute_assign_action_id);

CREATE INDEX pit_attr_assn_own_mship_idx ON grouper_pit_attribute_assign (owner_membership_id, attribute_assign_action_id);

CREATE INDEX pit_attr_assn_own_stem_idx ON grouper_pit_attribute_assign (owner_stem_id, attribute_assign_action_id);

CREATE UNIQUE INDEX pit_attr_assn_start_idx ON grouper_pit_attribute_assign (start_time, source_id);

CREATE INDEX pit_attr_assn_end_idx ON grouper_pit_attribute_assign (end_time);

CREATE TABLE grouper_pit_attr_assn_value
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    attribute_assign_id VARCHAR(40) NOT NULL,
    value_integer BIGINT,
    value_floating DOUBLE,
    value_string text NULL,
    value_member_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_val_source_id_idx ON grouper_pit_attr_assn_value (source_id);

CREATE INDEX pit_attr_val_assign_idx ON grouper_pit_attr_assn_value (attribute_assign_id);

CREATE INDEX pit_attr_val_integer_idx ON grouper_pit_attr_assn_value (value_integer);

CREATE INDEX pit_attr_val_floating_idx ON grouper_pit_attr_assn_value (value_floating);

CREATE INDEX pit_attr_val_member_id_idx ON grouper_pit_attr_assn_value (value_member_id);

CREATE UNIQUE INDEX pit_attr_val_start_idx ON grouper_pit_attr_assn_value (start_time, source_id);

CREATE INDEX pit_attr_val_end_idx ON grouper_pit_attr_assn_value (end_time);

CREATE TABLE grouper_pit_attr_assn_actn
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    attribute_def_id VARCHAR(40) NOT NULL,
    name VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_asn_act_source_id_idx ON grouper_pit_attr_assn_actn (source_id);

CREATE INDEX pit_attr_assn_act_def_id_idx ON grouper_pit_attr_assn_actn (attribute_def_id);

CREATE UNIQUE INDEX pit_attr_assn_act_start_idx ON grouper_pit_attr_assn_actn (start_time, source_id);

CREATE INDEX pit_attr_assn_act_end_idx ON grouper_pit_attr_assn_actn (end_time);

CREATE TABLE grouper_pit_attr_def_name
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    stem_id VARCHAR(40) NOT NULL,
    attribute_def_id VARCHAR(40) NOT NULL,
    name VARCHAR(1024) NOT NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_attrdef_name_srcid_idx ON grouper_pit_attr_def_name (source_id);

CREATE INDEX pit_attr_def_name_stem_idx ON grouper_pit_attr_def_name (stem_id);

CREATE INDEX pit_attr_def_name_def_idx ON grouper_pit_attr_def_name (attribute_def_id);

CREATE UNIQUE INDEX pit_attr_def_name_start_idx ON grouper_pit_attr_def_name (start_time, source_id);

CREATE INDEX pit_attr_def_name_end_idx ON grouper_pit_attr_def_name (end_time);

CREATE TABLE grouper_pit_attr_def_name_set
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    depth BIGINT NOT NULL,
    if_has_attribute_def_name_id VARCHAR(40) NOT NULL,
    then_has_attribute_def_name_id VARCHAR(40) NOT NULL,
    parent_attr_def_name_set_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_attrdef_name_set_srcid_idx ON grouper_pit_attr_def_name_set (source_id);

CREATE INDEX pit_attr_def_name_set_if_idx ON grouper_pit_attr_def_name_set (if_has_attribute_def_name_id);

CREATE INDEX pit_attr_def_name_set_then_idx ON grouper_pit_attr_def_name_set (then_has_attribute_def_name_id);

CREATE INDEX pit_attr_def_name_set_prnt_idx ON grouper_pit_attr_def_name_set (parent_attr_def_name_set_id);

CREATE UNIQUE INDEX pit_attr_def_name_set_strt_idx ON grouper_pit_attr_def_name_set (start_time, source_id);

CREATE INDEX pit_attr_def_name_set_end_idx ON grouper_pit_attr_def_name_set (end_time);

CREATE TABLE grouper_pit_attr_assn_actn_set
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    depth BIGINT NOT NULL,
    if_has_attr_assn_action_id VARCHAR(40) NOT NULL,
    then_has_attr_assn_action_id VARCHAR(40) NOT NULL,
    parent_attr_assn_action_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_action_set_source_id_idx ON grouper_pit_attr_assn_actn_set (source_id);

CREATE INDEX pit_action_set_if_idx ON grouper_pit_attr_assn_actn_set (if_has_attr_assn_action_id);

CREATE INDEX pit_action_set_then_idx ON grouper_pit_attr_assn_actn_set (then_has_attr_assn_action_id);

CREATE INDEX pit_action_set_parent_idx ON grouper_pit_attr_assn_actn_set (parent_attr_assn_action_id);

CREATE UNIQUE INDEX pit_action_set_start_idx ON grouper_pit_attr_assn_actn_set (start_time, source_id);

CREATE INDEX pit_action_set_end_idx ON grouper_pit_attr_assn_actn_set (end_time);

CREATE TABLE grouper_pit_role_set
(
    id VARCHAR(40) NOT NULL,
    source_id VARCHAR(40) NOT NULL,
    depth BIGINT NOT NULL,
    if_has_role_id VARCHAR(40) NOT NULL,
    then_has_role_id VARCHAR(40) NOT NULL,
    parent_role_set_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_rs_source_id_idx ON grouper_pit_role_set (source_id);

CREATE INDEX pit_rs_if_idx ON grouper_pit_role_set (if_has_role_id);

CREATE INDEX pit_rs_then_idx ON grouper_pit_role_set (then_has_role_id);

CREATE INDEX pit_rs_parent_idx ON grouper_pit_role_set (parent_role_set_id);

CREATE UNIQUE INDEX pit_rs_start_idx ON grouper_pit_role_set (start_time, source_id);

CREATE INDEX pit_rs_end_idx ON grouper_pit_role_set (end_time);

CREATE TABLE grouper_ext_subj
(
    uuid VARCHAR(40) NOT NULL,
    name VARCHAR(200) NULL,
    identifier VARCHAR(300) NULL,
    description VARCHAR(500) NULL,
    institution VARCHAR(200) NULL,
    email VARCHAR(200) NULL,
    search_string_lower text NULL,
    create_time BIGINT NOT NULL,
    creator_member_id VARCHAR(40) NOT NULL,
    modify_time BIGINT NOT NULL,
    modifier_member_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NOT NULL,
    enabled VARCHAR(1) NOT NULL,
    disabled_time BIGINT,
    hibernate_version_number BIGINT NOT NULL,
    vetted_email_addresses text NULL,
    PRIMARY KEY (uuid)
);

CREATE INDEX grouper_ext_subj_cxt_id_idx ON grouper_ext_subj (context_id);

CREATE TABLE grouper_ext_subj_attr
(
    uuid VARCHAR(40) NOT NULL,
    attribute_system_name VARCHAR(200) NOT NULL,
    attribute_value VARCHAR(600) NULL,
    subject_uuid VARCHAR(40) NOT NULL,
    create_time BIGINT NOT NULL,
    creator_member_id VARCHAR(40) NOT NULL,
    modify_time BIGINT NOT NULL,
    modifier_member_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    PRIMARY KEY (uuid)
);

CREATE INDEX grouper_extsubjattr_cxtid_idx ON grouper_ext_subj_attr (context_id);

CREATE UNIQUE INDEX grouper_extsubjattr_subj_idx ON grouper_ext_subj_attr (subject_uuid, attribute_system_name);

CREATE TABLE grouper_stem_set
(
    id VARCHAR(40) NOT NULL,
    if_has_stem_id VARCHAR(40) NOT NULL,
    then_has_stem_id VARCHAR(40) NOT NULL,
    parent_stem_set_id VARCHAR(40) NULL,
    type VARCHAR(32) NOT NULL,
    depth BIGINT NOT NULL,
    created_on BIGINT,
    last_updated BIGINT,
    context_id VARCHAR(40) NULL,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX stem_set_ifhas_idx ON grouper_stem_set (if_has_stem_id);

CREATE INDEX stem_set_then_idx ON grouper_stem_set (then_has_stem_id);

CREATE UNIQUE INDEX stem_set_unq_idx ON grouper_stem_set (parent_stem_set_id, if_has_stem_id, then_has_stem_id);

CREATE TABLE grouper_table_index
(
    id VARCHAR(40) NOT NULL,
    type VARCHAR(32) NOT NULL,
    last_index_reserved BIGINT,
    created_on BIGINT,
    last_updated BIGINT,
    hibernate_version_number BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX table_index_type_idx ON grouper_table_index (type);

CREATE TABLE grouper_loader_log
(
    id VARCHAR(40) NOT NULL,
    job_name VARCHAR(512) NULL,
    status VARCHAR(20) NULL,
    started_time DATETIME,
    ended_time DATETIME,
    millis INTEGER,
    millis_get_data INTEGER,
    millis_load_data INTEGER,
    job_type VARCHAR(128) NULL,
    job_schedule_type VARCHAR(128) NULL,
    job_description text NULL,
    job_message text NULL,
    host VARCHAR(128) NULL,
    group_uuid VARCHAR(40) NULL,
    job_schedule_quartz_cron VARCHAR(128) NULL,
    job_schedule_interval_seconds INTEGER,
    last_updated DATETIME,
    unresolvable_subject_count INTEGER,
    insert_count INTEGER,
    update_count INTEGER,
    delete_count INTEGER,
    total_count INTEGER,
    parent_job_name VARCHAR(512) NULL,
    parent_job_id VARCHAR(40) NULL,
    and_group_names VARCHAR(512) NULL,
    job_schedule_priority INTEGER,
    context_id VARCHAR(40) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX loader_context_idx ON grouper_loader_log (context_id);

CREATE TABLE grouper_message
(
    id VARCHAR(40) NOT NULL,
    sent_time_micros BIGINT NOT NULL,
    get_attempt_time_millis BIGINT NOT NULL,
    get_attempt_count BIGINT NOT NULL,
    state VARCHAR(20) NOT NULL,
    get_time_millis BIGINT,
    from_member_id VARCHAR(40) NOT NULL,
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

CREATE TABLE grouper_config
(
    id VARCHAR(40) NOT NULL,
    config_file_name VARCHAR(100) NOT NULL,
    config_key VARCHAR(400) NOT NULL,
    config_value text NULL,
    config_comment text NULL,
    config_file_hierarchy VARCHAR(50) NOT NULL,
    config_encrypted VARCHAR(1) NOT NULL,
    config_sequence BIGINT NOT NULL,
    config_version_index BIGINT,
    last_updated BIGINT NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    config_value_clob MEDIUMTEXT,
    config_value_bytes BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX grpconfig_config_file_idx ON grouper_config (config_file_name, last_updated);

CREATE INDEX grpconfig_last_updated_idx ON grouper_config (last_updated);

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
    last_full_sync_start DATETIME,
    last_full_sync_run DATETIME,
    last_full_metadata_sync_start DATETIME,
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
    last_sync_start DATETIME,
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
    last_group_sync_start DATETIME,
    last_group_sync DATETIME,
    last_group_metadata_sync_start DATETIME,
    last_group_metadata_sync DATETIME,
    group_from_id2 text NULL,
    group_from_id3 text NULL,
    group_to_id2 text NULL,
    group_to_id3 text NULL,
    metadata_updated DATETIME,
    error_message text NULL,
    error_timestamp DATETIME,
    last_time_work_was_done DATETIME,
    error_code VARCHAR(3) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_gr_sync_id_idx ON grouper_sync_group (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_gr_group_id_idx ON grouper_sync_group (group_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_gr_sy_gr_idx ON grouper_sync_group (grouper_sync_id, group_id);

CREATE INDEX grouper_sync_gr_er_idx ON grouper_sync_group (grouper_sync_id, error_code, error_timestamp);

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
    last_user_sync_start DATETIME,
    last_user_sync DATETIME,
    last_user_metadata_sync_start DATETIME,
    last_user_metadata_sync DATETIME,
    member_from_id2 text NULL,
    member_from_id3 text NULL,
    member_to_id2 text NULL,
    member_to_id3 text NULL,
    metadata_updated DATETIME,
    last_time_work_was_done DATETIME,
    error_message text NULL,
    error_timestamp DATETIME,
    error_code VARCHAR(3) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_us_sync_id_idx ON grouper_sync_member (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_us_mem_id_idx ON grouper_sync_member (member_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_us_sm_idx ON grouper_sync_member (grouper_sync_id, member_id);

CREATE INDEX grouper_sync_us_st_gr_idx ON grouper_sync_member (grouper_sync_id, source_id, subject_id);

CREATE INDEX grouper_sync_us_er_idx ON grouper_sync_member (grouper_sync_id, error_code, error_timestamp);

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
    error_code VARCHAR(3) NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_id, grouper_sync_group_id, grouper_sync_member_id);

CREATE INDEX grouper_sync_mship_me_idx ON grouper_sync_membership (grouper_sync_group_id, last_updated);

CREATE INDEX grouper_sync_mship_sy_idx ON grouper_sync_membership (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_mship_er_idx ON grouper_sync_membership (grouper_sync_id, error_code, error_timestamp);

CREATE TABLE grouper_sync_log
(
    id VARCHAR(40) NOT NULL,
    grouper_sync_owner_id VARCHAR(40) NULL,
    grouper_sync_id VARCHAR(40) NULL,
    status VARCHAR(20) NULL,
    sync_timestamp_start DATETIME,
    sync_timestamp DATETIME,
    description text NULL,
    records_processed INTEGER,
    records_changed INTEGER,
    job_took_millis INTEGER,
    server VARCHAR(200) NULL,
    last_updated DATETIME NOT NULL,
    description_clob MEDIUMTEXT,
    description_bytes BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_log_sy_idx ON grouper_sync_log (grouper_sync_id, sync_timestamp);

CREATE INDEX grouper_sync_log_ow_idx ON grouper_sync_log (grouper_sync_owner_id, sync_timestamp);

CREATE TABLE grouper_time
(
    time_label VARCHAR(10) NOT NULL,
    the_utc_timestamp DATETIME NOT NULL,
    this_tz_timestamp DATETIME NOT NULL,
    utc_millis_since_1970 BIGINT NOT NULL,
    utc_micros_since_1970 BIGINT NOT NULL,
    PRIMARY KEY (time_label)
);

CREATE TABLE grouper_cache_overall
(
    overall_cache INTEGER NOT NULL,
    nanos_since_1970 BIGINT NOT NULL,
    PRIMARY KEY (overall_cache)
);

CREATE TABLE grouper_cache_instance
(
    cache_name VARCHAR(250) NOT NULL,
    nanos_since_1970 BIGINT NOT NULL,
    PRIMARY KEY (cache_name)
);

CREATE INDEX grouper_cache_inst_cache_idx ON grouper_cache_instance (nanos_since_1970);

CREATE TABLE grouper_recent_mships_conf
(
    group_uuid_to VARCHAR(40) NOT NULL,
    group_name_to VARCHAR(1024) NOT NULL,
    group_uuid_from VARCHAR(40) NOT NULL,
    group_name_from VARCHAR(1024) NOT NULL,
    recent_micros BIGINT NOT NULL,
    include_eligible VARCHAR(1) NOT NULL,
    PRIMARY KEY (group_uuid_to)
);

CREATE INDEX grouper_recent_mships_idfr_idx ON grouper_recent_mships_conf (group_uuid_from);

CREATE TABLE grouper_pit_config 
(
    id VARCHAR(40) NOT NULL,
    config_file_name VARCHAR(100) NOT NULL,
    config_key VARCHAR(400) NOT NULL,
    config_value text NULL,
    config_comment text NULL,
    config_file_hierarchy VARCHAR(50) NOT NULL,
    config_encrypted VARCHAR(1) NOT NULL,
    config_sequence BIGINT NOT NULL,
    config_version_index BIGINT,
    last_updated BIGINT NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    config_value_clob MEDIUMTEXT,
    config_value_bytes BIGINT,
    prev_config_value text NULL,
    prev_config_value_clob MEDIUMTEXT,
    source_id VARCHAR(40) NOT NULL,
    context_id VARCHAR(40) NULL,
    active VARCHAR(1) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    PRIMARY KEY (id)
);

CREATE INDEX pit_config_context_idx ON grouper_pit_config (context_id);

CREATE INDEX pit_config_source_id_idx ON grouper_pit_config (source_id);

CREATE UNIQUE INDEX pit_config_start_idx ON grouper_pit_config (start_time, source_id);

CREATE INDEX pit_config_end_idx ON grouper_pit_config (end_time);

CREATE TABLE grouper_file
(
    id VARCHAR(40) NOT NULL,
    system_name VARCHAR(100) NOT NULL,
    file_name VARCHAR(100) NOT NULL,
    file_path VARCHAR(400) NOT NULL,
    hibernate_version_number BIGINT NOT NULL,
    context_id VARCHAR(40),
    file_contents_varchar VARCHAR(4000),
    file_contents_clob MEDIUMTEXT,
    file_contents_bytes BIGINT,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grpfile_unique_idx ON grouper_file (file_path(255));

ALTER TABLE grouper_composites
    ADD CONSTRAINT fk_composites_owner FOREIGN KEY (owner) REFERENCES grouper_groups (id);

ALTER TABLE grouper_composites
    ADD CONSTRAINT fk_composites_left_factor FOREIGN KEY (left_factor) REFERENCES grouper_groups (id);

ALTER TABLE grouper_composites
    ADD CONSTRAINT fk_composites_right_factor FOREIGN KEY (right_factor) REFERENCES grouper_groups (id);

ALTER TABLE grouper_composites
    ADD CONSTRAINT fk_composites_creator_id FOREIGN KEY (creator_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_groups
    ADD CONSTRAINT fk_groups_parent_stem FOREIGN KEY (parent_stem) REFERENCES grouper_stems (id);

ALTER TABLE grouper_groups
    ADD CONSTRAINT fk_groups_creator_id FOREIGN KEY (creator_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_groups
    ADD CONSTRAINT fk_groups_modifier_id FOREIGN KEY (modifier_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_memberships_member_id FOREIGN KEY (member_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_membership_field_id FOREIGN KEY (field_id) REFERENCES grouper_fields (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_memberships_creator_id FOREIGN KEY (creator_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_memberships_group_owner_id FOREIGN KEY (owner_group_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_memberships_stem_owner_id FOREIGN KEY (owner_stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_memberships_comp_via_id FOREIGN KEY (via_composite_id) REFERENCES grouper_composites (id);

ALTER TABLE grouper_memberships
    ADD CONSTRAINT fk_mship_attr_def_owner_id FOREIGN KEY (owner_attr_def_id) REFERENCES grouper_attribute_def (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_creator_id FOREIGN KEY (creator_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_field_id FOREIGN KEY (field_id) REFERENCES grouper_fields (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_via_group_id FOREIGN KEY (via_group_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_parent_id FOREIGN KEY (parent_id) REFERENCES grouper_group_set (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_owner_attr_def_id FOREIGN KEY (owner_attr_def_id) REFERENCES grouper_attribute_def (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_owner_group_id FOREIGN KEY (owner_group_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_member_group_id FOREIGN KEY (member_group_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_owner_stem_id FOREIGN KEY (owner_stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_member_stem_id FOREIGN KEY (member_stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_group_set
    ADD CONSTRAINT fk_group_set_member_field_id FOREIGN KEY (member_field_id) REFERENCES grouper_fields (id);

ALTER TABLE grouper_stems
    ADD CONSTRAINT fk_stems_parent_stem FOREIGN KEY (parent_stem) REFERENCES grouper_stems (id);

ALTER TABLE grouper_stems
    ADD CONSTRAINT fk_stems_creator_id FOREIGN KEY (creator_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_stems
    ADD CONSTRAINT fk_stems_modifier_id FOREIGN KEY (modifier_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_audit_entry
    ADD CONSTRAINT fk_audit_entry_type_id FOREIGN KEY (audit_type_id) REFERENCES grouper_audit_type (id);

ALTER TABLE grouper_change_log_entry
    ADD CONSTRAINT fk_change_log_entry_type_id FOREIGN KEY (change_log_type_id) REFERENCES grouper_change_log_type (id);

ALTER TABLE grouper_attribute_def
    ADD CONSTRAINT fk_attr_def_stem FOREIGN KEY (stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_attribute_def_name
    ADD CONSTRAINT fk_attr_def_name_stem FOREIGN KEY (stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_attribute_def_name
    ADD CONSTRAINT fk_attr_def_name_def_id FOREIGN KEY (attribute_def_id) REFERENCES grouper_attribute_def (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_action_id FOREIGN KEY (attribute_assign_action_id) REFERENCES grouper_attr_assign_action (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_def_name_id FOREIGN KEY (attribute_def_name_id) REFERENCES grouper_attribute_def_name (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_owner_assign_id FOREIGN KEY (owner_attribute_assign_id) REFERENCES grouper_attribute_assign (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_owner_def_id FOREIGN KEY (owner_attribute_def_id) REFERENCES grouper_attribute_def (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_owner_group_id FOREIGN KEY (owner_group_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_owner_member_id FOREIGN KEY (owner_member_id) REFERENCES grouper_members (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_owner_mship_id FOREIGN KEY (owner_membership_id) REFERENCES grouper_memberships (id);

ALTER TABLE grouper_attribute_assign
    ADD CONSTRAINT fk_attr_assign_owner_stem_id FOREIGN KEY (owner_stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_attribute_assign_value
    ADD CONSTRAINT fk_attr_assign_value_assign_id FOREIGN KEY (attribute_assign_id) REFERENCES grouper_attribute_assign (id);

ALTER TABLE grouper_attribute_def_scope
    ADD CONSTRAINT fk_attr_def_scope_def_id FOREIGN KEY (attribute_def_id) REFERENCES grouper_attribute_def (id);

ALTER TABLE grouper_attribute_def_name_set
    ADD CONSTRAINT fk_attr_def_name_set_parent FOREIGN KEY (parent_attr_def_name_set_id) REFERENCES grouper_attribute_def_name_set (id);

ALTER TABLE grouper_attribute_def_name_set
    ADD CONSTRAINT fk_attr_def_name_if FOREIGN KEY (if_has_attribute_def_name_id) REFERENCES grouper_attribute_def_name (id);

ALTER TABLE grouper_attribute_def_name_set
    ADD CONSTRAINT fk_attr_def_name_then FOREIGN KEY (then_has_attribute_def_name_id) REFERENCES grouper_attribute_def_name (id);

ALTER TABLE grouper_attr_assign_action
    ADD CONSTRAINT fk_attr_assn_attr_def_id FOREIGN KEY (attribute_def_id) REFERENCES grouper_attribute_def (id);

ALTER TABLE grouper_attr_assign_action_set
    ADD CONSTRAINT fk_attr_action_set_parent FOREIGN KEY (parent_attr_assn_action_id) REFERENCES grouper_attr_assign_action_set (id);

ALTER TABLE grouper_attr_assign_action_set
    ADD CONSTRAINT fk_attr_action_set_if FOREIGN KEY (if_has_attr_assn_action_id) REFERENCES grouper_attr_assign_action (id);

ALTER TABLE grouper_attr_assign_action_set
    ADD CONSTRAINT fk_attr_action_set_then FOREIGN KEY (then_has_attr_assn_action_id) REFERENCES grouper_attr_assign_action (id);

ALTER TABLE grouper_role_set
    ADD CONSTRAINT fk_role_set_parent FOREIGN KEY (parent_role_set_id) REFERENCES grouper_role_set (id);

ALTER TABLE grouper_role_set
    ADD CONSTRAINT fk_role_if FOREIGN KEY (if_has_role_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_role_set
    ADD CONSTRAINT fk_role_then FOREIGN KEY (then_has_role_id) REFERENCES grouper_groups (id);

ALTER TABLE grouper_pit_groups
    ADD CONSTRAINT fk_pit_group_stem FOREIGN KEY (stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_stems
    ADD CONSTRAINT fk_pit_stem_parent FOREIGN KEY (parent_stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_attribute_def
    ADD CONSTRAINT fk_pit_attr_def_stem FOREIGN KEY (stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_memberships
    ADD CONSTRAINT fk_pit_ms_owner_attrdef_id FOREIGN KEY (owner_attr_def_id) REFERENCES grouper_pit_attribute_def (id);

ALTER TABLE grouper_pit_memberships
    ADD CONSTRAINT fk_pit_ms_owner_group_id FOREIGN KEY (owner_group_id) REFERENCES grouper_pit_groups (id);

ALTER TABLE grouper_pit_memberships
    ADD CONSTRAINT fk_pit_ms_owner_stem_id FOREIGN KEY (owner_stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_memberships
    ADD CONSTRAINT fk_pit_ms_member_id FOREIGN KEY (member_id) REFERENCES grouper_pit_members (id);

ALTER TABLE grouper_pit_memberships
    ADD CONSTRAINT fk_pit_ms_field_id FOREIGN KEY (field_id) REFERENCES grouper_pit_fields (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_owner_attrdef_id FOREIGN KEY (owner_attr_def_id) REFERENCES grouper_pit_attribute_def (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_owner_group_id FOREIGN KEY (owner_group_id) REFERENCES grouper_pit_groups (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_owner_stem_id FOREIGN KEY (owner_stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_member_attrdef_id FOREIGN KEY (member_attr_def_id) REFERENCES grouper_pit_attribute_def (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_member_group_id FOREIGN KEY (member_group_id) REFERENCES grouper_pit_groups (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_member_stem_id FOREIGN KEY (member_stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_field_id FOREIGN KEY (field_id) REFERENCES grouper_pit_fields (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_member_field_id FOREIGN KEY (member_field_id) REFERENCES grouper_pit_fields (id);

ALTER TABLE grouper_pit_group_set
    ADD CONSTRAINT fk_pit_gs_parent_id FOREIGN KEY (parent_id) REFERENCES grouper_pit_group_set (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_action_id FOREIGN KEY (attribute_assign_action_id) REFERENCES grouper_pit_attr_assn_actn (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_def_name_id FOREIGN KEY (attribute_def_name_id) REFERENCES grouper_pit_attr_def_name (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_owner_assn_id FOREIGN KEY (owner_attribute_assign_id) REFERENCES grouper_pit_attribute_assign (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_owner_def_id FOREIGN KEY (owner_attribute_def_id) REFERENCES grouper_pit_attribute_def (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_owner_grp_id FOREIGN KEY (owner_group_id) REFERENCES grouper_pit_groups (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_owner_mem_id FOREIGN KEY (owner_member_id) REFERENCES grouper_pit_members (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_owner_ms_id FOREIGN KEY (owner_membership_id) REFERENCES grouper_pit_memberships (id);

ALTER TABLE grouper_pit_attribute_assign
    ADD CONSTRAINT fk_pit_attr_assn_owner_stem_id FOREIGN KEY (owner_stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_attr_assn_value
    ADD CONSTRAINT fk_pit_attr_assn_value_assn_id FOREIGN KEY (attribute_assign_id) REFERENCES grouper_pit_attribute_assign (id);

ALTER TABLE grouper_pit_attr_assn_actn
    ADD CONSTRAINT fk_pit_attr_assn_attr_def_id FOREIGN KEY (attribute_def_id) REFERENCES grouper_pit_attribute_def (id);

ALTER TABLE grouper_pit_attr_def_name
    ADD CONSTRAINT fk_pit_attr_def_name_stem FOREIGN KEY (stem_id) REFERENCES grouper_pit_stems (id);

ALTER TABLE grouper_pit_attr_def_name
    ADD CONSTRAINT fk_pit_attr_def_name_def_id FOREIGN KEY (attribute_def_id) REFERENCES grouper_pit_attribute_def (id);

ALTER TABLE grouper_pit_attr_def_name_set
    ADD CONSTRAINT fk_pit_attr_def_name_set_parnt FOREIGN KEY (parent_attr_def_name_set_id) REFERENCES grouper_pit_attr_def_name_set (id);

ALTER TABLE grouper_pit_attr_def_name_set
    ADD CONSTRAINT fk_pit_attr_def_name_if FOREIGN KEY (if_has_attribute_def_name_id) REFERENCES grouper_pit_attr_def_name (id);

ALTER TABLE grouper_pit_attr_def_name_set
    ADD CONSTRAINT fk_pit_attr_def_name_then FOREIGN KEY (then_has_attribute_def_name_id) REFERENCES grouper_pit_attr_def_name (id);

ALTER TABLE grouper_pit_attr_assn_actn_set
    ADD CONSTRAINT fk_pit_attr_action_set_parent FOREIGN KEY (parent_attr_assn_action_id) REFERENCES grouper_pit_attr_assn_actn_set (id);

ALTER TABLE grouper_pit_attr_assn_actn_set
    ADD CONSTRAINT fk_pit_attr_action_set_if FOREIGN KEY (if_has_attr_assn_action_id) REFERENCES grouper_pit_attr_assn_actn (id);

ALTER TABLE grouper_pit_attr_assn_actn_set
    ADD CONSTRAINT fk_pit_attr_action_set_then FOREIGN KEY (then_has_attr_assn_action_id) REFERENCES grouper_pit_attr_assn_actn (id);

ALTER TABLE grouper_pit_role_set
    ADD CONSTRAINT fk_pit_role_set_parent FOREIGN KEY (parent_role_set_id) REFERENCES grouper_pit_role_set (id);

ALTER TABLE grouper_pit_role_set
    ADD CONSTRAINT fk_pit_role_if FOREIGN KEY (if_has_role_id) REFERENCES grouper_pit_groups (id);

ALTER TABLE grouper_pit_role_set
    ADD CONSTRAINT fk_pit_role_then FOREIGN KEY (then_has_role_id) REFERENCES grouper_pit_groups (id);

ALTER TABLE grouper_ext_subj_attr
    ADD CONSTRAINT fk_ext_subj_attr_subj_uuid FOREIGN KEY (subject_uuid) REFERENCES grouper_ext_subj (uuid);

ALTER TABLE grouper_stem_set
    ADD CONSTRAINT fk_stem_set_parent FOREIGN KEY (parent_stem_set_id) REFERENCES grouper_stem_set (id);

ALTER TABLE grouper_stem_set
    ADD CONSTRAINT fk_stem_set_if FOREIGN KEY (if_has_stem_id) REFERENCES grouper_stems (id);

ALTER TABLE grouper_stem_set
    ADD CONSTRAINT fk_stem_set_then FOREIGN KEY (then_has_stem_id) REFERENCES grouper_stems (id);

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

CREATE INDEX group_alternate_name_idx ON grouper_groups (alternate_name(255));

CREATE INDEX member_name_idx ON grouper_members (name(255));

CREATE INDEX member_description_idx ON grouper_members (description(255));

CREATE INDEX audit_entry_string01_idx ON grouper_audit_entry (string01(255));

CREATE INDEX audit_entry_string02_idx ON grouper_audit_entry (string02(255));

CREATE INDEX audit_entry_string03_idx ON grouper_audit_entry (string03(255));

CREATE INDEX audit_entry_string04_idx ON grouper_audit_entry (string04(255));

CREATE INDEX audit_entry_string05_idx ON grouper_audit_entry (string05(255));

CREATE INDEX audit_entry_string06_idx ON grouper_audit_entry (string06(255));

CREATE INDEX audit_entry_string07_idx ON grouper_audit_entry (string07(255));

CREATE INDEX audit_entry_string08_idx ON grouper_audit_entry (string08(255));

CREATE INDEX change_log_temp_string01_idx ON grouper_change_log_entry_temp (string01(255));

CREATE INDEX change_log_temp_string02_idx ON grouper_change_log_entry_temp (string02(255));

CREATE INDEX change_log_temp_string03_idx ON grouper_change_log_entry_temp (string03(255));

CREATE INDEX change_log_temp_string04_idx ON grouper_change_log_entry_temp (string04(255));

CREATE INDEX change_log_temp_string05_idx ON grouper_change_log_entry_temp (string05(255));

CREATE INDEX change_log_temp_string06_idx ON grouper_change_log_entry_temp (string06(255));

CREATE INDEX change_log_temp_string07_idx ON grouper_change_log_entry_temp (string07(255));

CREATE INDEX change_log_temp_string08_idx ON grouper_change_log_entry_temp (string08(255));

CREATE INDEX change_log_temp_string09_idx ON grouper_change_log_entry_temp (string09(255));

CREATE INDEX change_log_temp_string10_idx ON grouper_change_log_entry_temp (string10(255));

CREATE INDEX change_log_temp_string11_idx ON grouper_change_log_entry_temp (string11(255));

CREATE INDEX change_log_temp_string12_idx ON grouper_change_log_entry_temp (string12(255));

CREATE INDEX change_log_entry_string01_idx ON grouper_change_log_entry (string01(255));

CREATE INDEX change_log_entry_string02_idx ON grouper_change_log_entry (string02(255));

CREATE INDEX change_log_entry_string03_idx ON grouper_change_log_entry (string03(255));

CREATE INDEX change_log_entry_string04_idx ON grouper_change_log_entry (string04(255));

CREATE INDEX change_log_entry_string05_idx ON grouper_change_log_entry (string05(255));

CREATE INDEX change_log_entry_string06_idx ON grouper_change_log_entry (string06(255));

CREATE INDEX change_log_entry_string07_idx ON grouper_change_log_entry (string07(255));

CREATE INDEX change_log_entry_string08_idx ON grouper_change_log_entry (string08(255));

CREATE INDEX change_log_entry_string09_idx ON grouper_change_log_entry (string09(255));

CREATE INDEX change_log_entry_string10_idx ON grouper_change_log_entry (string10(255));

CREATE INDEX change_log_entry_string11_idx ON grouper_change_log_entry (string11(255));

CREATE INDEX change_log_entry_string12_idx ON grouper_change_log_entry (string12(255));

CREATE unique INDEX attribute_def_name_idx ON grouper_attribute_def (name(255));

CREATE unique INDEX attribute_def_name_name_idx ON grouper_attribute_def_name (name(255));

CREATE INDEX attribute_val_string_idx ON grouper_attribute_assign_value (value_string(255));

CREATE INDEX pit_group_name_idx ON grouper_pit_groups (name(255));

CREATE INDEX pit_stem_name_idx ON grouper_pit_stems (name(255));

CREATE INDEX pit_attribute_def_name_idx ON grouper_pit_attribute_def (name(255));

CREATE INDEX pit_attr_val_string_idx ON grouper_pit_attr_assn_value (value_string(255));

CREATE INDEX pit_attr_def_name_name_idx ON grouper_pit_attr_def_name (name(255));

CREATE unique INDEX grouper_ext_subj_idfr_idx ON grouper_ext_subj (identifier(255));

CREATE INDEX grouper_extsubjattr_value_idx ON grouper_ext_subj_attr (attribute_value(255));

CREATE INDEX grouper_loader_job_name_idx ON grouper_loader_log (job_name(255), status, ended_time);

CREATE unique INDEX group_name_idx ON grouper_groups (name(255));

CREATE INDEX group_display_name_idx ON grouper_groups (display_name(255));

CREATE INDEX grpconfig_config_key_idx ON grouper_config (config_key(100), config_file_name(50));

CREATE unique INDEX grpconfig_unique_idx ON grouper_config (config_file_name(20), config_file_hierarchy(20), config_key(100), config_sequence);

CREATE VIEW grouper_groups_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, TYPE_OF_GROUP, GROUP_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.type_of_group, gg.id as group_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ;

CREATE VIEW grouper_roles_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, ROLE_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.id as role_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and type_of_group = 'role' ;

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

CREATE VIEW grouper_sync_membership_v (g_group_name, g_group_id_index, u_source_id, u_subject_id, u_subject_identifier, m_in_target, m_id, m_in_target_insert_or_exists, m_in_target_start, m_in_target_end, m_last_updated, m_membership_id, m_membership_id2, m_metadata_updated, m_error_message, m_error_timestamp, s_id, s_sync_engine, s_provisioner_name, u_id, u_member_id, u_in_target, u_in_target_insert_or_exists, u_in_target_start, u_in_target_end, u_provisionable, u_provisionable_start, u_provisionable_end, u_last_updated, u_last_user_sync_start, u_last_user_sync, u_last_user_meta_sync_start, u_last_user_metadata_sync, u_member_from_id2, u_member_from_id3, u_member_to_id2, u_member_to_id3, u_metadata_updated, u_last_time_work_was_done, u_error_message, u_error_timestamp, g_id, g_group_id, g_provisionable, g_in_target, g_in_target_insert_or_exists, g_in_target_start, g_in_target_end, g_provisionable_start, g_provisionable_end, g_last_updated, g_last_group_sync_start, g_last_group_sync, g_last_group_meta_sync_start, g_last_group_metadata_sync, g_group_from_id2, g_group_from_id3, g_group_to_id2, g_group_to_id3, g_metadata_updated, g_error_message, g_error_timestamp, g_last_time_work_was_done, m_error_code, u_error_code, g_error_code) AS select g.group_name as g_group_name, g.group_id_index as g_group_id_index, u.source_id as u_source_id, u.subject_id as u_subject_id, u.subject_identifier as u_subject_identifier, m.in_target as m_in_target, m.id as m_id, m.in_target_insert_or_exists as m_in_target_insert_or_exists, m.in_target_start as m_in_target_start, m.in_target_end as m_in_target_end, m.last_updated as m_last_updated, m.membership_id as m_membership_id, m.membership_id2 as m_membership_id2, m.metadata_updated as m_metadata_updated, m.error_message as m_error_message, m.error_timestamp as m_error_timestamp, s.id as s_id, s.sync_engine as s_sync_engine, s.provisioner_name as s_provisioner_name, u.id as u_id, u.member_id as u_member_id, u.in_target as u_in_target, u.in_target_insert_or_exists as u_in_target_insert_or_exists, u.in_target_start as u_in_target_start, u.in_target_end as u_in_target_end, u.provisionable as u_provisionable, u.provisionable_start as u_provisionable_start, u.provisionable_end as u_provisionable_end, u.last_updated as u_last_updated, u.last_user_sync_start as u_last_user_sync_start, u.last_user_sync as u_last_user_sync, u.last_user_metadata_sync_start as u_last_user_meta_sync_start, u.last_user_metadata_sync as u_last_user_metadata_sync, u.member_from_id2 as u_member_from_id2, u.member_from_id3 as u_member_from_id3, u.member_to_id2 as u_member_to_id2, u.member_to_id3 as u_member_to_id3, u.metadata_updated as u_metadata_updated, u.last_time_work_was_done as u_last_time_work_was_done, u.error_message as u_error_message, u.error_timestamp as u_error_timestamp, g.id as g_id, g.group_id as g_group_id, g.provisionable as g_provisionable, g.in_target as g_in_target, g.in_target_insert_or_exists as g_in_target_insert_or_exists, g.in_target_start as g_in_target_start, g.in_target_end as g_in_target_end, g.provisionable_start as g_provisionable_start, g.provisionable_end as g_provisionable_end, g.last_updated as g_last_updated, g.last_group_sync_start as g_last_group_sync_start, g.last_group_sync as g_last_group_sync, g.last_group_metadata_sync_start as g_last_group_meta_sync_start, g.last_group_metadata_sync as g_last_group_metadata_sync, g.group_from_id2 as g_group_from_id2, g.group_from_id3 as g_group_from_id3, g.group_to_id2 as g_group_to_id2, g.group_to_id3 as g_group_to_id3, g.metadata_updated as g_metadata_updated, g.error_message as g_error_message, g.error_timestamp as g_error_timestamp, g.last_time_work_was_done as g_last_time_work_was_done,  m.error_code as m_error_code, u.error_code as u_error_code, g.error_code as g_error_code from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id;

CREATE VIEW grouper_audit_entry_v (created_on, audit_category, action_name, logged_in_subject_id, act_as_subject_id, label_string01, string01, label_string02, string02, label_string03, string03, label_string04, string04, label_string05, string05, label_string06, string06, label_string07, string07, label_string08, string08, label_int01, int01, label_int02, int02, label_int03, int03, label_int04, int04, label_int05, int05, context_id, grouper_engine, description, logged_in_source_id, act_as_source_id, logged_in_member_id, act_as_member_id, audit_type_id, user_ip_address, server_host, audit_entry_last_updated, audit_entry_id, grouper_version, env_name) AS select gae.created_on, gat.audit_category, gat.action_name, (select gm.subject_id from grouper_members gm where gm.id = gae.logged_in_member_id) as logged_in_subject_id, (select gm.subject_id from grouper_members gm where gm.id = gae.act_as_member_id) as act_as_subject_id, gat.label_string01, gae.string01, gat.label_string02, gae.string02, gat.label_string03, gae.string03, gat.label_string04, gae.string04, gat.label_string05, gae.string05, gat.label_string06, gae.string06, gat.label_string07, gae.string07, gat.label_string08, gae.string08, gat.label_int01, gae.int01, gat.label_int02, gae.int02, gat.label_int03, gae.int03, gat.label_int04, gae.int04, gat.label_int05, gae.int05, gae.context_id, gae.grouper_engine, gae.description, (select gm.subject_source from grouper_members gm where gm.id = gae.logged_in_member_id) as logged_in_source_id, (select gm.subject_source from grouper_members gm where gm.id = gae.act_as_member_id) as act_as_source_id, gae.logged_in_member_id, gae.act_as_member_id, gat.id as audit_type_id, gae.user_ip_address, gae.server_host, gae.last_updated, gae.id as audit_entry_id, gae.grouper_version, gae.env_name from grouper_audit_type gat, grouper_audit_entry gae where gat.id = gae.audit_type_id ;

CREATE VIEW grouper_change_log_entry_v (created_on, change_log_category, action_name, sequence_number, label_string01, string01, label_string02, string02, label_string03, string03, label_string04, string04, label_string05, string05, label_string06, string06, label_string07, string07, label_string08, string08, label_string09, string09, label_string10, string10, label_string11, string11, label_string12, string12, context_id, change_log_type_id) AS SELECT gcle.created_on, gclt.change_log_category, gclt.action_name, gcle.sequence_number,        gclt.label_string01, gcle.string01, gclt.label_string02, gcle.string02,        gclt.label_string03, gcle.string03, gclt.label_string04, gcle.string04,        gclt.label_string05, gcle.string05, gclt.label_string06, gcle.string06,        gclt.label_string07, gcle.string07, gclt.label_string08, gcle.string08,        gclt.label_string09, gcle.string09, gclt.label_string10, gcle.string10,        gclt.label_string11, gcle.string11, gclt.label_string12, gcle.string12,        gcle.context_id, gcle.change_log_type_id   FROM grouper_change_log_type gclt, grouper_change_log_entry gcle  WHERE gclt.id = gcle.change_log_type_id;

CREATE VIEW grouper_composites_v (OWNER_GROUP_NAME, COMPOSITE_TYPE, LEFT_FACTOR_GROUP_NAME, RIGHT_FACTOR_GROUP_NAME, OWNER_GROUP_DISPLAYNAME, LEFT_FACTOR_GROUP_DISPLAYNAME, RIGHT_FACTOR_GROUP_DISPLAYNAME, owner_group_id, LEFT_FACTOR_GROUP_ID, RIGHT_FACTOR_GROUP_ID, COMPOSITE_ID, CREATE_TIME, CREATOR_ID, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  (select gg.name from grouper_groups gg  where gg.id = gc.owner) as owner_group_name,  gc.TYPE as composite_type,  (select gg.name from grouper_groups gg  where gg.id =  gc.left_factor) as left_factor_group_name,  (select gg.name from grouper_groups gg  where gg.id = gc.right_factor) as right_factor_group_name,  (select gg.display_name from grouper_groups gg  where gg.id = gc.owner) as owner_group_displayname,  (select gg.display_name from grouper_groups gg  where gg.id = gc.left_factor) as left_factor_group_displayname,  (select gg.display_name from grouper_groups gg  where gg.id = gc.right_factor) as right_factor_group_displayname,  gc.OWNER as owner_group_id,  gc.LEFT_FACTOR as left_factor_group_id,  gc.RIGHT_FACTOR as right_factor_group_id,  gc.ID as composite_id,  gc.CREATE_TIME,  gc.CREATOR_ID,  gc.HIBERNATE_VERSION_NUMBER, gc.context_id from grouper_composites gc  ;

CREATE VIEW grouper_ext_subj_v (uuid, name, identifier, description, institution, email, search_string_lower) AS SELECT ges.uuid, ges.name, ges.identifier, ges.description , ges.institution , ges.email , ges.search_string_lower  FROM grouper_ext_subj ges WHERE ges.enabled = 'T';

CREATE VIEW grouper_memberships_all_v (MEMBERSHIP_ID, IMMEDIATE_MEMBERSHIP_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, IMMEDIATE_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, VIA_GROUP_ID, VIA_COMPOSITE_ID, DEPTH, MSHIP_TYPE, IMMEDIATE_MSHIP_ENABLED, IMMEDIATE_MSHIP_ENABLED_TIME, IMMEDIATE_MSHIP_DISABLED_TIME, GROUP_SET_PARENT_ID, MEMBERSHIP_CREATOR_ID, MEMBERSHIP_CREATE_TIME, GROUP_SET_CREATOR_ID, GROUP_SET_CREATE_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select concat(ms.id, ':', gs.id) as membership_id, ms.id as immediate_membership_id, gs.id as group_set_id, ms.member_id, gs.field_id, ms.field_id, gs.owner_id, gs.owner_attr_def_id, gs.owner_group_id, gs.owner_stem_id, gs.via_group_id, ms.via_composite_id, gs.depth, gs.mship_type, ms.enabled, ms.enabled_timestamp, ms.disabled_timestamp, gs.parent_id as group_set_parent_id, ms.creator_id as membership_creator_id, ms.create_time as membership_create_time, gs.creator_id as group_set_creator_id, gs.create_time as group_set_create_time, ms.hibernate_version_number, ms.context_id from grouper_memberships ms, grouper_group_set gs where ms.owner_id = gs.member_id and ms.field_id = gs.member_field_id;

CREATE VIEW grouper_pit_memberships_all_v (ID, MEMBERSHIP_ID, MEMBERSHIP_SOURCE_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, MEMBERSHIP_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, GROUP_SET_ACTIVE, GROUP_SET_START_TIME, GROUP_SET_END_TIME, MEMBERSHIP_ACTIVE, MEMBERSHIP_START_TIME, MEMBERSHIP_END_TIME, DEPTH, GROUP_SET_PARENT_ID) AS select concat(ms.id, ':', gs.id) as membership_id, ms.id as immediate_membership_id, ms.source_id as membership_source_id, gs.id as group_set_id, ms.member_id, gs.field_id, ms.field_id, gs.owner_id, gs.owner_attr_def_id, gs.owner_group_id, gs.owner_stem_id, gs.active, gs.start_time, gs.end_time, ms.active, ms.start_time, ms.end_time, gs.depth, gs.parent_id as group_set_parent_id from grouper_pit_memberships ms, grouper_pit_group_set gs where ms.owner_id = gs.member_id and ms.field_id = gs.member_field_id;

CREATE VIEW grouper_memberships_lw_v (SUBJECT_ID, SUBJECT_SOURCE, GROUP_NAME, LIST_NAME, LIST_TYPE, GROUP_ID, MEMBER_ID) AS select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gg.name as group_name, gfl.NAME as list_name, gfl.TYPE as list_type, gg.ID as group_id, gm.ID as member_id  from grouper_memberships_all_v gms, grouper_members gm, grouper_groups gg, grouper_fields gfl where gms.OWNER_GROUP_ID = gg.id and gms.FIELD_ID = gfl.ID and gms.MEMBER_ID = gm.ID and gms.IMMEDIATE_MSHIP_ENABLED = 'T';

CREATE VIEW grouper_mship_stem_lw_v (SUBJECT_ID, SUBJECT_SOURCE, STEM_NAME, LIST_NAME, LIST_TYPE, STEM_ID) AS select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gs.name as stem_name, gfl.NAME as list_name, gfl.TYPE as list_type, gs.ID as stem_id from grouper_memberships_all_v gms, grouper_members gm, grouper_stems gs, grouper_fields gfl where gms.OWNER_STEM_ID = gs.id and gms.FIELD_ID = gfl.ID and gms.MEMBER_ID = gm.ID;

CREATE VIEW grouper_mship_attrdef_lw_v (SUBJECT_ID, SUBJECT_SOURCE, ATTRIBUTE_DEF_NAME, LIST_NAME, LIST_TYPE, ATTRIBUTE_DEF_ID) AS select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gad.name as attribute_def_name, gfl.NAME as list_name, gfl.TYPE as list_type, gad.id as attribute_def_id from grouper_memberships_all_v gms, grouper_members gm, grouper_attribute_def gad, grouper_fields gfl where gms.OWNER_ATTR_DEF_ID = gad.id and gms.FIELD_ID = gfl.ID and gms.MEMBER_ID = gm.ID;

CREATE VIEW grouper_memberships_v (GROUP_NAME, GROUP_DISPLAYNAME, STEM_NAME, STEM_DISPLAYNAME, SUBJECT_ID, SUBJECT_SOURCE, MEMBER_ID, LIST_TYPE, LIST_NAME, MEMBERSHIP_TYPE, COMPOSITE_PARENT_GROUP_NAME, DEPTH, CREATOR_SOURCE, CREATOR_SUBJECT_ID, MEMBERSHIP_ID, IMMEDIATE_MEMBERSHIP_ID, GROUP_SET_ID, STEM_ID, GROUP_ID, CREATE_TIME, CREATOR_ID, FIELD_ID, CONTEXT_ID) AS select  (select gg.name from grouper_groups gg  where gg.id = gms.owner_group_id) as group_name,  (select gg.display_name from grouper_groups gg  where gg.id = gms.owner_group_id) as group_displayname,  (select gs.NAME from grouper_stems gs  where gs.ID = gms.owner_stem_id) as stem_name,  (select gs.display_NAME from grouper_stems gs  where gs.ID = gms.owner_stem_id) as stem_displayname,  gm.subject_id, gm.subject_source, gms.member_id, gf.TYPE as list_type,  gf.NAME as list_name,  gms.MSHIP_TYPE as membership_type,  (select gg.name from grouper_groups gg, grouper_composites gc  where gg.id = gms.VIA_composite_ID and gg.id = gc.OWNER) as composite_parent_group_name,  depth,   (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gms.membership_creator_ID) as creator_source,  (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gms.membership_creator_ID) as creator_subject_id,  gms.membership_id as membership_id,   gms.immediate_membership_id as immediate_membership_id,   gms.GROUP_SET_ID as group_set_id,  (select gs.id from grouper_stems gs where gs.ID = gms.owner_stem_id) as stem_id,  (select gg.id from grouper_groups gg where gg.id = gms.owner_group_id) as group_id,  gms.membership_create_time,  gms.membership_creator_id,  gms.field_id, gms.context_id   from grouper_memberships_all_v gms, grouper_members gm, grouper_fields gf   where gms.MEMBER_ID = gm.ID and gms.field_id = gf.id  ;

CREATE VIEW grouper_stems_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, PARENT_STEM_DISPLAYNAME, CREATOR_SOURCE, CREATOR_SUBJECT_ID, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATE_TIME, CREATOR_ID, STEM_ID, MODIFIER_ID, MODIFY_TIME, PARENT_STEM, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select gs.extension, gs.NAME, gs.DISPLAY_EXTENSION, gs.DISPLAY_NAME, gs.DESCRIPTION, (select gs_parent.NAME from grouper_stems gs_parent where gs_parent.id = gs.PARENT_STEM) as parent_stem_name, (select gs_parent.DISPLAY_NAME from grouper_stems gs_parent where gs_parent.id = gs.PARENT_STEM) as parent_stem_displayname, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gs.creator_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gs.creator_ID) as creator_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gs.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gs.MODIFIER_ID) as modifier_subject_id, gs.CREATE_TIME, gs.CREATOR_ID,  gs.ID as stem_id, gs.MODIFIER_ID, gs.MODIFY_TIME, gs.PARENT_STEM, gs.HIBERNATE_VERSION_NUMBER, gs.context_id from grouper_stems gs ;

CREATE VIEW grouper_rpt_composites_v (COMPOSITE_TYPE, THE_COUNT) AS select gc.TYPE as composite_type, count(*) as the_count from grouper_composites gc group by gc.type ;

CREATE VIEW grouper_rpt_group_field_v (GROUP_NAME, GROUP_DISPLAYNAME, FIELD_TYPE, FIELD_NAME, MEMBER_COUNT) AS select gg.name as group_name, gg.display_name as group_displayName, gf.type as field_type, gf.name as field_name, count(distinct gms.member_id) as member_count from grouper_memberships_all_v gms, grouper_groups gg, grouper_fields gf where gms.FIELD_ID = gf.ID and gg.id = gms.OWNER_group_ID group by gg.name, gg.display_name, gf.type, gf.name ;

CREATE VIEW grouper_rpt_groups_v (GROUP_NAME, GROUP_DISPLAYNAME, TYPE_OF_GROUP, IMMEDIATE_MEMBERSHIP_COUNT, MEMBERSHIP_COUNT, ISA_COMPOSITE_FACTOR_COUNT, ISA_MEMBER_COUNT, GROUP_ID) AS select  gg.name as group_name, gg.display_name as group_displayname, gg.type_of_group, (select count(distinct gms.MEMBER_ID) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id and gms.MSHIP_TYPE = 'immediate') as immediate_membership_count, (select count(distinct gms.MEMBER_ID) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id) as membership_count, (select count(*) from grouper_composites gc where gc.LEFT_FACTOR = gg.id or gc.RIGHT_FACTOR = gg.id) as isa_composite_factor_count, (select count(distinct gms.OWNER_group_ID) from grouper_memberships_all_v gms, grouper_members gm where gm.SUBJECT_ID = gg.ID and gms.MEMBER_ID = gm.ID ) as isa_member_count, gg.ID as group_id from grouper_groups gg ;

CREATE VIEW grouper_rpt_roles_v (ROLE_NAME, ROLE_DISPLAYNAME, IMMEDIATE_MEMBERSHIP_COUNT, MEMBERSHIP_COUNT, ISA_COMPOSITE_FACTOR_COUNT, ISA_MEMBER_COUNT, ROLE_ID) AS select  gg.name as role_name, gg.display_name as role_displayname, (select count(distinct gms.member_id) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id and gms.mship_type = 'immediate') as immediate_membership_count, (select count(distinct gms.member_id) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id) as membership_count, (select count(*) from grouper_composites gc where gc.LEFT_FACTOR = gg.id or gc.RIGHT_FACTOR = gg.id) as isa_composite_factor_count, (select count(distinct gms.OWNER_group_ID) from grouper_memberships_all_v gms, grouper_members gm where gm.SUBJECT_ID = gg.ID and gms.MEMBER_ID = gm.ID ) as isa_member_count, gg.ID as role_id from grouper_groups gg  where gg.type_of_group = 'role' ;

CREATE VIEW grouper_rpt_members_v (SUBJECT_ID, SUBJECT_SOURCE, MEMBERSHIP_COUNT, MEMBER_ID) AS select gm.SUBJECT_ID, gm.SUBJECT_SOURCE, (select count(distinct gms.owner_group_id) from grouper_memberships gms where gms.MEMBER_ID = gm.ID) as membership_count, gm.ID as member_id from grouper_members gm ;

CREATE VIEW grouper_rpt_stems_v (STEM_NAME, STEM_DISPLAYNAME, GROUP_IMMEDIATE_COUNT, STEM_IMMEDIATE_COUNT, GROUP_COUNT, STEM_COUNT, THIS_STEM_MEMBERSHIP_COUNT, CHILD_GROUP_MEMBERSHIP_COUNT, GROUP_MEMBERSHIP_COUNT, STEM_ID) AS select gs.name as stem_name, gs.display_name as stem_displayname, (select count(*) from grouper_groups gg where gg.parent_stem = gs.ID) as group_immediate_count, (select count(*) from grouper_stems gs2 where gs.id = gs2.parent_stem ) as stem_immediate_count, (select count(*) from grouper_groups gg where gg.name like concat(gs.name,'%')) as group_count, (select count(*) from grouper_stems gs2 where gs2.name like concat(gs.name,'%')) as stem_count, (select count(distinct gm.member_id) from grouper_memberships_all_v gm where gm.owner_stem_id = gs.id) as this_stem_membership_count,  (select count(distinct gm.member_id) from grouper_memberships_all_v gm, grouper_groups gg where gg.parent_stem = gs.id and gm.owner_stem_id = gg.id) as child_group_membership_count,  (select count(distinct gm.member_id) from grouper_memberships_all_v gm, grouper_groups gg where gm.owner_group_id = gg.id and gg.name like concat(gs.name,'%')) as group_membership_count, gs.ID as stem_id from grouper_stems gs ;

CREATE VIEW grouper_role_set_v (if_has_role_name, then_has_role_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_role_id, then_has_role_id, parent_role_set_id) AS select ifHas.name as if_has_role_name, thenHas.name as then_has_role_name,  grs.depth,   grs.type, grParentIfHas.name as parent_if_has_name, grParentThenHas.name as parent_then_has_name,   grs.id, ifHas.id as if_has_role_id, thenHas.id as then_has_role_id,   grs.parent_role_set_id  from grouper_role_set grs,   grouper_role_set grsParent,   grouper_groups grParentIfHas,   grouper_groups grParentThenHas,   grouper_groups ifHas, grouper_groups thenHas   where  thenHas.id = grs.then_has_role_id   and ifHas.id = grs.if_has_role_id   and grs.parent_role_set_id = grsParent.id   and grParentIfHas.id = grsParent.if_has_role_id   and grParentThenHas.id = grsParent.then_has_role_id   ;

CREATE VIEW grouper_attr_def_name_set_v (if_has_attr_def_name_name, then_has_attr_def_name_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_attr_def_name_id, then_has_attr_def_name_id, parent_attr_def_name_set_id) AS select ifHas.name as if_has_attr_def_name_name, thenHas.name as then_has_attr_def_name_name,  gadns.depth,  gadns.type, gadnParentIfHas.name as parent_if_has_name, gadnParentThenHas.name as parent_then_has_name,  gadns.id,  ifHas.id as if_has_attr_def_name_id, thenHas.id as then_has_attr_def_name_id,  gadns.parent_attr_def_name_set_id from grouper_attribute_def_name_set gadns,  grouper_attribute_def_name_set gadnsParent,  grouper_attribute_def_name gadnParentIfHas,  grouper_attribute_def_name gadnParentThenHas,  grouper_attribute_def_name ifHas, grouper_attribute_def_name thenHas  where  thenHas.id = gadns.then_has_attribute_def_name_id  and ifHas.id = gadns.if_has_attribute_def_name_id  and gadns.parent_attr_def_name_set_id = gadnsParent.id  and gadnParentIfHas.id = gadnsParent.if_has_attribute_def_name_id  and gadnParentThenHas.id = gadnsParent.then_has_attribute_def_name_id  ;

CREATE VIEW grouper_attr_assn_action_set_v (if_has_attr_assn_action_name, then_has_attr_assn_action_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_attr_assn_action_id, then_has_attr_assn_action_id, parent_attr_assn_action_id) AS select ifHas.name as if_has_attr_assn_action_name , thenHas.name as then_has_attr_assn_action_name,   gaaas.depth,   gaaas.type, gaaaParentIfHas.name as parent_if_has_name, gaaaParentThenHas.name as parent_then_has_name,   gaaas.id,   ifHas.id as if_has_attr_assn_action_id, thenHas.id as then_has_attr_assn_action_id,   gaaas.parent_attr_assn_action_id  from grouper_attr_assign_action_set gaaas,   grouper_attr_assign_action_set gaaasParent,   grouper_attr_assign_action gaaaParentIfHas,   grouper_attr_assign_action gaaaParentThenHas,   grouper_attr_assign_action ifHas, grouper_attr_assign_action thenHas   where  thenHas.id = gaaas.then_has_attr_assn_action_id   and ifHas.id = gaaas.if_has_attr_assn_action_id   and gaaas.parent_attr_assn_action_id = gaaasParent.id   and gaaaParentIfHas.id = gaaasParent.if_has_attr_assn_action_id   and gaaaParentThenHas.id = gaaasParent.then_has_attr_assn_action_id   ;

CREATE VIEW grouper_attr_asn_group_v (group_name, action, attribute_def_name_name, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gg.name as group_name, gaaa.name as action, gadn.name as attribute_def_name_name, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa  where gaa.owner_group_id = gg.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_member_id is null and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_attr_asn_efmship_v (group_name, subject_source_id, subject_id, action, attribute_def_name_name, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, list_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, member_id, action_id) AS select distinct gg.name as group_name, gm.subject_source as subject_source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gf.name as list_name, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gm.id as member_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_attr_assign_action gaaa  where gaa.owner_group_id = gmav.owner_group_id and gaa.owner_member_id = gmav.member_id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gmav.immediate_mship_enabled = 'T' and gmav.owner_group_id = gg.id and gmav.field_id = gf.id and gf.type = 'list' and gmav.member_id = gm.id and gaa.owner_member_id is not null and gaa.owner_group_id is not null and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_attr_asn_stem_v (stem_name, action, attribute_def_name_name, stem_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, enabled, enabled_time, disabled_time, stem_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gs.name as stem_name, gaaa.name as action, gadn.name as attribute_def_name_name, gs.display_name as stem_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gs.id as stem_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_stems gs, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa  where gaa.owner_stem_id = gs.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_attr_asn_member_v (source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_members gm, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa where gaa.owner_member_id = gm.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_group_id is null  and gaa.attribute_assign_action_id = gaaa.id;

CREATE VIEW grouper_attr_asn_mship_v (group_name, source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, list_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, membership_id, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name, gadn.display_name as attribute_def_name_disp_name, gf.name as list_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa  where gaa.owner_membership_id = gms.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id  and gf.type = 'list' and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_attr_asn_attrdef_v (name_of_attr_def_assigned_to, action, attribute_def_name_name, attribute_def_name_disp_name, name_of_attribute_def_assigned, attribute_assign_notes, enabled, enabled_time, disabled_time, id_of_attr_def_assigned_to, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gad_assigned_to.name as name_of_attr_def_assigned_to, gaaa.name as action, gadn.name as attribute_def_name_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gad_assigned_to.id as id_of_attr_def_assigned_to, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_attribute_def gad_assigned_to, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa  where gaa.owner_attribute_def_id = gad_assigned_to.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_attr_asn_asn_group_v (group_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, group_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gg.name as group_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gg.display_name as group_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2   where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gg.id = gaa1.owner_group_id and gaa1.owner_member_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id;

CREATE VIEW grouper_attr_asn_asn_efmship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select distinct gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.owner_member_id = gmav.member_id and gaa1.owner_group_id = gmav.owner_group_id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gmav.immediate_mship_enabled = 'T' and gmav.field_id = gf.id and gmav.member_id = gm.id and gmav.owner_group_id = gg.id and gf.type = 'list' and gaa1.owner_member_id is not null  and gaa1.owner_group_id is not null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_attr_asn_asn_stem_v (stem_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, stem_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, stem_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gs.name as stem_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gs.display_name as stem_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gs.id as stem_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_stems gs, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gs.id = gaa1.owner_stem_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_attr_asn_asn_member_v (source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_members gm, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gm.id = gaa1.owner_member_id and gaa1.owner_group_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_attr_asn_asn_mship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, membership_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.owner_membership_id = gms.id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T'  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id and gf.type = 'list' and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_attr_asn_asn_attrdef_v (name_of_attr_def_assigned_to, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, id_of_attr_def_assigned_to, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gad.name as name_of_attr_def_assigned_to, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gad.id as id_of_attr_def_assigned_to, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_attribute_def gad, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2 , grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gad.id = gaa1.owner_attribute_def_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_aval_asn_group_v (group_name, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gg.name as group_name, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav   where gaav.attribute_assign_id = gaa.id  and gaa.owner_group_id = gg.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_member_id is null and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_aval_asn_efmship_v (group_name, subject_source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, list_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, member_id, action_id, attribute_assign_value_id) AS select distinct gg.name as group_name, gm.subject_source as subject_source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gf.name as list_name, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gm.id as member_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_group_id = gmav.owner_group_id and gaa.owner_member_id = gmav.member_id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gmav.immediate_mship_enabled = 'T' and gmav.owner_group_id = gg.id and gmav.field_id = gf.id and gf.type = 'list' and gmav.member_id = gm.id and gaa.owner_member_id is not null and gaa.owner_group_id is not null and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_aval_asn_stem_v (stem_name, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, stem_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, enabled, enabled_time, disabled_time, stem_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gs.name as stem_name, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gs.display_name as stem_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gs.id as stem_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_stems gs, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_stem_id = gs.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_aval_asn_member_v (source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_members gm, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa.id  and gaa.owner_member_id = gm.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_group_id is null  and gaa.attribute_assign_action_id = gaaa.id;

CREATE VIEW grouper_aval_asn_mship_v (group_name, source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name, list_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, membership_id, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn.display_name as attribute_def_name_disp_name, gf.name as list_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_membership_id = gms.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id  and gf.type = 'list' and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_aval_asn_attrdef_v (name_of_attr_def_assigned_to, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name, name_of_attribute_def_assigned, attribute_assign_notes, enabled, enabled_time, disabled_time, id_of_attr_def_assigned_to, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gad_assigned_to.name as name_of_attr_def_assigned_to, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gad_assigned_to.id as id_of_attr_def_assigned_to, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_attribute_def gad_assigned_to, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_attribute_def_id = gad_assigned_to.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

CREATE VIEW grouper_aval_asn_asn_group_v (group_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gg.name as group_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav   where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gg.id = gaa1.owner_group_id and gaa1.owner_member_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id;

CREATE VIEW grouper_aval_asn_asn_efmship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select distinct gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.owner_member_id = gmav.member_id and gaa1.owner_group_id = gmav.owner_group_id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gmav.immediate_mship_enabled = 'T' and gmav.field_id = gf.id and gmav.member_id = gm.id and gmav.owner_group_id = gg.id and gf.type = 'list' and gaa1.owner_member_id is not null  and gaa1.owner_group_id is not null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_aval_asn_asn_stem_v (stem_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, stem_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, stem_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gs.name as stem_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gs.display_name as stem_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gs.id as stem_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_stems gs, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gs.id = gaa1.owner_stem_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_aval_asn_asn_member_v (source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_members gm, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gm.id = gaa1.owner_member_id and gaa1.owner_group_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_aval_asn_asn_mship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, membership_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.owner_membership_id = gms.id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T'  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id and gf.type = 'list' and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_aval_asn_asn_attrdef_v (name_of_attr_def_assigned_to, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, id_of_attr_def_assigned_to, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gad.name as name_of_attr_def_assigned_to, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gad.id as id_of_attr_def_assigned_to, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_attribute_def gad, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gad.id = gaa1.owner_attribute_def_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

CREATE VIEW grouper_attr_def_priv_v (subject_id, subject_source_id, field_name, attribute_def_name, attribute_def_description, attribute_def_type, attribute_def_stem_id, attribute_def_id, member_id, field_id, immediate_membership_id, membership_id) AS select distinct gm.subject_id, gm.subject_source as subject_source_id,  gf.name as field_name, gad.name as attribute_def_name, gad.description as attribute_def_description,  gad.attribute_def_type, gad.stem_id as attribute_def_stem_id, gad.id as attribute_def_id,  gm.id as member_id, gmav.field_id, gmav.immediate_membership_id, gmav.membership_id  from grouper_memberships_all_v gmav, grouper_attribute_def gad, grouper_fields gf, grouper_members gm where gmav.owner_attr_def_id = gad.id and gmav.field_id = gf.id and gmav.immediate_mship_enabled = 'T' and gmav.member_id = gm.id ;

CREATE VIEW grouper_perms_assigned_role_v (role_name, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, attribute_def_name_id, action_id, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, attribute_assign_id, assignment_notes, disallowed, permission_type) AS SELECT distinct gr.name AS role_name,      gaaa.name AS action,     gadn.name AS attribute_def_name_name,     gadn.display_name AS attribute_def_name_disp_name,     gr.display_name AS role_display_name,     gaa.attribute_assign_delegatable,      gaa.enabled,     gaa.enabled_time,      gaa.disabled_time,      gr.ID AS role_id,     gadn.attribute_def_id,     gadn.ID AS attribute_def_name_id,      gaaa.ID AS action_id,     grs.DEPTH AS role_set_depth,     gadns.DEPTH AS attr_def_name_set_depth,     gaaas.DEPTH AS attr_assign_action_set_depth,     gaa.ID AS attribute_assign_id,     gaa.notes AS assignment_notes,     gaa.disallowed,     'role' AS permission_type FROM grouper_groups gr,     grouper_role_set grs,     grouper_attribute_def gad,     grouper_attribute_assign gaa,     grouper_attribute_def_name gadn,     grouper_attribute_def_name_set gadns,     grouper_attr_assign_action gaaa,     grouper_attr_assign_action_set gaaas WHERE grs.if_has_role_id = gr.id and gr.type_of_group = 'role'  AND gadn.attribute_def_id = gad.id AND gad.attribute_def_type = 'perm' AND gaa.owner_group_id = grs.then_has_role_id AND gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id AND gadn.id = gadns.then_has_attribute_def_name_id AND gaa.attribute_assign_type = 'group' AND gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id AND gaaa.id = gaaas.then_has_attr_assn_action_id ;

CREATE VIEW grouper_perms_role_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS select distinct gr.name as role_name,  gm.subject_source as subject_source_id,  gm.subject_id,  gaaa.name as action, gadn.name as attribute_def_name_name,  gadn.display_name as attribute_def_name_disp_name,  gr.display_name as role_display_name,  gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gr.id as role_id,  gadn.attribute_def_id,  gm.id as member_id,  gadn.id as attribute_def_name_id,  gaaa.id as action_id, gmav.depth AS membership_depth, grs.depth AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gaa.id AS attribute_assign_id, 'role' as permission_type, gaa.notes as assignment_notes, gmav.immediate_mship_enabled_time, gmav.immediate_mship_disabled_time, gaa.disallowed from grouper_groups gr,  grouper_memberships_all_v gmav,  grouper_members gm,  grouper_fields gf,  grouper_role_set grs,  grouper_attribute_def gad,  grouper_attribute_assign gaa,  grouper_attribute_def_name gadn,  grouper_attribute_def_name_set gadns, grouper_attr_assign_action gaaa, grouper_attr_assign_action_set gaaas where gmav.owner_group_id = gr.id  and gmav.field_id = gf.id  and gr.type_of_group = 'role' and gf.type = 'list'  and gf.name = 'members'  and gmav.immediate_mship_enabled = 'T'  and gmav.member_id = gm.id  and grs.if_has_role_id = gr.id  and gadn.attribute_def_id = gad.id  and gad.attribute_def_type = 'perm'  and gaa.owner_group_id = grs.then_has_role_id  and gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id  and gadn.id = gadns.then_has_attribute_def_name_id  and gaa.attribute_assign_type = 'group' and gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id and gaaa.id = gaaas.then_has_attr_assn_action_id ;

CREATE VIEW grouper_perms_role_subject_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS SELECT DISTINCT gr.name AS role_name,   gm.subject_source AS subject_source_id,   gm.subject_id,   gaaa.name AS ACTION,  gadn.name AS attribute_def_name_name,   gadn.display_name AS attribute_def_name_disp_name,   gr.display_name AS role_display_name,   gaa.attribute_assign_delegatable,  gaa.enabled,  gaa.enabled_time,  gaa.disabled_time,  gr.id AS role_id,   gadn.attribute_def_id,   gm.id AS member_id,   gadn.id AS attribute_def_name_id,   gaaa.id AS action_id, gmav.depth AS membership_depth, -1 AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gaa.id as attribute_assign_id, 'role_subject' as permission_type, gaa.notes as assignment_notes, gmav.immediate_mship_enabled_time, gmav.immediate_mship_disabled_time, gaa.disallowed FROM grouper_groups gr,   grouper_memberships_all_v gmav,   grouper_members gm,   grouper_fields gf,   grouper_attribute_def gad,  grouper_attribute_assign gaa,   grouper_attribute_def_name gadn,   grouper_attribute_def_name_set gadns,   grouper_attr_assign_action gaaa,  grouper_attr_assign_action_set gaaas  WHERE gmav.owner_group_id = gr.id  and gr.type_of_group = 'role' and gmav.field_id = gf.id  and gmav.owner_group_id = gaa.owner_group_id  AND gmav.member_id = gaa.owner_member_id   AND gf.type = 'list'   AND gf.name = 'members'   AND gmav.immediate_mship_enabled = 'T'   AND gmav.member_id = gm.id   AND gadn.attribute_def_id = gad.id  AND gad.attribute_def_type = 'perm'  AND gaa.attribute_assign_type = 'any_mem'  AND gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id   AND gadn.id = gadns.then_has_attribute_def_name_id  AND gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id  AND gaaa.id = gaaas.then_has_attr_assn_action_id  ;

CREATE VIEW grouper_perms_all_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  attribute_def_name_disp_name,  role_display_name,  attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed from grouper_perms_role_v  union  select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  attribute_def_name_disp_name,  role_display_name,  attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed from grouper_perms_role_subject_v  ;

CREATE VIEW grouper_pit_perms_role_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS select distinct gr.name as role_name,  gm.subject_source as subject_source_id,  gm.subject_id,  gaaa.name as action, gadn.name as attribute_def_name_name,  gr.id as role_id,  gadn.attribute_def_id,  gm.id as member_id,  gadn.id as attribute_def_name_id,  gaaa.id as action_id, gmav.depth AS membership_depth, grs.depth AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gmav.group_set_id as group_set_id, grs.id as role_set_id, gadns.id as attribute_def_name_set_id, gaaas.id as action_set_id, gaa.id AS attribute_assign_id, 'role' as permission_type, gmav.group_set_active, gmav.group_set_start_time, gmav.group_set_end_time, gmav.membership_active, gmav.membership_start_time, gmav.membership_end_time, grs.active as role_set_active, grs.start_time as role_set_start_time, grs.end_time as role_set_end_time, gaaas.active as action_set_active, gaaas.start_time as action_set_start_time, gaaas.end_time as action_set_end_time, gadns.active as attr_def_name_set_active, gadns.start_time as attr_def_name_set_start_time, gadns.end_time as attr_def_name_set_end_time, gaa.active as attribute_assign_active, gaa.start_time as attribute_assign_start_time, gaa.end_time as attribute_assign_end_time, gaa.disallowed,gaaa.source_id as action_source_id, gr.source_id as role_source_id, gadn.source_id as attribute_def_name_source_id, gad.source_id as attribute_def_source_id, gm.source_id as member_source_id, gmav.membership_source_id as membership_source_id, gaa.source_id as attribute_assign_source_id from grouper_pit_groups gr,  grouper_pit_memberships_all_v gmav,  grouper_pit_members gm,  grouper_pit_fields gf,  grouper_pit_role_set grs,  grouper_pit_attribute_def gad,  grouper_pit_attribute_assign gaa,  grouper_pit_attr_def_name gadn,  grouper_pit_attr_def_name_set gadns, grouper_pit_attr_assn_actn gaaa, grouper_pit_attr_assn_actn_set gaaas where gmav.owner_group_id = gr.id  and gmav.field_id = gf.id  and gf.type = 'list'  and gf.name = 'members'  and gmav.member_id = gm.id  and grs.if_has_role_id = gr.id  and gadn.attribute_def_id = gad.id  and gad.attribute_def_type = 'perm'  and gaa.owner_group_id = grs.then_has_role_id  and gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id  and gadn.id = gadns.then_has_attribute_def_name_id  and gaa.attribute_assign_type = 'group' and gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id and gaaa.id = gaaas.then_has_attr_assn_action_id ;

CREATE VIEW grouper_pit_perms_role_subj_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS SELECT DISTINCT gr.name AS role_name,   gm.subject_source AS subject_source_id,   gm.subject_id,   gaaa.name AS ACTION,  gadn.name AS attribute_def_name_name,   gr.id AS role_id,   gadn.attribute_def_id,   gm.id AS member_id,   gadn.id AS attribute_def_name_id,   gaaa.id AS action_id, gmav.depth AS membership_depth, -1 AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gmav.group_set_id as group_set_id, grs.id as role_set_id, gadns.id as attribute_def_name_set_id, gaaas.id as action_set_id, gaa.id as attribute_assign_id, 'role_subject' as permission_type, gmav.group_set_active, gmav.group_set_start_time, gmav.group_set_end_time, gmav.membership_active, gmav.membership_start_time, gmav.membership_end_time, grs.active as role_set_active, grs.start_time as role_set_start_time, grs.end_time as role_set_end_time, gaaas.active as action_set_active, gaaas.start_time as action_set_start_time, gaaas.end_time as action_set_end_time, gadns.active as attr_def_name_set_active, gadns.start_time as attr_def_name_set_start_time, gadns.end_time as attr_def_name_set_end_time, gaa.active as attribute_assign_active, gaa.start_time as attribute_assign_start_time, gaa.end_time as attribute_assign_end_time, gaa.disallowed, gaaa.source_id as action_source_id, gr.source_id as role_source_id, gadn.source_id as attribute_def_name_source_id, gad.source_id as attribute_def_source_id, gm.source_id as member_source_id, gmav.membership_source_id as membership_source_id, gaa.source_id as attribute_assign_source_id FROM grouper_pit_groups gr,   grouper_pit_memberships_all_v gmav,   grouper_pit_members gm,   grouper_pit_fields gf,   grouper_pit_role_set grs,  grouper_pit_attribute_def gad,  grouper_pit_attribute_assign gaa,   grouper_pit_attr_def_name gadn,   grouper_pit_attr_def_name_set gadns,   grouper_pit_attr_assn_actn gaaa,  grouper_pit_attr_assn_actn_set gaaas  WHERE gmav.owner_group_id = gr.id  and gmav.field_id = gf.id  and gmav.owner_group_id = gaa.owner_group_id  AND gmav.member_id = gaa.owner_member_id   AND gf.type = 'list'   AND gf.name = 'members'   AND gmav.member_id = gm.id   AND gadn.attribute_def_id = gad.id  AND gad.attribute_def_type = 'perm'  AND gaa.attribute_assign_type = 'any_mem'  AND gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id   AND gadn.id = gadns.then_has_attribute_def_name_id  AND gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id  AND gaaa.id = gaaas.then_has_attr_assn_action_id  AND grs.if_has_role_id = gr.id and grs.depth='0'  ;

CREATE VIEW grouper_pit_perms_all_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id from grouper_pit_perms_role_v  union  select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id from grouper_pit_perms_role_subj_v  ;

CREATE VIEW grouper_pit_attr_asn_value_v (attribute_assign_value_id, attribute_assign_id, attribute_def_name_id, attribute_assign_action_id, attribute_assign_type, owner_attribute_assign_id, owner_attribute_def_id, owner_group_id, owner_member_id, owner_membership_id, owner_stem_id, value_integer, value_floating, value_string, value_member_id, active, start_time, end_time) AS select gpaav.id as attribute_assign_value_id,  gpaa.id as attribute_assign_id,  gpaa.attribute_def_name_id,  gpaa.attribute_assign_action_id,  gpaa.attribute_assign_type,  gpaa.owner_attribute_assign_id,  gpaa.owner_attribute_def_id,  gpaa.owner_group_id,  gpaa.owner_member_id,  gpaa.owner_membership_id, gpaa.owner_stem_id, gpaav.value_integer, gpaav.value_floating, gpaav.value_string, gpaav.value_member_id, gpaav.active, gpaav.start_time, gpaav.end_time from grouper_pit_attribute_assign gpaa, grouper_pit_attr_assn_value gpaav where gpaa.id = gpaav.attribute_assign_id;

CREATE VIEW grouper_stem_set_v (if_has_stem_name, then_has_stem_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_stem_id, then_has_stem_id, parent_stem_set_id) AS select ifHas.name as if_has_stem_name , thenHas.name as then_has_stem_name,   gss.depth,   gss.type, gsParentIfHas.name as parent_if_has_name, gsParentThenHas.name as parent_then_has_name,   gss.id,   ifHas.id as if_has_stem_id, thenHas.id as then_has_stem_id,   gss.parent_stem_set_id  from grouper_stem_set gss,   grouper_stem_set gssParent,   grouper_stems gsParentIfHas,   grouper_stems gsParentThenHas,   grouper_stems ifHas, grouper_stems thenHas   where  thenHas.id = gss.then_has_stem_id   and ifHas.id = gss.if_has_stem_id   and gss.parent_stem_set_id = gssParent.id   and gsParentIfHas.id = gssParent.if_has_stem_id   and gsParentThenHas.id = gssParent.then_has_stem_id   ;

CREATE VIEW grouper_ext_subj_invite_v (invite_id, invite_member_id, invite_date, email_address, invite_email_when_registered, invite_group_uuids, invite_expire_date, email_body, expire_attr_expire_date, expire_attr_enabled, assignment_expire_date, assignment_enabled, attribute_assign_id) AS SELECT (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteUuid'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_id, (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteMemberId'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_member_id,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteDate'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_date,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectEmailAddress'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS email_address, (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteEmailWhenRegistered'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_email_when_registered,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteGroupUuids'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_group_uuids,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteExpireDate'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_expire_date,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteEmail'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS email_body,  (SELECT gaaasv.disabled_time2  FROM grouper_attr_asn_asn_stem_v gaaasv  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteExpireDate'  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS expire_attr_expire_date,  (SELECT gaaasv.enabled2  FROM grouper_attr_asn_asn_stem_v gaaasv  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteExpireDate'  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS expire_attr_enabled,  gaasv.disabled_time AS assignment_expire_date,  gaasv.enabled AS assignment_enabled,  gaasv.attribute_assign_id  FROM grouper_attr_asn_stem_v gaasv  WHERE gaasv.attribute_def_name_name = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInvite'  AND gaasv.enabled = 'T' ;

CREATE VIEW grouper_rules_v (assigned_to_type, assigned_to_group_name, assigned_to_stem_name, assigned_to_member_subject_id, assigned_to_attribute_def_name, rule_check_type, rule_check_owner_id, rule_check_owner_name, rule_check_stem_scope, rule_check_arg0, rule_check_arg1, rule_if_condition_el, rule_if_condition_enum, rule_if_condition_enum_arg0, rule_if_condition_enum_arg1, rule_if_owner_id, rule_if_owner_name, rule_if_stem_scope, rule_then_el, rule_then_enum, rule_then_enum_arg0, rule_then_enum_arg1, rule_then_enum_arg2, rule_valid, rule_run_daemon, rule_act_as_subject_id, rule_act_as_subject_identifier, rule_act_as_subject_source_id, assignment_enabled, attribute_assign_id) AS SELECT main_gaa.attribute_assign_type AS assigned_to_type,  (SELECT gg.name  FROM grouper_groups gg WHERE gg.id = main_gaa.owner_group_id  ) AS assigned_to_group_name,  (SELECT gs.name  FROM grouper_stems gs WHERE gs.id = main_gaa.owner_stem_id  ) AS assigned_to_stem_name,  (SELECT gm.subject_id  FROM grouper_members gm WHERE gm.id = main_gaa.owner_member_id  ) AS assigned_to_member_subject_id,  (SELECT gad.name  FROM grouper_attribute_def gad WHERE gad.id = main_gaa.owner_attribute_def_id  ) AS assigned_to_attribute_def_name,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckType'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_type,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckOwnerId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_owner_id,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckOwnerName'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_owner_name,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckStemScope'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_stem_scope,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckArg0'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_arg0,  (SELECT gaav.value_string  FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckArg1'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_arg1,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEl'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_el,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEnum'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_enum,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEnumArg0'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_enum_arg0,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEnumArg1'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_enum_arg1,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfOwnerId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_owner_id,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfOwnerName'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_owner_name,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfStemScope'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_stem_scope,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEl'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_el,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnum'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnumArg0'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum_arg0,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnumArg1'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum_arg1,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnumArg2'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum_arg2,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleValid'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_valid,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleRunDaemon'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_run_daemon,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleActAsSubjectId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_act_as_subject_id,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleActAsSubjectIdentifier'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_act_as_subject_identifier,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleActAsSubjectSourceId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_act_as_subject_source_id,   main_gaa.enabled AS assignment_enabled,  main_gaa.id AS attribute_assign_id  FROM grouper_attribute_assign main_gaa, grouper_attribute_def_name main_gadn  WHERE main_gadn.name = 'etc:attribute:rules:rule'  AND main_gaa.attribute_def_name_id = main_gadn.id ;

CREATE VIEW grouper_service_role_v (service_role, group_name, name_of_service_def_name, subject_source_id, subject_id, field_name, name_of_service_def, group_display_name, group_id, service_def_id, service_name_id, member_id, field_id, display_name_of_service_name, service_stem_id) AS select distinct (CASE gf.name WHEN 'admins' THEN 'admin' WHEN 'updaters' then 'admin' when 'members' then 'user' end ) as service_role, gg.name as group_name, gadn.name as name_of_service_def_name,  gm.subject_source as subject_source_id, gm.subject_id,  gf.name as field_name, gad.name as name_of_service_def,  gg.display_name as group_display_name, gg.id as group_id, gad.id as service_def_id, gadn.id as service_name_id, gm.id as member_id, gf.id as field_id, gadn.display_name as display_name_of_service_name, gaa.owner_stem_id as service_stem_id from grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_memberships_all_v gmav, grouper_attribute_assign gaa, grouper_stem_set gss, grouper_members gm, grouper_fields gf where gadn.attribute_def_id = gad.id and gad.attribute_def_type='service' and gaa.attribute_def_name_id = gadn.id and gad.assign_to_stem='T' and gmav.field_id = gf.id and gmav.immediate_mship_enabled='T' and gmav.owner_group_id = gg.id and gaa.owner_stem_id = gss.then_has_stem_id and gg.parent_stem=gss.if_has_stem_id and gaa.enabled='T' and gmav.member_id = gm.id and gf.name in ('admins', 'members', 'readers', 'updaters') ;

CREATE VIEW grouper_pit_memberships_lw_v (ID, MEMBERSHIP_ID, MEMBERSHIP_SOURCE_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, MEMBERSHIP_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, GROUP_SET_ACTIVE, GROUP_SET_START_TIME, GROUP_SET_END_TIME, MEMBERSHIP_ACTIVE, MEMBERSHIP_START_TIME, MEMBERSHIP_END_TIME, DEPTH, GROUP_SET_PARENT_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE) AS select concat(gpmship.id, ':', gpgs.id) as membership_id, gpmship.id as immediate_membership_id, gpmship.source_id as membership_source_id, gpgs.id as group_set_id, gpmship.member_id, gpgs.field_id, gpmship.field_id, gpgs.owner_id, gpgs.owner_attr_def_id, gpgs.owner_group_id, gpgs.owner_stem_id, gpgs.active, gpgs.start_time, gpgs.end_time, gpmship.active, gpmship.start_time, gpmship.end_time, gpgs.depth, gpgs.parent_id as group_set_parent_id,  (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F'  end) as the_active from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and ((gpmship.start_time >= gpgs.start_time and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)) or (gpgs.start_time >= gpmship.start_time and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)));

CREATE VIEW grouper_pit_mship_group_lw_v (GROUP_NAME, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, GROUP_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gpg.name as group_name, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gpg.source_id as group_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, concat(gpmship.source_id, ':', gpgs.source_id) as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_groups gpg, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gpg.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )    or    (       gpgs.start_time >= gpmship.start_time       and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) ) ;

CREATE VIEW grouper_pit_mship_stem_lw_v (STEM_NAME, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, STEM_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gps.name as stem_name, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gps.source_id as stem_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, concat(gpmship.source_id, ':', gpgs.source_id) as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_stems gps, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gps.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )   or   (      gpgs.start_time >= gpmship.start_time       and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) );

CREATE VIEW grouper_pit_mship_attr_lw_v (NAME_OF_ATTRIBUTE_DEF, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, ATTRIBUTE_DEF_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gpa.name as name_of_attribute_def, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gpa.source_id as attribute_def_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, concat(gpmship.source_id, ':', gpgs.source_id) as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_attribute_def gpa, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gpa.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )    or    (       gpgs.start_time >= gpmship.start_time      and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) ) ;

CREATE VIEW grouper_recent_mships_conf_v (group_name_from, group_uuid_from, recent_micros, group_uuid_to, group_name_to, include_eligible) AS select distinct   gg.name group_name_from,  gaaagv_groupUuidFrom.value_string group_uuid_from,  gaaagv_recentMembershipsMicros.value_integer recent_micros,  gaaagv_groupUuidFrom.group_id group_uuid_to,  gaaagv_groupUuidFrom.group_name group_name_to,   gaaagv_includeEligible.value_string include_eligible   from   grouper_aval_asn_asn_group_v gaaagv_recentMembershipsMicros,  grouper_aval_asn_asn_group_v gaaagv_groupUuidFrom,  grouper_aval_asn_asn_group_v gaaagv_includeEligible,  grouper_groups gg  where gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_groupUuidFrom.attribute_assign_id1  and gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_includeEligible.attribute_assign_id1  and gaaagv_recentMembershipsMicros.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsMicros'  and gaaagv_groupUuidFrom.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsGroupUuidFrom'  and gaaagv_includeEligible.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsIncludeCurrent'  and gaaagv_recentMembershipsMicros.value_integer > 0  and gaaagv_recentMembershipsMicros.value_integer is not null  and gaaagv_groupUuidFrom.value_string is not null  and gaaagv_includeEligible.value_string is not null  and (gaaagv_includeEligible.value_string = 'T' or gaaagv_includeEligible.value_string = 'F')  and gg.id = gaaagv_groupUuidFrom.value_string ;

CREATE VIEW grouper_recent_mships_load_v (group_name, subject_source_id, subject_id) AS select grmc.group_name_to as group_name, gpmglv.subject_source as subject_source_id, gpmglv.subject_id as subject_id from grouper_recent_mships_conf grmc,  grouper_pit_mship_group_lw_v gpmglv, grouper_time gt, grouper_members gm where gm.id = gpmglv.member_id and gm.subject_resolution_deleted = 'F' and gt.time_label = 'now' and (gpmglv.group_id = grmc.group_uuid_from or gpmglv.group_name = grmc.group_name_from) and gpmglv.subject_source != 'g:gsa' and gpmglv.field_name = 'members' and (gpmglv.the_end_time is null or gpmglv.the_end_time >= gt.utc_micros_since_1970 - grmc.recent_micros) and ( grmc.include_eligible = 'T' or not exists (select 1 from grouper_memberships mship2, grouper_group_set gs2 WHERE mship2.owner_id = gs2.member_id AND mship2.field_id = gs2.member_field_id and gs2.field_id = mship2.field_id and mship2.member_id = gm.id and gs2.field_id = gpmglv.field_id and gs2.owner_id = grmc.group_uuid_from and mship2.enabled = 'T'));

insert into grouper_ddl (id, object_name, db_version, last_updated, history) values 
('c08d3e076fdb4c41acdafe5992e5dc4d', 'Grouper', 36, date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), 
concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V0 to V36, '));
commit;
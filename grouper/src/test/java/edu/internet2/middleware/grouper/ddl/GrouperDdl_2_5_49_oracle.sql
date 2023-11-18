CREATE TABLE grouper_ddl
(
    id VARCHAR2(40) NOT NULL,
    object_name VARCHAR2(128),
    db_version INTEGER,
    last_updated VARCHAR2(50),
    history VARCHAR2(4000),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_ddl_object_name_idx ON grouper_ddl (object_name);

CREATE TABLE grouper_composites
(
    id VARCHAR2(40) NOT NULL,
    owner VARCHAR2(40) NOT NULL,
    left_factor VARCHAR2(40) NOT NULL,
    right_factor VARCHAR2(40) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    creator_id VARCHAR2(40) NOT NULL,
    create_time NUMBER(38) NOT NULL,
    hibernate_version_number NUMBER(38),
    context_id VARCHAR2(40),
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
    id VARCHAR2(40) NOT NULL,
    name VARCHAR2(32) NOT NULL,
    read_privilege VARCHAR2(32) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    write_privilege VARCHAR2(32) NOT NULL,
    hibernate_version_number NUMBER(38),
    context_id VARCHAR2(40),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX name_and_type ON grouper_fields (name, type);

CREATE INDEX fields_context_idx ON grouper_fields (context_id);

CREATE INDEX grouper_fields_type_idx ON grouper_fields (type);

CREATE TABLE grouper_groups
(
    id VARCHAR2(40) NOT NULL,
    parent_stem VARCHAR2(40) NOT NULL,
    creator_id VARCHAR2(40) NOT NULL,
    create_time NUMBER(38) NOT NULL,
    modifier_id VARCHAR2(40),
    modify_time NUMBER(38),
    last_membership_change NUMBER(38),
    last_imm_membership_change NUMBER(38),
    alternate_name VARCHAR2(1024),
    enabled VARCHAR2(1) DEFAULT 'T' NOT NULL,
    enabled_timestamp NUMBER(38),
    disabled_timestamp NUMBER(38),
    hibernate_version_number NUMBER(38),
    name VARCHAR2(1024),
    display_name VARCHAR2(1024),
    extension VARCHAR2(255),
    display_extension VARCHAR2(255),
    description VARCHAR2(1024),
    context_id VARCHAR2(40),
    type_of_group VARCHAR2(10) DEFAULT 'group' NOT NULL,
    id_index NUMBER(38) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX group_alternate_name_idx ON grouper_groups (alternate_name);

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

CREATE UNIQUE INDEX group_name_idx ON grouper_groups (name);

CREATE INDEX group_display_name_idx ON grouper_groups (display_name);

CREATE UNIQUE INDEX group_parent_idx ON grouper_groups (parent_stem, extension);

CREATE INDEX group_parent_display_idx ON grouper_groups (parent_stem, display_extension);

CREATE TABLE grouper_members
(
    id VARCHAR2(40) NOT NULL,
    subject_id VARCHAR2(255) NOT NULL,
    subject_source VARCHAR2(255) NOT NULL,
    subject_type VARCHAR2(255) NOT NULL,
    hibernate_version_number NUMBER(38),
    subject_identifier0 VARCHAR2(255),
    sort_string0 VARCHAR2(50),
    sort_string1 VARCHAR2(50),
    sort_string2 VARCHAR2(50),
    sort_string3 VARCHAR2(50),
    sort_string4 VARCHAR2(50),
    search_string0 VARCHAR2(2048),
    search_string1 VARCHAR2(2048),
    search_string2 VARCHAR2(2048),
    search_string3 VARCHAR2(2048),
    search_string4 VARCHAR2(2048),
    name VARCHAR2(2048),
    description VARCHAR2(2048),
    context_id VARCHAR2(40),
    subject_resolution_deleted VARCHAR2(1) DEFAULT 'F' NOT NULL,
    subject_resolution_resolvable VARCHAR2(1) DEFAULT 'T' NOT NULL,
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

CREATE INDEX member_name_idx ON grouper_members (name);

CREATE INDEX member_description_idx ON grouper_members (description);

CREATE INDEX member_context_idx ON grouper_members (context_id);

CREATE INDEX member_subjidentifier0_idx ON grouper_members (subject_identifier0);

CREATE INDEX member_resolvable_idx ON grouper_members (subject_resolution_resolvable);

CREATE INDEX member_deleted_idx ON grouper_members (subject_resolution_deleted);

CREATE TABLE grouper_memberships
(
    id VARCHAR2(40) NOT NULL,
    member_id VARCHAR2(40) NOT NULL,
    owner_id VARCHAR2(40) NOT NULL,
    field_id VARCHAR2(40) NOT NULL,
    owner_group_id VARCHAR2(40),
    owner_stem_id VARCHAR2(40),
    owner_attr_def_id VARCHAR2(40),
    via_composite_id VARCHAR2(40),
    enabled VARCHAR2(1) DEFAULT 'T' NOT NULL,
    enabled_timestamp NUMBER(38),
    disabled_timestamp NUMBER(38),
    mship_type VARCHAR2(32) NOT NULL,
    creator_id VARCHAR2(40),
    create_time NUMBER(38) NOT NULL,
    hibernate_version_number NUMBER(38),
    context_id VARCHAR2(40),
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

CREATE INDEX membership_gown_field_type_idx ON grouper_memberships (owner_group_id, field_id, mship_type);

CREATE INDEX membership_sown_field_type_idx ON grouper_memberships (owner_stem_id, field_id, mship_type);

CREATE INDEX membership_type_idx ON grouper_memberships (mship_type);

CREATE INDEX membership_context_idx ON grouper_memberships (context_id);

CREATE UNIQUE INDEX membership_uniq_idx ON grouper_memberships (owner_id, member_id, field_id);

CREATE INDEX groupmem_ownid_fieldid_idx ON grouper_memberships (owner_id, field_id);

CREATE TABLE grouper_group_set
(
    id VARCHAR2(40) NOT NULL,
    owner_attr_def_id VARCHAR2(40),
    owner_attr_def_id_null VARCHAR2(40) DEFAULT '<NULL>' NOT NULL,
    owner_group_id VARCHAR2(40),
    owner_group_id_null VARCHAR2(40) DEFAULT '<NULL>' NOT NULL,
    owner_stem_id VARCHAR2(40),
    owner_stem_id_null VARCHAR2(40) DEFAULT '<NULL>' NOT NULL,
    member_attr_def_id VARCHAR2(40),
    member_group_id VARCHAR2(40),
    member_stem_id VARCHAR2(40),
    member_id VARCHAR2(40) NOT NULL,
    field_id VARCHAR2(40) NOT NULL,
    member_field_id VARCHAR2(40) NOT NULL,
    owner_id VARCHAR2(40) NOT NULL,
    mship_type VARCHAR2(16) NOT NULL,
    depth INTEGER NOT NULL,
    via_group_id VARCHAR2(40),
    parent_id VARCHAR2(40),
    creator_id VARCHAR2(40) NOT NULL,
    create_time NUMBER(38) NOT NULL,
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    id VARCHAR2(40) NOT NULL,
    parent_stem VARCHAR2(40),
    name VARCHAR2(255) NOT NULL,
    display_name VARCHAR2(255) NOT NULL,
    creator_id VARCHAR2(40) NOT NULL,
    create_time NUMBER(38) NOT NULL,
    modifier_id VARCHAR2(40),
    modify_time NUMBER(38),
    display_extension VARCHAR2(255) NOT NULL,
    extension VARCHAR2(255) NOT NULL,
    description VARCHAR2(1024),
    last_membership_change NUMBER(38),
    alternate_name VARCHAR2(255),
    hibernate_version_number NUMBER(38),
    context_id VARCHAR2(40),
    id_index NUMBER(38) NOT NULL,
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
    action_name VARCHAR2(50),
    audit_category VARCHAR2(50),
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    label_int01 VARCHAR2(50),
    label_int02 VARCHAR2(50),
    label_int03 VARCHAR2(50),
    label_int04 VARCHAR2(50),
    label_int05 VARCHAR2(50),
    label_string01 VARCHAR2(50),
    label_string02 VARCHAR2(50),
    label_string03 VARCHAR2(50),
    label_string04 VARCHAR2(50),
    label_string05 VARCHAR2(50),
    label_string06 VARCHAR2(50),
    label_string07 VARCHAR2(50),
    label_string08 VARCHAR2(50),
    last_updated NUMBER(38),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX audit_type_category_type_idx ON grouper_audit_type (audit_category, action_name);

CREATE TABLE grouper_audit_entry
(
    act_as_member_id VARCHAR2(40),
    audit_type_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    description VARCHAR2(4000),
    env_name VARCHAR2(50),
    grouper_engine VARCHAR2(50),
    grouper_version VARCHAR2(20),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    int01 NUMBER(38),
    int02 NUMBER(38),
    int03 NUMBER(38),
    int04 NUMBER(38),
    int05 NUMBER(38),
    last_updated NUMBER(38),
    logged_in_member_id VARCHAR2(40),
    server_host VARCHAR2(50),
    string01 VARCHAR2(4000),
    string02 VARCHAR2(4000),
    string03 VARCHAR2(4000),
    string04 VARCHAR2(4000),
    string05 VARCHAR2(4000),
    string06 VARCHAR2(4000),
    string07 VARCHAR2(4000),
    string08 VARCHAR2(4000),
    user_ip_address VARCHAR2(50),
    duration_microseconds NUMBER(38),
    query_count INTEGER,
    server_user_name VARCHAR2(50),
    PRIMARY KEY (id)
);

CREATE INDEX audit_entry_act_as_idx ON grouper_audit_entry (act_as_member_id);

CREATE INDEX audit_entry_act_as_created_idx ON grouper_audit_entry (act_as_member_id, created_on);

CREATE INDEX audit_entry_type_idx ON grouper_audit_entry (audit_type_id);

CREATE INDEX audit_entry_context_idx ON grouper_audit_entry (context_id);

CREATE INDEX audit_entry_logged_in_idx ON grouper_audit_entry (logged_in_member_id);

CREATE INDEX audit_entry_string01_idx ON grouper_audit_entry (string01);

CREATE INDEX audit_entry_string02_idx ON grouper_audit_entry (string02);

CREATE INDEX audit_entry_string03_idx ON grouper_audit_entry (string03);

CREATE INDEX audit_entry_string04_idx ON grouper_audit_entry (string04);

CREATE INDEX audit_entry_string05_idx ON grouper_audit_entry (string05);

CREATE INDEX audit_entry_string06_idx ON grouper_audit_entry (string06);

CREATE INDEX audit_entry_string07_idx ON grouper_audit_entry (string07);

CREATE INDEX audit_entry_string08_idx ON grouper_audit_entry (string08);

CREATE TABLE grouper_change_log_type
(
    action_name VARCHAR2(50),
    change_log_category VARCHAR2(50),
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    label_string01 VARCHAR2(50),
    label_string02 VARCHAR2(50),
    label_string03 VARCHAR2(50),
    label_string04 VARCHAR2(50),
    label_string05 VARCHAR2(50),
    label_string06 VARCHAR2(50),
    label_string07 VARCHAR2(50),
    label_string08 VARCHAR2(50),
    label_string09 VARCHAR2(50),
    label_string10 VARCHAR2(50),
    label_string11 VARCHAR2(50),
    label_string12 VARCHAR2(50),
    last_updated NUMBER(38),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX change_log_type_cat_type_idx ON grouper_change_log_type (change_log_category, action_name);

CREATE TABLE grouper_change_log_consumer
(
    name VARCHAR2(100) NOT NULL,
    last_sequence_processed NUMBER(38),
    last_updated NUMBER(38),
    created_on NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX change_log_consumer_name_idx ON grouper_change_log_consumer (name);

CREATE TABLE grouper_change_log_entry_temp
(
    id VARCHAR2(40) NOT NULL,
    change_log_type_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38) NOT NULL,
    string01 VARCHAR2(4000),
    string02 VARCHAR2(4000),
    string03 VARCHAR2(4000),
    string04 VARCHAR2(4000),
    string05 VARCHAR2(4000),
    string06 VARCHAR2(4000),
    string07 VARCHAR2(4000),
    string08 VARCHAR2(4000),
    string09 VARCHAR2(4000),
    string10 VARCHAR2(4000),
    string11 VARCHAR2(4000),
    string12 VARCHAR2(4000),
    PRIMARY KEY (id)
);

CREATE INDEX change_log_temp_string01_idx ON grouper_change_log_entry_temp (string01);

CREATE INDEX change_log_temp_string02_idx ON grouper_change_log_entry_temp (string02);

CREATE INDEX change_log_temp_string03_idx ON grouper_change_log_entry_temp (string03);

CREATE INDEX change_log_temp_string04_idx ON grouper_change_log_entry_temp (string04);

CREATE INDEX change_log_temp_string05_idx ON grouper_change_log_entry_temp (string05);

CREATE INDEX change_log_temp_string06_idx ON grouper_change_log_entry_temp (string06);

CREATE INDEX change_log_temp_string07_idx ON grouper_change_log_entry_temp (string07);

CREATE INDEX change_log_temp_string08_idx ON grouper_change_log_entry_temp (string08);

CREATE INDEX change_log_temp_string09_idx ON grouper_change_log_entry_temp (string09);

CREATE INDEX change_log_temp_string10_idx ON grouper_change_log_entry_temp (string10);

CREATE INDEX change_log_temp_string11_idx ON grouper_change_log_entry_temp (string11);

CREATE INDEX change_log_temp_string12_idx ON grouper_change_log_entry_temp (string12);

CREATE INDEX change_log_temp_created_on_idx ON grouper_change_log_entry_temp (created_on);

CREATE TABLE grouper_change_log_entry
(
    change_log_type_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    sequence_number NUMBER(38),
    string01 VARCHAR2(4000),
    string02 VARCHAR2(4000),
    string03 VARCHAR2(4000),
    string04 VARCHAR2(4000),
    string05 VARCHAR2(4000),
    string06 VARCHAR2(4000),
    string07 VARCHAR2(4000),
    string08 VARCHAR2(4000),
    string09 VARCHAR2(4000),
    string10 VARCHAR2(4000),
    string11 VARCHAR2(4000),
    string12 VARCHAR2(4000),
    PRIMARY KEY (sequence_number)
);

CREATE INDEX change_log_entry_string01_idx ON grouper_change_log_entry (string01);

CREATE INDEX change_log_entry_string02_idx ON grouper_change_log_entry (string02);

CREATE INDEX change_log_entry_string03_idx ON grouper_change_log_entry (string03);

CREATE INDEX change_log_entry_string04_idx ON grouper_change_log_entry (string04);

CREATE INDEX change_log_entry_string05_idx ON grouper_change_log_entry (string05);

CREATE INDEX change_log_entry_string06_idx ON grouper_change_log_entry (string06);

CREATE INDEX change_log_entry_string07_idx ON grouper_change_log_entry (string07);

CREATE INDEX change_log_entry_string08_idx ON grouper_change_log_entry (string08);

CREATE INDEX change_log_entry_string09_idx ON grouper_change_log_entry (string09);

CREATE INDEX change_log_entry_string10_idx ON grouper_change_log_entry (string10);

CREATE INDEX change_log_entry_string11_idx ON grouper_change_log_entry (string11);

CREATE INDEX change_log_entry_string12_idx ON grouper_change_log_entry (string12);

CREATE INDEX change_log_sequence_number_idx ON grouper_change_log_entry (sequence_number, created_on);

CREATE INDEX change_log_context_id_idx ON grouper_change_log_entry (context_id);

CREATE INDEX change_log_created_on_idx ON grouper_change_log_entry (created_on);

CREATE TABLE grouper_attribute_def
(
    attribute_def_public VARCHAR2(1) DEFAULT 'F' NOT NULL,
    attribute_def_type VARCHAR2(32) DEFAULT 'attr' NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    creator_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    last_updated NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    description VARCHAR2(1024),
    extension VARCHAR2(255) NOT NULL,
    name VARCHAR2(1024) NOT NULL,
    multi_assignable VARCHAR2(1) DEFAULT 'F' NOT NULL,
    multi_valued VARCHAR2(1) DEFAULT 'F' NOT NULL,
    stem_id VARCHAR2(40) NOT NULL,
    value_type VARCHAR2(32) DEFAULT 'marker' NOT NULL,
    assign_to_attribute_def VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_attribute_def_assn VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_eff_membership VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_eff_membership_assn VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_group VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_group_assn VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_imm_membership VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_imm_membership_assn VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_member VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_member_assn VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_stem VARCHAR2(1) DEFAULT 'F' NOT NULL,
    assign_to_stem_assn VARCHAR2(1) DEFAULT 'F' NOT NULL,
    id_index NUMBER(38) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX attribute_def_name_idx ON grouper_attribute_def (name);

CREATE INDEX attribute_def_type_idx ON grouper_attribute_def (attribute_def_type);

CREATE UNIQUE INDEX attribute_def_id_index_idx ON grouper_attribute_def (id_index);

CREATE TABLE grouper_attribute_def_name
(
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    last_updated NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    description VARCHAR2(1024),
    extension VARCHAR2(255) NOT NULL,
    name VARCHAR2(1024) NOT NULL,
    stem_id VARCHAR2(40) NOT NULL,
    attribute_def_id VARCHAR2(40) NOT NULL,
    display_extension VARCHAR2(128) NOT NULL,
    display_name VARCHAR2(1024) NOT NULL,
    id_index NUMBER(38) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX attribute_def_name_name_idx ON grouper_attribute_def_name (name);

CREATE UNIQUE INDEX attr_def_name_id_index_idx ON grouper_attribute_def_name (id_index);

CREATE TABLE grouper_attribute_assign
(
    attribute_assign_action_id VARCHAR2(40) NOT NULL,
    attribute_def_name_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    disabled_time NUMBER(38),
    enabled VARCHAR2(1) DEFAULT 'T' NOT NULL,
    enabled_time NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    notes VARCHAR2(1024),
    attribute_assign_delegatable VARCHAR2(15) NOT NULL,
    attribute_assign_type VARCHAR2(15) NOT NULL,
    owner_attribute_assign_id VARCHAR2(40),
    owner_attribute_def_id VARCHAR2(40),
    owner_group_id VARCHAR2(40),
    owner_member_id VARCHAR2(40),
    owner_membership_id VARCHAR2(40),
    owner_stem_id VARCHAR2(40),
    disallowed VARCHAR2(1),
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
    attribute_assign_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    value_integer NUMBER(38),
    value_floating FLOAT,
    value_string VARCHAR2(4000),
    value_member_id VARCHAR2(40),
    PRIMARY KEY (id)
);

CREATE INDEX attribute_val_assign_idx ON grouper_attribute_assign_value (attribute_assign_id);

CREATE INDEX attribute_val_string_idx ON grouper_attribute_assign_value (value_string);

CREATE INDEX attribute_val_integer_idx ON grouper_attribute_assign_value (value_integer);

CREATE INDEX attribute_val_member_id_idx ON grouper_attribute_assign_value (value_member_id);

CREATE TABLE grouper_attribute_def_scope
(
    attribute_def_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    attribute_def_scope_type VARCHAR2(32),
    scope_string VARCHAR2(1024),
    scope_string2 VARCHAR2(1024),
    PRIMARY KEY (id)
);

CREATE INDEX attribute_def_scope_atdef_idx ON grouper_attribute_def_scope (attribute_def_id);

CREATE TABLE grouper_attribute_def_name_set
(
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    depth NUMBER(38) NOT NULL,
    if_has_attribute_def_name_id VARCHAR2(40) NOT NULL,
    then_has_attribute_def_name_id VARCHAR2(40) NOT NULL,
    parent_attr_def_name_set_id VARCHAR2(40),
    type VARCHAR2(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX attr_def_name_set_ifhas_idx ON grouper_attribute_def_name_set (if_has_attribute_def_name_id);

CREATE INDEX attr_def_name_set_then_idx ON grouper_attribute_def_name_set (then_has_attribute_def_name_id);

CREATE UNIQUE INDEX attr_def_name_set_unq_idx ON grouper_attribute_def_name_set (parent_attr_def_name_set_id, if_has_attribute_def_name_id, then_has_attribute_def_name_id);

CREATE TABLE grouper_attr_assign_action
(
    attribute_def_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    name VARCHAR2(40),
    PRIMARY KEY (id)
);

CREATE INDEX attr_assn_act_def_id_idx ON grouper_attr_assign_action (attribute_def_id);

CREATE TABLE grouper_attr_assign_action_set
(
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    depth NUMBER(38) NOT NULL,
    if_has_attr_assn_action_id VARCHAR2(40) NOT NULL,
    then_has_attr_assn_action_id VARCHAR2(40) NOT NULL,
    parent_attr_assn_action_id VARCHAR2(40),
    type VARCHAR2(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX action_set_ifhas_idx ON grouper_attr_assign_action_set (if_has_attr_assn_action_id);

CREATE INDEX action_set_then_idx ON grouper_attr_assign_action_set (then_has_attr_assn_action_id);

CREATE UNIQUE INDEX action_set_unq_idx ON grouper_attr_assign_action_set (parent_attr_assn_action_id, if_has_attr_assn_action_id, then_has_attr_assn_action_id);

CREATE TABLE grouper_role_set
(
    context_id VARCHAR2(40),
    created_on NUMBER(38),
    hibernate_version_number NUMBER(38),
    id VARCHAR2(40) NOT NULL,
    last_updated NUMBER(38),
    depth NUMBER(38) NOT NULL,
    if_has_role_id VARCHAR2(40) NOT NULL,
    then_has_role_id VARCHAR2(40) NOT NULL,
    parent_role_set_id VARCHAR2(40),
    type VARCHAR2(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX role_set_ifhas_idx ON grouper_role_set (if_has_role_id);

CREATE INDEX role_set_then_idx ON grouper_role_set (then_has_role_id);

CREATE UNIQUE INDEX role_set_unq_idx ON grouper_role_set (parent_role_set_id, if_has_role_id, then_has_role_id);

CREATE TABLE grouper_pit_members
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    subject_id VARCHAR2(255) NOT NULL,
    subject_source VARCHAR2(255) NOT NULL,
    subject_type VARCHAR2(255) NOT NULL,
    subject_identifier0 VARCHAR2(255),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    name VARCHAR2(32) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_field_source_id_idx ON grouper_pit_fields (source_id);

CREATE INDEX pit_field_name_idx ON grouper_pit_fields (name);

CREATE INDEX pit_field_context_idx ON grouper_pit_fields (context_id);

CREATE UNIQUE INDEX pit_field_start_idx ON grouper_pit_fields (start_time, source_id);

CREATE INDEX pit_field_end_idx ON grouper_pit_fields (end_time);

CREATE TABLE grouper_pit_groups
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    name VARCHAR2(1024) NOT NULL,
    stem_id VARCHAR2(40) NOT NULL,
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_group_source_id_idx ON grouper_pit_groups (source_id);

CREATE INDEX pit_group_name_idx ON grouper_pit_groups (name);

CREATE INDEX pit_group_parent_idx ON grouper_pit_groups (stem_id);

CREATE INDEX pit_group_context_idx ON grouper_pit_groups (context_id);

CREATE UNIQUE INDEX pit_group_start_idx ON grouper_pit_groups (start_time, source_id);

CREATE INDEX pit_group_end_idx ON grouper_pit_groups (end_time);

CREATE TABLE grouper_pit_stems
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    name VARCHAR2(1024) NOT NULL,
    parent_stem_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_stem_source_id_idx ON grouper_pit_stems (source_id);

CREATE INDEX pit_stem_name_idx ON grouper_pit_stems (name);

CREATE INDEX pit_stem_parent_idx ON grouper_pit_stems (parent_stem_id);

CREATE INDEX pit_stem_context_idx ON grouper_pit_stems (context_id);

CREATE UNIQUE INDEX pit_stem_start_idx ON grouper_pit_stems (start_time, source_id);

CREATE INDEX pit_stem_end_idx ON grouper_pit_stems (end_time);

CREATE TABLE grouper_pit_attribute_def
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    name VARCHAR2(1024) NOT NULL,
    stem_id VARCHAR2(40) NOT NULL,
    attribute_def_type VARCHAR2(32) NOT NULL,
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_def_source_id_idx ON grouper_pit_attribute_def (source_id);

CREATE INDEX pit_attribute_def_name_idx ON grouper_pit_attribute_def (name);

CREATE INDEX pit_attribute_def_parent_idx ON grouper_pit_attribute_def (stem_id);

CREATE INDEX pit_attribute_def_context_idx ON grouper_pit_attribute_def (context_id);

CREATE INDEX pit_attribute_def_type_idx ON grouper_pit_attribute_def (attribute_def_type);

CREATE UNIQUE INDEX pit_attribute_def_start_idx ON grouper_pit_attribute_def (start_time, source_id);

CREATE INDEX pit_attribute_def_end_idx ON grouper_pit_attribute_def (end_time);

CREATE TABLE grouper_pit_memberships
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    owner_id VARCHAR2(40) NOT NULL,
    owner_attr_def_id VARCHAR2(40),
    owner_group_id VARCHAR2(40),
    owner_stem_id VARCHAR2(40),
    member_id VARCHAR2(40) NOT NULL,
    field_id VARCHAR2(40) NOT NULL,
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    owner_id VARCHAR2(40) NOT NULL,
    owner_attr_def_id VARCHAR2(40),
    owner_group_id VARCHAR2(40),
    owner_stem_id VARCHAR2(40),
    member_id VARCHAR2(40) NOT NULL,
    member_attr_def_id VARCHAR2(40),
    member_group_id VARCHAR2(40),
    member_stem_id VARCHAR2(40),
    field_id VARCHAR2(40) NOT NULL,
    member_field_id VARCHAR2(40) NOT NULL,
    depth INTEGER NOT NULL,
    parent_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    attribute_def_name_id VARCHAR2(40) NOT NULL,
    attribute_assign_action_id VARCHAR2(40) NOT NULL,
    attribute_assign_type VARCHAR2(15) NOT NULL,
    owner_attribute_assign_id VARCHAR2(40),
    owner_attribute_def_id VARCHAR2(40),
    owner_group_id VARCHAR2(40),
    owner_member_id VARCHAR2(40),
    owner_membership_id VARCHAR2(40),
    owner_stem_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    disallowed VARCHAR2(1),
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
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    attribute_assign_id VARCHAR2(40) NOT NULL,
    value_integer NUMBER(38),
    value_floating FLOAT,
    value_string VARCHAR2(4000),
    value_member_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_val_source_id_idx ON grouper_pit_attr_assn_value (source_id);

CREATE INDEX pit_attr_val_assign_idx ON grouper_pit_attr_assn_value (attribute_assign_id);

CREATE INDEX pit_attr_val_string_idx ON grouper_pit_attr_assn_value (value_string);

CREATE INDEX pit_attr_val_integer_idx ON grouper_pit_attr_assn_value (value_integer);

CREATE INDEX pit_attr_val_floating_idx ON grouper_pit_attr_assn_value (value_floating);

CREATE INDEX pit_attr_val_member_id_idx ON grouper_pit_attr_assn_value (value_member_id);

CREATE UNIQUE INDEX pit_attr_val_start_idx ON grouper_pit_attr_assn_value (start_time, source_id);

CREATE INDEX pit_attr_val_end_idx ON grouper_pit_attr_assn_value (end_time);

CREATE TABLE grouper_pit_attr_assn_actn
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    attribute_def_id VARCHAR2(40) NOT NULL,
    name VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_attr_asn_act_source_id_idx ON grouper_pit_attr_assn_actn (source_id);

CREATE INDEX pit_attr_assn_act_def_id_idx ON grouper_pit_attr_assn_actn (attribute_def_id);

CREATE UNIQUE INDEX pit_attr_assn_act_start_idx ON grouper_pit_attr_assn_actn (start_time, source_id);

CREATE INDEX pit_attr_assn_act_end_idx ON grouper_pit_attr_assn_actn (end_time);

CREATE TABLE grouper_pit_attr_def_name
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    stem_id VARCHAR2(40) NOT NULL,
    attribute_def_id VARCHAR2(40) NOT NULL,
    name VARCHAR2(1024) NOT NULL,
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_attrdef_name_srcid_idx ON grouper_pit_attr_def_name (source_id);

CREATE INDEX pit_attr_def_name_name_idx ON grouper_pit_attr_def_name (name);

CREATE INDEX pit_attr_def_name_stem_idx ON grouper_pit_attr_def_name (stem_id);

CREATE INDEX pit_attr_def_name_def_idx ON grouper_pit_attr_def_name (attribute_def_id);

CREATE UNIQUE INDEX pit_attr_def_name_start_idx ON grouper_pit_attr_def_name (start_time, source_id);

CREATE INDEX pit_attr_def_name_end_idx ON grouper_pit_attr_def_name (end_time);

CREATE TABLE grouper_pit_attr_def_name_set
(
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    depth NUMBER(38) NOT NULL,
    if_has_attribute_def_name_id VARCHAR2(40) NOT NULL,
    then_has_attribute_def_name_id VARCHAR2(40) NOT NULL,
    parent_attr_def_name_set_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    depth NUMBER(38) NOT NULL,
    if_has_attr_assn_action_id VARCHAR2(40) NOT NULL,
    then_has_attr_assn_action_id VARCHAR2(40) NOT NULL,
    parent_attr_assn_action_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    id VARCHAR2(40) NOT NULL,
    source_id VARCHAR2(40) NOT NULL,
    depth NUMBER(38) NOT NULL,
    if_has_role_id VARCHAR2(40) NOT NULL,
    then_has_role_id VARCHAR2(40) NOT NULL,
    parent_role_set_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
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
    uuid VARCHAR2(40) NOT NULL,
    name VARCHAR2(200),
    identifier VARCHAR2(300),
    description VARCHAR2(500),
    institution VARCHAR2(200),
    email VARCHAR2(200),
    search_string_lower VARCHAR2(4000),
    create_time NUMBER(38) NOT NULL,
    creator_member_id VARCHAR2(40) NOT NULL,
    modify_time NUMBER(38) NOT NULL,
    modifier_member_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40) NOT NULL,
    enabled VARCHAR2(1) NOT NULL,
    disabled_time NUMBER(38),
    hibernate_version_number NUMBER(38) NOT NULL,
    vetted_email_addresses VARCHAR2(4000),
    PRIMARY KEY (uuid)
);

CREATE INDEX grouper_ext_subj_cxt_id_idx ON grouper_ext_subj (context_id);

CREATE UNIQUE INDEX grouper_ext_subj_idfr_idx ON grouper_ext_subj (identifier);

CREATE TABLE grouper_ext_subj_attr
(
    uuid VARCHAR2(40) NOT NULL,
    attribute_system_name VARCHAR2(200) NOT NULL,
    attribute_value VARCHAR2(600),
    subject_uuid VARCHAR2(40) NOT NULL,
    create_time NUMBER(38) NOT NULL,
    creator_member_id VARCHAR2(40) NOT NULL,
    modify_time NUMBER(38) NOT NULL,
    modifier_member_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40) NOT NULL,
    hibernate_version_number NUMBER(38) NOT NULL,
    PRIMARY KEY (uuid)
);

CREATE INDEX grouper_extsubjattr_cxtid_idx ON grouper_ext_subj_attr (context_id);

CREATE UNIQUE INDEX grouper_extsubjattr_subj_idx ON grouper_ext_subj_attr (subject_uuid, attribute_system_name);

CREATE INDEX grouper_extsubjattr_value_idx ON grouper_ext_subj_attr (attribute_value);

CREATE TABLE grouper_stem_set
(
    id VARCHAR2(40) NOT NULL,
    if_has_stem_id VARCHAR2(40) NOT NULL,
    then_has_stem_id VARCHAR2(40) NOT NULL,
    parent_stem_set_id VARCHAR2(40),
    type VARCHAR2(32) NOT NULL,
    depth NUMBER(38) NOT NULL,
    created_on NUMBER(38),
    last_updated NUMBER(38),
    context_id VARCHAR2(40),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX stem_set_ifhas_idx ON grouper_stem_set (if_has_stem_id);

CREATE INDEX stem_set_then_idx ON grouper_stem_set (then_has_stem_id);

CREATE UNIQUE INDEX stem_set_unq_idx ON grouper_stem_set (parent_stem_set_id, if_has_stem_id, then_has_stem_id);

CREATE TABLE grouper_table_index
(
    id VARCHAR2(40) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    last_index_reserved NUMBER(38),
    created_on NUMBER(38),
    last_updated NUMBER(38),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX table_index_type_idx ON grouper_table_index (type);

CREATE TABLE grouper_loader_log
(
    id VARCHAR2(40) NOT NULL,
    job_name VARCHAR2(512),
    status VARCHAR2(20),
    started_time DATE,
    ended_time DATE,
    millis INTEGER,
    millis_get_data INTEGER,
    millis_load_data INTEGER,
    job_type VARCHAR2(128),
    job_schedule_type VARCHAR2(128),
    job_description VARCHAR2(4000),
    job_message VARCHAR2(4000),
    host VARCHAR2(128),
    group_uuid VARCHAR2(40),
    job_schedule_quartz_cron VARCHAR2(128),
    job_schedule_interval_seconds INTEGER,
    last_updated DATE,
    unresolvable_subject_count INTEGER,
    insert_count INTEGER,
    update_count INTEGER,
    delete_count INTEGER,
    total_count INTEGER,
    parent_job_name VARCHAR2(512),
    parent_job_id VARCHAR2(40),
    and_group_names VARCHAR2(512),
    job_schedule_priority INTEGER,
    context_id VARCHAR2(40),
    PRIMARY KEY (id)
);

CREATE INDEX grouper_loader_job_name_idx ON grouper_loader_log (job_name, status, ended_time);

CREATE INDEX loader_context_idx ON grouper_loader_log (context_id);

CREATE TABLE grouper_message
(
    id VARCHAR2(40) NOT NULL,
    sent_time_micros NUMBER(38) NOT NULL,
    get_attempt_time_millis NUMBER(38) NOT NULL,
    get_attempt_count NUMBER(38) NOT NULL,
    state VARCHAR2(20) NOT NULL,
    get_time_millis NUMBER(38),
    from_member_id VARCHAR2(40) NOT NULL,
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

CREATE TABLE grouper_config
(
    id VARCHAR2(40) NOT NULL,
    config_file_name VARCHAR2(100) NOT NULL,
    config_key VARCHAR2(400) NOT NULL,
    config_value VARCHAR2(4000),
    config_comment VARCHAR2(4000),
    config_file_hierarchy VARCHAR2(50) NOT NULL,
    config_encrypted VARCHAR2(1) NOT NULL,
    config_sequence NUMBER(38) NOT NULL,
    config_version_index NUMBER(38),
    last_updated NUMBER(38) NOT NULL,
    hibernate_version_number NUMBER(38) NOT NULL,
    config_value_clob CLOB,
    config_value_bytes INTEGER,
    PRIMARY KEY (id)
);

CREATE INDEX grpconfig_config_file_idx ON grouper_config (config_file_name, last_updated);

CREATE INDEX grpconfig_config_key_idx ON grouper_config (config_key, config_file_name);

CREATE INDEX grpconfig_last_updated_idx ON grouper_config (last_updated);

CREATE UNIQUE INDEX grpconfig_unique_idx ON grouper_config (config_file_name, config_file_hierarchy, config_key, config_sequence);

CREATE TABLE grouper_password
(
    id VARCHAR2(40) NOT NULL,
    username VARCHAR2(255) NOT NULL,
    member_id VARCHAR2(40),
    entity_type VARCHAR2(20),
    is_hashed VARCHAR2(1) NOT NULL,
    encryption_type VARCHAR2(20) NOT NULL,
    the_salt VARCHAR2(255),
    the_password VARCHAR2(4000),
    application VARCHAR2(20) NOT NULL,
    allowed_from_cidrs VARCHAR2(4000),
    recent_source_addresses VARCHAR2(4000),
    failed_source_addresses VARCHAR2(4000),
    last_authenticated NUMBER(38),
    last_edited NUMBER(38) NOT NULL,
    failed_logins VARCHAR2(4000),
    hibernate_version_number NUMBER(38),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grppassword_username_idx ON grouper_password (username, application);

CREATE TABLE grouper_password_recently_used
(
    id VARCHAR2(40) NOT NULL,
    grouper_password_id VARCHAR2(40) NOT NULL,
    jwt_jti VARCHAR2(100) NOT NULL,
    jwt_iat INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE grouper_sync
(
    id VARCHAR2(40) NOT NULL,
    sync_engine VARCHAR2(50),
    provisioner_name VARCHAR2(100) NOT NULL,
    group_count INTEGER,
    user_count INTEGER,
    records_count INTEGER,
    incremental_index NUMBER(38),
    incremental_timestamp DATE,
    last_incremental_sync_run DATE,
    last_full_sync_start DATE,
    last_full_sync_run DATE,
    last_full_metadata_sync_start DATE,
    last_full_metadata_sync_run DATE,
    last_updated DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_eng_idx ON grouper_sync (sync_engine, provisioner_name);

CREATE UNIQUE INDEX grouper_sync_eng_prov_idx ON grouper_sync (provisioner_name);

CREATE TABLE grouper_sync_job
(
    id VARCHAR2(40) NOT NULL,
    grouper_sync_id VARCHAR2(40) NOT NULL,
    sync_type VARCHAR2(50) NOT NULL,
    job_state VARCHAR2(50),
    last_sync_index NUMBER(38),
    last_sync_start DATE,
    last_sync_timestamp DATE,
    last_time_work_was_done DATE,
    heartbeat DATE,
    quartz_job_name VARCHAR2(400),
    percent_complete INTEGER,
    last_updated DATE NOT NULL,
    error_message VARCHAR2(4000),
    error_timestamp DATE,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_st_ty_idx ON grouper_sync_job (grouper_sync_id, sync_type);

CREATE TABLE grouper_sync_group
(
    id VARCHAR2(40) NOT NULL,
    grouper_sync_id VARCHAR2(40) NOT NULL,
    group_id VARCHAR2(40) NOT NULL,
    group_name VARCHAR2(1024),
    group_id_index NUMBER(38),
    provisionable VARCHAR2(1),
    in_target VARCHAR2(1),
    in_target_insert_or_exists VARCHAR2(1),
    in_target_start DATE,
    in_target_end DATE,
    provisionable_start DATE,
    provisionable_end DATE,
    last_updated DATE NOT NULL,
    last_group_sync_start DATE,
    last_group_sync DATE,
    last_group_metadata_sync_start DATE,
    last_group_metadata_sync DATE,
    group_from_id2 VARCHAR2(4000),
    group_from_id3 VARCHAR2(4000),
    group_to_id2 VARCHAR2(4000),
    group_to_id3 VARCHAR2(4000),
    metadata_updated DATE,
    error_message VARCHAR2(4000),
    error_timestamp DATE,
    last_time_work_was_done DATE,
    error_code VARCHAR2(3),
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_gr_sync_id_idx ON grouper_sync_group (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_gr_group_id_idx ON grouper_sync_group (group_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_gr_sy_gr_idx ON grouper_sync_group (grouper_sync_id, group_id);

CREATE INDEX grouper_sync_gr_f2_idx ON grouper_sync_group (grouper_sync_id, group_from_id2);

CREATE INDEX grouper_sync_gr_f3_idx ON grouper_sync_group (grouper_sync_id, group_from_id3);

CREATE INDEX grouper_sync_gr_t2_idx ON grouper_sync_group (grouper_sync_id, group_to_id2);

CREATE INDEX grouper_sync_gr_t3_idx ON grouper_sync_group (grouper_sync_id, group_to_id3);

CREATE INDEX grouper_sync_gr_er_idx ON grouper_sync_group (grouper_sync_id, error_code, error_timestamp);

CREATE TABLE grouper_sync_member
(
    id VARCHAR2(40) NOT NULL,
    grouper_sync_id VARCHAR2(40) NOT NULL,
    member_id VARCHAR2(128) NOT NULL,
    source_id VARCHAR2(255),
    subject_id VARCHAR2(255),
    subject_identifier VARCHAR2(255),
    in_target VARCHAR2(1),
    in_target_insert_or_exists VARCHAR2(1),
    in_target_start DATE,
    in_target_end DATE,
    provisionable VARCHAR2(1),
    provisionable_start DATE,
    provisionable_end DATE,
    last_updated DATE NOT NULL,
    last_user_sync_start DATE,
    last_user_sync DATE,
    last_user_metadata_sync_start DATE,
    last_user_metadata_sync DATE,
    member_from_id2 VARCHAR2(4000),
    member_from_id3 VARCHAR2(4000),
    member_to_id2 VARCHAR2(4000),
    member_to_id3 VARCHAR2(4000),
    metadata_updated DATE,
    last_time_work_was_done DATE,
    error_message VARCHAR2(4000),
    error_timestamp DATE,
    error_code VARCHAR2(3),
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_us_sync_id_idx ON grouper_sync_member (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_us_mem_id_idx ON grouper_sync_member (member_id, last_updated);

CREATE UNIQUE INDEX grouper_sync_us_sm_idx ON grouper_sync_member (grouper_sync_id, member_id);

CREATE INDEX grouper_sync_us_f2_idx ON grouper_sync_member (grouper_sync_id, member_from_id2);

CREATE INDEX grouper_sync_us_f3_idx ON grouper_sync_member (grouper_sync_id, member_from_id3);

CREATE INDEX grouper_sync_us_t2_idx ON grouper_sync_member (grouper_sync_id, member_to_id2);

CREATE INDEX grouper_sync_us_t3_idx ON grouper_sync_member (grouper_sync_id, member_to_id3);

CREATE INDEX grouper_sync_us_st_gr_idx ON grouper_sync_member (grouper_sync_id, source_id, subject_id);

CREATE INDEX grouper_sync_us_er_idx ON grouper_sync_member (grouper_sync_id, error_code, error_timestamp);

CREATE TABLE grouper_sync_membership
(
    id VARCHAR2(40) NOT NULL,
    grouper_sync_id VARCHAR2(40) NOT NULL,
    grouper_sync_group_id VARCHAR2(40) NOT NULL,
    grouper_sync_member_id VARCHAR2(40) NOT NULL,
    in_target VARCHAR2(1),
    in_target_insert_or_exists VARCHAR2(1),
    in_target_start DATE,
    in_target_end DATE,
    last_updated DATE NOT NULL,
    membership_id VARCHAR2(4000),
    membership_id2 VARCHAR2(4000),
    metadata_updated DATE,
    error_message VARCHAR2(4000),
    error_timestamp DATE,
    error_code VARCHAR2(3),
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_id, grouper_sync_group_id, grouper_sync_member_id);

CREATE INDEX grouper_sync_mship_me_idx ON grouper_sync_membership (grouper_sync_group_id, last_updated);

CREATE INDEX grouper_sync_mship_sy_idx ON grouper_sync_membership (grouper_sync_id, last_updated);

CREATE INDEX grouper_sync_mship_f1_idx ON grouper_sync_membership (grouper_sync_id, membership_id);

CREATE INDEX grouper_sync_mship_f2_idx ON grouper_sync_membership (grouper_sync_id, membership_id2);

CREATE INDEX grouper_sync_mship_er_idx ON grouper_sync_membership (grouper_sync_id, error_code, error_timestamp);

CREATE TABLE grouper_sync_log
(
    id VARCHAR2(40) NOT NULL,
    grouper_sync_owner_id VARCHAR2(40),
    grouper_sync_id VARCHAR2(40),
    status VARCHAR2(20),
    sync_timestamp_start DATE,
    sync_timestamp DATE,
    description VARCHAR2(4000),
    records_processed INTEGER,
    records_changed INTEGER,
    job_took_millis INTEGER,
    server VARCHAR2(200),
    last_updated DATE NOT NULL,
    description_clob CLOB,
    description_bytes INTEGER,
    PRIMARY KEY (id)
);

CREATE INDEX grouper_sync_log_sy_idx ON grouper_sync_log (grouper_sync_id, sync_timestamp);

CREATE INDEX grouper_sync_log_ow_idx ON grouper_sync_log (grouper_sync_owner_id, sync_timestamp);

CREATE TABLE grouper_time
(
    time_label VARCHAR2(10) NOT NULL,
    the_utc_timestamp DATE NOT NULL,
    this_tz_timestamp DATE NOT NULL,
    utc_millis_since_1970 NUMBER(38) NOT NULL,
    utc_micros_since_1970 NUMBER(38) NOT NULL,
    PRIMARY KEY (time_label)
);

CREATE TABLE grouper_cache_overall
(
    overall_cache INTEGER NOT NULL,
    nanos_since_1970 NUMBER(38) NOT NULL,
    PRIMARY KEY (overall_cache)
);

CREATE TABLE grouper_cache_instance
(
    cache_name VARCHAR2(250) NOT NULL,
    nanos_since_1970 NUMBER(38) NOT NULL,
    PRIMARY KEY (cache_name)
);

CREATE INDEX grouper_cache_inst_cache_idx ON grouper_cache_instance (nanos_since_1970);

CREATE TABLE grouper_recent_mships_conf
(
    group_uuid_to VARCHAR2(40) NOT NULL,
    group_name_to VARCHAR2(1024) NOT NULL,
    group_uuid_from VARCHAR2(40) NOT NULL,
    group_name_from VARCHAR2(1024) NOT NULL,
    recent_micros NUMBER(38) NOT NULL,
    include_eligible VARCHAR2(1) NOT NULL,
    PRIMARY KEY (group_uuid_to)
);

CREATE INDEX grouper_recent_mships_idfr_idx ON grouper_recent_mships_conf (group_uuid_from);

CREATE TABLE grouper_pit_config
(
    id VARCHAR2(40) NOT NULL,
    config_file_name VARCHAR2(100) NOT NULL,
    config_key VARCHAR2(400) NOT NULL,
    config_value VARCHAR2(4000),
    config_comment VARCHAR2(4000),
    config_file_hierarchy VARCHAR2(50) NOT NULL,
    config_encrypted VARCHAR2(1) NOT NULL,
    config_sequence NUMBER(38) NOT NULL,
    config_version_index NUMBER(38),
    last_updated NUMBER(38) NOT NULL,
    hibernate_version_number NUMBER(38) NOT NULL,
    config_value_clob CLOB,
    config_value_bytes INTEGER,
    prev_config_value VARCHAR2(4000),
    prev_config_value_clob CLOB,
    source_id VARCHAR2(40) NOT NULL,
    context_id VARCHAR2(40),
    active VARCHAR2(1) NOT NULL,
    start_time NUMBER(38) NOT NULL,
    end_time NUMBER(38),
    PRIMARY KEY (id)
);

CREATE INDEX pit_config_context_idx ON grouper_pit_config (context_id);

CREATE INDEX pit_config_source_id_idx ON grouper_pit_config (source_id);

CREATE UNIQUE INDEX pit_config_start_idx ON grouper_pit_config (start_time, source_id);

CREATE INDEX pit_config_end_idx ON grouper_pit_config (end_time);

CREATE TABLE grouper_file
(
    id VARCHAR2(40) NOT NULL,
    system_name VARCHAR2(100) NOT NULL,
    file_name VARCHAR2(100) NOT NULL,
    file_path VARCHAR2(400) NOT NULL,
    hibernate_version_number NUMBER(38) NOT NULL,
    context_id VARCHAR2(40),
    file_contents_varchar VARCHAR2(4000),
    file_contents_bytes INTEGER,
    file_contents_clob CLOB,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX grpfile_unique_idx ON grouper_file (file_path);

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

COMMENT ON COLUMN grouper_members.subject_identifier0 IS 'subject identifier of the subject';

COMMENT ON COLUMN grouper_pit_members.subject_identifier0 IS 'subject identifier of the subject';

CREATE VIEW grouper_groups_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, TYPE_OF_GROUP, GROUP_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.type_of_group, gg.id as group_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ;

COMMENT ON TABLE grouper_groups_v IS 'Contains one record for each group, with friendly names for some attributes and some more information';

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

CREATE VIEW grouper_roles_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, ROLE_ID, PARENT_STEM_ID, ENABLED, ENABLED_TIMESTAMP, DISABLED_TIMESTAMP, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATOR_SOURCE, CREATOR_SUBJECT_ID, IS_COMPOSITE_OWNER, IS_COMPOSITE_FACTOR, CREATOR_ID, CREATE_TIME, MODIFIER_ID, MODIFY_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  gg.extension as extension, gg.name as name, gg.display_extension as display_extension, gg.display_name as display_name, gg.description as description, gs.NAME as parent_stem_name, gg.id as role_id, gs.ID as parent_stem_id, gg.enabled, gg.enabled_timestamp, gg.disabled_timestamp, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, (select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, (select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, gg.CREATOR_ID, gg.CREATE_TIME, gg.MODIFIER_ID, gg.MODIFY_TIME, gg.HIBERNATE_VERSION_NUMBER, gg.context_id   from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and type_of_group = 'role' ;

COMMENT ON TABLE grouper_roles_v IS 'Contains one record for each role, with friendly names for some attributes and some more information';

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

COMMENT ON COLUMN grouper_roles_v.IS_COMPOSITE_FACTOR IS 'IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemented';

COMMENT ON COLUMN grouper_roles_v.CREATOR_ID IS 'CREATOR_ID: member id of the subject who created this role, foreign key to grouper_members';

COMMENT ON COLUMN grouper_roles_v.CREATE_TIME IS 'CREATE_TIME: number of millis since 1970 since this role was created';

COMMENT ON COLUMN grouper_roles_v.MODIFIER_ID IS 'MODIFIER_ID: member id of the subject who last modified this role, foreign key to grouper_members';

COMMENT ON COLUMN grouper_roles_v.MODIFY_TIME IS 'MODIFY_TIME: number of millis since 1970 since this role was last changed';

COMMENT ON COLUMN grouper_roles_v.HIBERNATE_VERSION_NUMBER IS 'HIBERNATE_VERSION_NUMBER: increments by 1 for each update';

COMMENT ON COLUMN grouper_roles_v.CONTEXT_ID IS 'Context id links together multiple operations into one high level action';

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

COMMENT ON COLUMN grouper_sync_group.error_code IS 'Error code e.g. ERR error, INV invalid based on script, LEN attribute too large, REQ required attribute missing, DNE data in target does not exist';

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

COMMENT ON COLUMN grouper_sync_member.error_code IS 'Error code e.g. ERR error, INV invalid based on script, LEN attribute too large, REQ required attribute missing, DNE data in target does not exist';

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

COMMENT ON COLUMN grouper_sync_membership.error_code IS 'Error code e.g. ERR error, INV invalid based on script, LEN attribute too large, REQ required attribute missing, DNE data in target does not exist';

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

COMMENT ON TABLE grouper_time IS 'Update the row with current time before joining to other tables (e.g. for recent memberships)';

COMMENT ON COLUMN grouper_time.time_label IS 'should only need one row with value: now';

COMMENT ON COLUMN grouper_time.the_utc_timestamp IS 'timestamp with time zone utc';

COMMENT ON COLUMN grouper_time.this_tz_timestamp IS 'timestamp with this time zone (from java)';

COMMENT ON COLUMN grouper_time.utc_millis_since_1970 IS 'millis since 1970 utc';

COMMENT ON COLUMN grouper_time.utc_micros_since_1970 IS 'micros since 1970 utc';

COMMENT ON TABLE grouper_cache_overall IS 'One row for the most time that any cache needs to be cleared';

COMMENT ON COLUMN grouper_cache_overall.overall_cache IS 'One row with an integer of 0 only';

COMMENT ON COLUMN grouper_cache_overall.nanos_since_1970 IS 'nanos since 1970 that the most recent cache was cleared';

COMMENT ON TABLE grouper_cache_instance IS 'Row for each cache instance and the time that it needs to be cleared';

COMMENT ON COLUMN grouper_cache_instance.cache_name IS 'cache name, if there are two underscores, split and the first part is cache, and second part is instance';

COMMENT ON COLUMN grouper_cache_instance.nanos_since_1970 IS 'time the cache was last changed';

COMMENT ON TABLE grouper_recent_mships_conf IS 'Contains one row for each recent membership configured on a group, sourced from grouper_recent_mships_conf_v';

COMMENT ON COLUMN grouper_recent_mships_conf.group_uuid_to IS 'group_uuid_to: uuid of the group which has the destination for the recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf.group_name_to IS 'group_name_to: name of the group which has the destination for the recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf.group_name_from IS 'group_name_from: group name of the group where the recent memberships are sourced from';

COMMENT ON COLUMN grouper_recent_mships_conf.group_uuid_from IS 'group_uuid_from: group uuid of the group where the recent memberships are sourced from';

COMMENT ON COLUMN grouper_recent_mships_conf.recent_micros IS 'recent_micros: number of microseconds of recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf.include_eligible IS 'include_eligible: T to include people still in group, F if not';

COMMENT ON COLUMN grouper_config.config_value_clob IS 'config value for large data';

COMMENT ON COLUMN grouper_config.config_value_bytes IS 'size of config value in bytes';

COMMENT ON TABLE grouper_pit_config IS 'keeps track of grouper config.  Records are never deleted from this table';

COMMENT ON COLUMN grouper_pit_config.id IS 'uuid of record is unique for all records in table and primary key';

COMMENT ON COLUMN grouper_pit_config.source_id IS 'source_id: id of the grouper_config table';

COMMENT ON COLUMN grouper_pit_config.config_value_bytes IS 'size of config value in bytes';

COMMENT ON COLUMN grouper_pit_config.config_value_clob IS 'config value for large data';

COMMENT ON COLUMN grouper_pit_config.config_file_name IS 'Config file name of the config this record relates to, e.g. grouper.config.properties';

COMMENT ON COLUMN grouper_pit_config.config_key IS 'key of the config, not including elConfig';

COMMENT ON COLUMN grouper_pit_config.config_value IS 'Value of the config';

COMMENT ON COLUMN grouper_pit_config.config_comment IS 'documentation of the config value';

COMMENT ON COLUMN grouper_pit_config.config_file_hierarchy IS 'config file hierarchy, e.g. base, institution, or env';

COMMENT ON COLUMN grouper_pit_config.config_encrypted IS 'if the value is encrypted';

COMMENT ON COLUMN grouper_pit_config.config_sequence IS 'if there is more data than fits in the column this is the 0 indexed order';

COMMENT ON COLUMN grouper_pit_config.config_version_index IS 'for built in configs, this is the index that will identify if the database configs should be replaced from the java code';

COMMENT ON COLUMN grouper_pit_config.last_updated IS 'when this record was inserted or last updated';

COMMENT ON COLUMN grouper_pit_config.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_pit_config.active IS 'T or F if this is an active record based on start and end dates';

COMMENT ON COLUMN grouper_pit_config.start_time IS 'millis from 1970 when this record was inserted';

COMMENT ON COLUMN grouper_pit_config.end_time IS 'millis from 1970 when this record was deleted';

COMMENT ON COLUMN grouper_pit_config.context_id IS 'Context id links together audit entry with the row';

COMMENT ON TABLE grouper_file IS 'table to store files for grouper. eg: workflow, reports';

COMMENT ON COLUMN grouper_file.id IS 'uuid of record is unique for all records in table and primary key';

COMMENT ON COLUMN grouper_file.system_name IS 'System name this file belongs to eg: workflow';

COMMENT ON COLUMN grouper_file.file_name IS 'Name of the file';

COMMENT ON COLUMN grouper_file.file_path IS 'Unique path of the file';

COMMENT ON COLUMN grouper_file.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_file.context_id IS 'Context id links together audit entry with the row';

COMMENT ON COLUMN grouper_file.file_contents_varchar IS 'contents of the file if can fit into 4000 bytes';

COMMENT ON COLUMN grouper_file.file_contents_clob IS 'large contents of the file';

COMMENT ON COLUMN grouper_file.file_contents_bytes IS 'size of file contents in bytes';

COMMENT ON COLUMN grouper_sync.last_full_sync_start IS 'start time of last successful full sync';

COMMENT ON COLUMN grouper_sync.last_full_metadata_sync_start IS 'start time of last successful full metadata sync';

COMMENT ON COLUMN grouper_sync_job.last_sync_start IS 'start time of this job';

COMMENT ON COLUMN grouper_sync_log.description_clob IS 'description for large data';

COMMENT ON COLUMN grouper_sync_log.description_bytes IS 'size of description in bytes';

COMMENT ON COLUMN grouper_sync_log.sync_timestamp_start IS 'start of sync operation for log';

COMMENT ON COLUMN grouper_sync_group.last_group_sync_start IS 'start of last successful group sync';

COMMENT ON COLUMN grouper_sync_group.last_group_metadata_sync_start IS 'start of last successful group metadata sync';

COMMENT ON COLUMN grouper_sync_member.last_user_sync_start IS 'start of last successful user sync';

COMMENT ON COLUMN grouper_sync_member.last_user_metadata_sync_start IS 'start of last successful user metadata sync';

CREATE VIEW grouper_sync_membership_v (g_group_name, g_group_id_index, u_source_id, u_subject_id, u_subject_identifier, m_in_target, m_id, m_in_target_insert_or_exists, m_in_target_start, m_in_target_end, m_last_updated, m_membership_id, m_membership_id2, m_metadata_updated, m_error_message, m_error_timestamp, s_id, s_sync_engine, s_provisioner_name, u_id, u_member_id, u_in_target, u_in_target_insert_or_exists, u_in_target_start, u_in_target_end, u_provisionable, u_provisionable_start, u_provisionable_end, u_last_updated, u_last_user_sync_start, u_last_user_sync, u_last_user_meta_sync_start, u_last_user_metadata_sync, u_member_from_id2, u_member_from_id3, u_member_to_id2, u_member_to_id3, u_metadata_updated, u_last_time_work_was_done, u_error_message, u_error_timestamp, g_id, g_group_id, g_provisionable, g_in_target, g_in_target_insert_or_exists, g_in_target_start, g_in_target_end, g_provisionable_start, g_provisionable_end, g_last_updated, g_last_group_sync_start, g_last_group_sync, g_last_group_meta_sync_start, g_last_group_metadata_sync, g_group_from_id2, g_group_from_id3, g_group_to_id2, g_group_to_id3, g_metadata_updated, g_error_message, g_error_timestamp, g_last_time_work_was_done, m_error_code, u_error_code, g_error_code) AS select g.group_name as g_group_name, g.group_id_index as g_group_id_index, u.source_id as u_source_id, u.subject_id as u_subject_id, u.subject_identifier as u_subject_identifier, m.in_target as m_in_target, m.id as m_id, m.in_target_insert_or_exists as m_in_target_insert_or_exists, m.in_target_start as m_in_target_start, m.in_target_end as m_in_target_end, m.last_updated as m_last_updated, m.membership_id as m_membership_id, m.membership_id2 as m_membership_id2, m.metadata_updated as m_metadata_updated, m.error_message as m_error_message, m.error_timestamp as m_error_timestamp, s.id as s_id, s.sync_engine as s_sync_engine, s.provisioner_name as s_provisioner_name, u.id as u_id, u.member_id as u_member_id, u.in_target as u_in_target, u.in_target_insert_or_exists as u_in_target_insert_or_exists, u.in_target_start as u_in_target_start, u.in_target_end as u_in_target_end, u.provisionable as u_provisionable, u.provisionable_start as u_provisionable_start, u.provisionable_end as u_provisionable_end, u.last_updated as u_last_updated, u.last_user_sync_start as u_last_user_sync_start, u.last_user_sync as u_last_user_sync, u.last_user_metadata_sync_start as u_last_user_meta_sync_start, u.last_user_metadata_sync as u_last_user_metadata_sync, u.member_from_id2 as u_member_from_id2, u.member_from_id3 as u_member_from_id3, u.member_to_id2 as u_member_to_id2, u.member_to_id3 as u_member_to_id3, u.metadata_updated as u_metadata_updated, u.last_time_work_was_done as u_last_time_work_was_done, u.error_message as u_error_message, u.error_timestamp as u_error_timestamp, g.id as g_id, g.group_id as g_group_id, g.provisionable as g_provisionable, g.in_target as g_in_target, g.in_target_insert_or_exists as g_in_target_insert_or_exists, g.in_target_start as g_in_target_start, g.in_target_end as g_in_target_end, g.provisionable_start as g_provisionable_start, g.provisionable_end as g_provisionable_end, g.last_updated as g_last_updated, g.last_group_sync_start as g_last_group_sync_start, g.last_group_sync as g_last_group_sync, g.last_group_metadata_sync_start as g_last_group_meta_sync_start, g.last_group_metadata_sync as g_last_group_metadata_sync, g.group_from_id2 as g_group_from_id2, g.group_from_id3 as g_group_from_id3, g.group_to_id2 as g_group_to_id2, g.group_to_id3 as g_group_to_id3, g.metadata_updated as g_metadata_updated, g.error_message as g_error_message, g.error_timestamp as g_error_timestamp, g.last_time_work_was_done as g_last_time_work_was_done,  m.error_code as m_error_code, u.error_code as u_error_code, g.error_code as g_error_code from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id;

COMMENT ON TABLE grouper_sync_membership_v IS 'Memberships for provisioning joined with the group, member, and sync tables';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_name IS 'g_group_name: grouper group system name';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_id_index IS 'g_group_id_index: grouper group id index';

COMMENT ON COLUMN grouper_sync_membership_v.u_source_id IS 'u_source_id: subject source id';

COMMENT ON COLUMN grouper_sync_membership_v.u_subject_id IS 'u_subject_id: subject id';

COMMENT ON COLUMN grouper_sync_membership_v.u_subject_identifier IS 'u_subject_identifier: subject identifier0';

COMMENT ON COLUMN grouper_sync_membership_v.m_in_target IS 'm_in_target: t/f if provisioned to target';

COMMENT ON COLUMN grouper_sync_membership_v.m_id IS 'm_id: sync membership id';

COMMENT ON COLUMN grouper_sync_membership_v.m_in_target_insert_or_exists IS 'm_in_target_insert_or_exists: t/f if it was inserted into target or already existed';

COMMENT ON COLUMN grouper_sync_membership_v.m_in_target_start IS 'm_in_target_start: timestamp was inserted or detected to be in target';

COMMENT ON COLUMN grouper_sync_membership_v.m_in_target_end IS 'm_in_target_end: timestamp was removed from target or detected not there';

COMMENT ON COLUMN grouper_sync_membership_v.m_last_updated IS 'm_last_updated: when sync membership last updated';

COMMENT ON COLUMN grouper_sync_membership_v.m_membership_id IS 'm_membership_id: link membership id';

COMMENT ON COLUMN grouper_sync_membership_v.m_membership_id2 IS 'm_membership_id2: link membership id2';

COMMENT ON COLUMN grouper_sync_membership_v.m_metadata_updated IS 'm_metadata_updated: when metadata e.g. links was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.m_error_message IS 'm_error_message: error message when last operation occurred unless a success happened afterward';

COMMENT ON COLUMN grouper_sync_membership_v.m_error_timestamp IS 'm_error_timestamp: timestamp last error occurred unless a success happened afterward';

COMMENT ON COLUMN grouper_sync_membership_v.s_id IS 's_id: sync id overall';

COMMENT ON COLUMN grouper_sync_membership_v.s_sync_engine IS 's_sync_engine: sync engine';

COMMENT ON COLUMN grouper_sync_membership_v.s_provisioner_name IS 's_provisioner_name: name of provisioner';

COMMENT ON COLUMN grouper_sync_membership_v.u_id IS 'u_id: sync member id';

COMMENT ON COLUMN grouper_sync_membership_v.u_member_id IS 'u_member_id: grouper member uuid for subject';

COMMENT ON COLUMN grouper_sync_membership_v.u_in_target IS 'u_in_target: t/f if entity is in target';

COMMENT ON COLUMN grouper_sync_membership_v.u_in_target_insert_or_exists IS 'u_in_target_insert_or_exists: t/f if grouper inserted the entity or if it already existed';

COMMENT ON COLUMN grouper_sync_membership_v.u_in_target_start IS 'u_in_target_start: when this entity started being in target or detected there';

COMMENT ON COLUMN grouper_sync_membership_v.u_in_target_end IS 'u_in_target_end: when this entity stopped being in target or detected not there';

COMMENT ON COLUMN grouper_sync_membership_v.u_provisionable IS 'u_provisionable: t/f if the entity is provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.u_provisionable_start IS 'u_provisionable_start: when this entity started being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.u_provisionable_end IS 'u_provisionable_end: when this entity stopped being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.u_last_updated IS 'u_last_updated: when the sync member was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.u_last_user_sync_start IS 'u_last_user_sync_start: when the user was last overall sync started';

COMMENT ON COLUMN grouper_sync_membership_v.u_last_user_sync IS 'u_last_user_sync: when the user was last overall synced';

COMMENT ON COLUMN grouper_sync_membership_v.u_last_user_meta_sync_start IS 'u_last_user_meta_sync_start: when the metadata was sync started';

COMMENT ON COLUMN grouper_sync_membership_v.u_last_user_metadata_sync IS 'u_last_user_metadata_sync: when the metadata was last synced';

COMMENT ON COLUMN grouper_sync_membership_v.u_member_from_id2 IS 'u_member_from_id2: link data from id2';

COMMENT ON COLUMN grouper_sync_membership_v.u_member_from_id3 IS 'u_member_from_id3: link data from id3';

COMMENT ON COLUMN grouper_sync_membership_v.u_member_to_id2 IS 'u_member_to_id2: link data to id2';

COMMENT ON COLUMN grouper_sync_membership_v.u_member_to_id3 IS 'u_member_to_id3: link data to id3';

COMMENT ON COLUMN grouper_sync_membership_v.u_metadata_updated IS 'u_metadata_updated: when metadata was last updated for entity';

COMMENT ON COLUMN grouper_sync_membership_v.u_last_time_work_was_done IS 'u_last_time_work_was_done: time last work was done on user object';

COMMENT ON COLUMN grouper_sync_membership_v.u_error_message IS 'u_error_message: error message last time work was done on user unless a success happened afterward';

COMMENT ON COLUMN grouper_sync_membership_v.u_error_timestamp IS 'u_error_timestamp: timestamp the last error occurred unless a success happened afterwards';

COMMENT ON COLUMN grouper_sync_membership_v.g_id IS 'g_id: sync group id';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_id IS 'g_group_id: grouper group id';

COMMENT ON COLUMN grouper_sync_membership_v.g_provisionable IS 'g_provisionable: t/f if group is provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.g_in_target IS 'g_in_target: t/f if the group is in target';

COMMENT ON COLUMN grouper_sync_membership_v.g_in_target_insert_or_exists IS 'g_in_target_insert_or_exists: t/f if the group was inserted by grouper or already existed in target';

COMMENT ON COLUMN grouper_sync_membership_v.g_in_target_start IS 'g_in_target_start: when the group was detected to be in the target';

COMMENT ON COLUMN grouper_sync_membership_v.g_in_target_end IS 'g_in_target_end: when the group was detected to not be in the target anymore';

COMMENT ON COLUMN grouper_sync_membership_v.g_provisionable_start IS 'g_provisionable_start: when this group started being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.g_provisionable_end IS 'g_provisionable_end: when this group stopped being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.g_last_updated IS 'g_last_updated: when the sync group was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.g_last_group_sync_start IS 'g_last_group_sync_start: when the group was sync started';

COMMENT ON COLUMN grouper_sync_membership_v.g_last_group_sync IS 'g_last_group_sync: when the group was last synced';

COMMENT ON COLUMN grouper_sync_membership_v.g_last_group_meta_sync_start IS 'g_last_group_meta_sync_start: when the metadata sync started';

COMMENT ON COLUMN grouper_sync_membership_v.g_last_group_metadata_sync IS 'g_last_group_metadata_sync: when the metadata was last synced';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_from_id2 IS 'g_group_from_id2: link data from id2';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_from_id3 IS 'g_group_from_id3: link data from id3';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_to_id2 IS 'g_group_to_id2: link data to id2';

COMMENT ON COLUMN grouper_sync_membership_v.g_group_to_id3 IS 'g_group_to_id3: link data to id3';

COMMENT ON COLUMN grouper_sync_membership_v.g_metadata_updated IS 'g_metadata_updated: when metadata e.g. link data was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.g_error_message IS 'g_error_message: if there is an error message last time work was done it is here';

COMMENT ON COLUMN grouper_sync_membership_v.g_error_timestamp IS 'g_error_timestamp: timestamp if last time work was done there was an error';

COMMENT ON COLUMN grouper_sync_membership_v.g_last_time_work_was_done IS 'g_last_time_work_was_done: timestamp of last time work was done on group';

COMMENT ON COLUMN grouper_sync_membership_v.m_error_code IS 'm_error_code: Error code e.g. ERR error, INV invalid based on script, LEN attribute too large, REQ required attribute missing, DNE data in target does not exist';

COMMENT ON COLUMN grouper_sync_membership_v.u_error_code IS 'u_error_code: Error code e.g. ERR error, INV invalid based on script, LEN attribute too large, REQ required attribute missing, DNE data in target does not exist';

COMMENT ON COLUMN grouper_sync_membership_v.g_error_code IS 'g_error_code: Error code e.g. ERR error, INV invalid based on script, LEN attribute too large, REQ required attribute missing, DNE data in target does not exist';

COMMENT ON TABLE grouper_ddl IS 'holds a record for each database object name, and db version, and java version';

COMMENT ON COLUMN grouper_ddl.id IS 'uuid of this ddl record';

COMMENT ON COLUMN grouper_ddl.object_name IS 'Corresponds to an enum in grouper.ddl package (with Ddl on end), represents one module, so grouper itself is one object';

COMMENT ON COLUMN grouper_ddl.db_version IS 'Version of this object as far as DB knows about';

COMMENT ON COLUMN grouper_ddl.last_updated IS 'last update timestamp, string so it can easily be used from update statement';

COMMENT ON COLUMN grouper_ddl.history IS 'history of this object name, with most recent first (truncated after 4k)';

COMMENT ON TABLE grouper_attr_assign_action IS 'list of actions that are available for attributes';

COMMENT ON COLUMN grouper_attr_assign_action.attribute_def_id IS 'attribute definition foreign key';

COMMENT ON COLUMN grouper_attr_assign_action.context_id IS 'context id in the auditing table';

COMMENT ON COLUMN grouper_attr_assign_action.created_on IS 'number of millis since 1970 when this was created';

COMMENT ON COLUMN grouper_attr_assign_action.id IS 'uuid of this record';

COMMENT ON COLUMN grouper_attr_assign_action.last_updated IS 'number of millis since 1970 when this was created';

COMMENT ON COLUMN grouper_attr_assign_action.name IS 'name of this action';

COMMENT ON COLUMN grouper_attr_assign_action.hibernate_version_number IS 'optimistic locking for grouper updates/deletes';

COMMENT ON TABLE grouper_pit_attr_assn_actn IS 'point in time: list of actions that are available for attributes';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.attribute_def_id IS 'attribute definition foreign key';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.context_id IS 'context id in the auditing table';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.active IS 'T or F for if this row is active, based on start and end time';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.start_time IS 'number of millis since 1970 when this row was inserted';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.end_time IS 'number of millis since 1970 when this row was deleted';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.id IS 'uuid of this record';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.name IS 'name of this action';

COMMENT ON COLUMN grouper_pit_attr_assn_actn.hibernate_version_number IS 'optimistic locking for grouper updates/deletes';

COMMENT ON TABLE grouper_attr_assign_action_set IS 'relationships in action inheritance... e.g. admin action implies read and write actions.  also holds effective relationships';

COMMENT ON COLUMN grouper_attr_assign_action_set.context_id IS 'uuid for the audit table';

COMMENT ON COLUMN grouper_attr_assign_action_set.created_on IS 'millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_attr_assign_action_set.depth IS 'number of hops from one node to another, immediate is one';

COMMENT ON COLUMN grouper_attr_assign_action_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_attr_assign_action_set.if_has_attr_assn_action_id IS 'uuid foreign key of left hand side of this relationship, if you have this action, it implies the then_has action';

COMMENT ON COLUMN grouper_attr_assign_action_set.last_updated IS 'millis since 1970 when this was last updated';

COMMENT ON COLUMN grouper_attr_assign_action_set.parent_attr_assn_action_id IS 'if this is not immediate, then this is the row that puts this relationship n-1 almost there';

COMMENT ON COLUMN grouper_attr_assign_action_set.then_has_attr_assn_action_id IS 'uuid foreign key of the right hand side of this relationship, if you have the if_has action, then you have this one';

COMMENT ON COLUMN grouper_attr_assign_action_set.type IS 'from enum AttributeAssignActionType: self, immediate, effective';

COMMENT ON COLUMN grouper_attr_assign_action_set.hibernate_version_number IS 'hibernate optimistic locking number for updates and deletes';

COMMENT ON TABLE grouper_pit_attr_assn_actn_set IS 'point in time relationships in action inheritance... e.g. admin action implies read and write actions.  also holds effective relationships';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.context_id IS 'uuid for the audit table';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.active IS 'T or F for if this row is active based on start and end times';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.start_time IS 'millis since 1970 that this row was inserted';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.end_time IS 'millis since 1970 that this row was deleted';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.depth IS 'number of hops from one node to another, immediate is one';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.if_has_attr_assn_action_id IS 'uuid foreign key of left hand side of this relationship, if you have this action, it implies the then_has action';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.parent_attr_assn_action_id IS 'if this is not immediate, then this is the row that puts this relationship n-1 almost there';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.then_has_attr_assn_action_id IS 'uuid foreign key of the right hand side of this relationship, if you have the if_has action, then you have this one';

COMMENT ON COLUMN grouper_pit_attr_assn_actn_set.hibernate_version_number IS 'hibernate optimistic locking number for updates and deletes';

COMMENT ON TABLE grouper_attribute_assign IS 'table that assigns an attribute def name to an owner (one of various types), and has an action';

COMMENT ON COLUMN grouper_attribute_assign.attribute_assign_action_id IS 'foreign key to the action which is in this attribute assignment, or permissions, it could be custom, for attributes, it is assign';

COMMENT ON COLUMN grouper_attribute_assign.attribute_assign_delegatable IS 'AttributeAssignDelegatable enum, TRUE, FALSE, or GRANT (can grant to someone else)';

COMMENT ON COLUMN grouper_attribute_assign.attribute_assign_type IS 'AttributeAssignType enum, what is the type of owner: any_mem, any_mem_asgn, attr_def, attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn';

COMMENT ON COLUMN grouper_attribute_assign.attribute_def_name_id IS 'foreign key to the attribute def name is which attribute is assigned';

COMMENT ON COLUMN grouper_attribute_assign.context_id IS 'links this row to an audit record';

COMMENT ON COLUMN grouper_attribute_assign.created_on IS 'number of millis since 1970 when this was created';

COMMENT ON COLUMN grouper_attribute_assign.disabled_time IS 'null if not disabled, or the number of millis since 1970 when this was or will be disabled.  if in the future, Grouper will disable this row at that time.';

COMMENT ON COLUMN grouper_attribute_assign.disallowed IS 'T or F for if disallowed';

COMMENT ON COLUMN grouper_attribute_assign.enabled_time IS 'number of millis since 1970 when this was or will be enabled.  if it future then this row will not be enabled';

COMMENT ON COLUMN grouper_attribute_assign.id IS 'uuid of row';

COMMENT ON COLUMN grouper_attribute_assign.last_updated IS 'millis since 1970 when this row was last updated';

COMMENT ON COLUMN grouper_attribute_assign.notes IS 'notes about this assignment to describe why it exists or anything else, freeform';

COMMENT ON COLUMN grouper_attribute_assign.owner_attribute_assign_id IS 'if this is an assignment on an assignment, then this is the foreign key to this table which is which assignment owns this assignment';

COMMENT ON COLUMN grouper_attribute_assign.owner_attribute_def_id IS 'if this is an assignment on an attribute definition, then this is the foreign key to the attribute definition table';

COMMENT ON COLUMN grouper_attribute_assign.owner_group_id IS 'if this is an assignment on a group or role or effective membership then this is the foreign key to the grouper_groups table';

COMMENT ON COLUMN grouper_attribute_assign.owner_member_id IS 'if this is an assignment on a member or effective membership, then this is the foreign key to the grouper_members table';

COMMENT ON COLUMN grouper_attribute_assign.owner_membership_id IS 'if this is an assignment on an immediate membership, then this is the foreign key to the grouper_memberships table';

COMMENT ON COLUMN grouper_attribute_assign.owner_stem_id IS 'if this is an assignment on a stem aka folder, then this is the foreign key to the grouper_stems table';

COMMENT ON COLUMN grouper_attribute_assign.hibernate_version_number IS 'optimistic locking column for hibernate on updates or deletes';

COMMENT ON COLUMN grouper_attribute_assign.enabled IS 'T or F to indicate if this assignment is enabled';

COMMENT ON TABLE grouper_pit_attribute_assign IS 'point in time table that assigns an attribute def name to an owner (one of various types), and has an action';

COMMENT ON COLUMN grouper_pit_attribute_assign.attribute_assign_action_id IS 'foreign key to the action which is in this attribute assignment, or permissions, it could be custom, for attributes, it is assign';

COMMENT ON COLUMN grouper_pit_attribute_assign.active IS 'T of F for if this row is active or not based on start and end dates';

COMMENT ON COLUMN grouper_pit_attribute_assign.start_time IS 'number of millis since 1970 that this row was inserted';

COMMENT ON COLUMN grouper_pit_attribute_assign.end_time IS 'number of millis since 1970 that this row was deleted';

COMMENT ON COLUMN grouper_pit_attribute_assign.attribute_assign_type IS 'AttributeAssignType enum, what is the type of owner: any_mem, any_mem_asgn, attr_def, attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn';

COMMENT ON COLUMN grouper_pit_attribute_assign.attribute_def_name_id IS 'foreign key to the attribute def name is which attribute is assigned';

COMMENT ON COLUMN grouper_pit_attribute_assign.context_id IS 'links this row to an audit record';

COMMENT ON COLUMN grouper_pit_attribute_assign.disallowed IS 'T or F for if disallowed or not';

COMMENT ON COLUMN grouper_pit_attribute_assign.id IS 'uuid of row';

COMMENT ON COLUMN grouper_pit_attribute_assign.owner_attribute_assign_id IS 'if this is an assignment on an assignment, then this is the foreign key to this table which is which assignment owns this assignment';

COMMENT ON COLUMN grouper_pit_attribute_assign.owner_attribute_def_id IS 'if this is an assignment on an attribute definition, then this is the foreign key to the attribute definition table';

COMMENT ON COLUMN grouper_pit_attribute_assign.owner_group_id IS 'if this is an assignment on a group or role or effective membership then this is the foreign key to the grouper_pit_groups table';

COMMENT ON COLUMN grouper_pit_attribute_assign.owner_member_id IS 'if this is an assignment on a member or effective membership, then this is the foreign key to the grouper_pit_members table';

COMMENT ON COLUMN grouper_pit_attribute_assign.owner_membership_id IS 'if this is an assignment on an immediate membership, then this is the foreign key to the grouper_pit_memberships table';

COMMENT ON COLUMN grouper_pit_attribute_assign.owner_stem_id IS 'if this is an assignment on a stem aka folder, then this is the foreign key to the grouper_pit_stems table';

COMMENT ON COLUMN grouper_pit_attribute_assign.hibernate_version_number IS 'optimistic locking column for hibernate on updates or deletes';

COMMENT ON TABLE grouper_attribute_def IS 'table that holds attribute definitions, which is the first part of the attribute framework';

COMMENT ON COLUMN grouper_attribute_def.assign_to_attribute_def IS 'T or F if you can assign this attribute to an attribute definition';

COMMENT ON COLUMN grouper_attribute_def.assign_to_attribute_def_assn IS 'T or F if you can assign this attribute to an assignment on an attribute definition';

COMMENT ON COLUMN grouper_attribute_def.assign_to_eff_membership IS 'T or F if you you can assign this attribute to an effective membership: group/member pair';

COMMENT ON COLUMN grouper_attribute_def.assign_to_eff_membership_assn IS 'T or F if you can assign this attribute to an effective membership attribute assignment: group/member pair';

COMMENT ON COLUMN grouper_attribute_def.assign_to_group IS 'T or F if you can assign this attribute to a group or role';

COMMENT ON COLUMN grouper_attribute_def.assign_to_group_assn IS 'T or F if you can assign this attribute to an assignment on a group or role';

COMMENT ON COLUMN grouper_attribute_def.assign_to_imm_membership IS 'T or F if you can assign this attribute to an immediate membership';

COMMENT ON COLUMN grouper_attribute_def.assign_to_imm_membership_assn IS 'T or F if you can assign this attribute to an attribute assignment on an immediate membership';

COMMENT ON COLUMN grouper_attribute_def.assign_to_member IS 'T or F if you can assign this attribute to a member';

COMMENT ON COLUMN grouper_attribute_def.assign_to_member_assn IS 'T or F if you can assign this attribute to an assignment on a member';

COMMENT ON COLUMN grouper_attribute_def.assign_to_stem IS 'T or F if you can assign this attribute to a stem/folder';

COMMENT ON COLUMN grouper_attribute_def.assign_to_stem_assn IS 'T or F if you can assign this attribute to an assignment on an attribute definition';

COMMENT ON COLUMN grouper_attribute_def.attribute_def_public IS 'T or F if this is a public attribute';

COMMENT ON COLUMN grouper_attribute_def.attribute_def_type IS 'AttributeDefType enum: attr, domain, type, limit, perm';

COMMENT ON COLUMN grouper_attribute_def.context_id IS 'links back to the grouper audit entry table';

COMMENT ON COLUMN grouper_attribute_def.created_on IS 'number of millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_attribute_def.creator_id IS 'member id of the subject who created this row';

COMMENT ON COLUMN grouper_attribute_def.description IS 'freeform text that describes this attribute definition';

COMMENT ON COLUMN grouper_attribute_def.extension IS 'system name in the folder of this attribute definition';

COMMENT ON COLUMN grouper_attribute_def.id IS 'uuid of this record';

COMMENT ON COLUMN grouper_attribute_def.last_updated IS 'number of millis since 1970 when this row was last updated';

COMMENT ON COLUMN grouper_attribute_def.multi_assignable IS 'T or F if you can assign this attribute to the same owner twice';

COMMENT ON COLUMN grouper_attribute_def.multi_valued IS 'T or F if this assignment can have multiple values';

COMMENT ON COLUMN grouper_attribute_def.name IS 'full system name including system folder names separated by colons';

COMMENT ON COLUMN grouper_attribute_def.stem_id IS 'uuid of the stem/folder where this attribute definition lives';

COMMENT ON COLUMN grouper_attribute_def.value_type IS 'AttributeAssignValueType enum: floating, integerValue, memberId, nullValue, string';

COMMENT ON COLUMN grouper_attribute_def.hibernate_version_number IS 'hibernate version number for optimistic locking during updates and deletes';

COMMENT ON TABLE grouper_pit_attribute_def IS 'point in time table that holds attribute definitions, which is the first part of the attribute framework';

COMMENT ON COLUMN grouper_pit_attribute_def.attribute_def_type IS 'AttributeDefType enum: attr, domain, type, limit, perm';

COMMENT ON COLUMN grouper_pit_attribute_def.context_id IS 'links back to the grouper audit entry table';

COMMENT ON COLUMN grouper_pit_attribute_def.id IS 'uuid of this record';

COMMENT ON COLUMN grouper_pit_attribute_def.name IS 'full system name including system folder names separated by colons';

COMMENT ON COLUMN grouper_pit_attribute_def.stem_id IS 'uuid of the stem/folder where this attribute definition lives';

COMMENT ON COLUMN grouper_pit_attribute_def.hibernate_version_number IS 'hibernate version number for optimistic locking during updates and deletes';

COMMENT ON COLUMN grouper_pit_attribute_def.active IS 'T or F if this row is active based on start and end time';

COMMENT ON COLUMN grouper_pit_attribute_def.start_time IS 'millis since 1970 that this row was inserted';

COMMENT ON COLUMN grouper_pit_attribute_def.end_time IS 'millis since 1970 that this row was deleted';

COMMENT ON TABLE grouper_attribute_def_name IS 'table that holds attribute names, which is the second part of the attribute framework, along with the attribute definition';

COMMENT ON COLUMN grouper_attribute_def_name.attribute_def_id IS 'uuid foreign key links back to the attribute definition';

COMMENT ON COLUMN grouper_attribute_def_name.context_id IS 'uuid that links to the audit entry table';

COMMENT ON COLUMN grouper_attribute_def_name.created_on IS 'number of millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_attribute_def_name.description IS 'freeform description of this attribute name';

COMMENT ON COLUMN grouper_attribute_def_name.display_extension IS 'display name (can change) of this attribute name, not including the stem/folder names';

COMMENT ON COLUMN grouper_attribute_def_name.display_name IS 'display name (can change) of this attribute name, including the display names of folders separated by colons';

COMMENT ON COLUMN grouper_attribute_def_name.extension IS 'system name (should not change often) of this attribute name, not including the stem/folder names';

COMMENT ON COLUMN grouper_attribute_def_name.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_attribute_def_name.last_updated IS 'number of millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_attribute_def_name.name IS 'system name (should not change often) of this attribute name, including the stem/folder system names separated by colons';

COMMENT ON COLUMN grouper_attribute_def_name.stem_id IS 'uuid of the stem where this attribute name lives';

COMMENT ON COLUMN grouper_attribute_def_name.hibernate_version_number IS 'optimistic locking column for this row for updates and deletes';

COMMENT ON TABLE grouper_pit_attr_def_name IS 'point in time table that holds attribute names, which is the second part of the attribute framework, along with the attribute definition';

COMMENT ON COLUMN grouper_pit_attr_def_name.attribute_def_id IS 'uuid foreign key links back to the attribute definition';

COMMENT ON COLUMN grouper_pit_attr_def_name.context_id IS 'uuid that links to the audit entry table';

COMMENT ON COLUMN grouper_pit_attr_def_name.active IS 'T or F if this row is active based on start and end times';

COMMENT ON COLUMN grouper_pit_attr_def_name.start_time IS 'millis since 1970 that this row was inserted';

COMMENT ON COLUMN grouper_pit_attr_def_name.end_time IS 'millis since 1970 that this row was deleted';

COMMENT ON COLUMN grouper_pit_attr_def_name.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_pit_attr_def_name.name IS 'system name (should not change often) of this attribute name, including the stem/folder system names separated by colons';

COMMENT ON COLUMN grouper_pit_attr_def_name.stem_id IS 'uuid of the stem where this attribute name lives';

COMMENT ON COLUMN grouper_pit_attr_def_name.hibernate_version_number IS 'optimistic locking column for this row for updates and deletes';

COMMENT ON TABLE grouper_attribute_def_scope IS 'table that holds rules for where attributes can be assigned (i.e. only to objects in a certain folder etc)';

COMMENT ON COLUMN grouper_attribute_def_scope.attribute_def_id IS 'foreign key to the uuid of the attribute definition: grouper_attribute_def';

COMMENT ON COLUMN grouper_attribute_def_scope.attribute_def_scope_type IS 'AttributeDefScopeType enum: attributeDefNameIdAssigned, idEquals, inStem, nameEquals, nameLike, sourceId';

COMMENT ON COLUMN grouper_attribute_def_scope.context_id IS 'uuid of the audit entry';

COMMENT ON COLUMN grouper_attribute_def_scope.created_on IS 'number of millis since 1970 when this was created';

COMMENT ON COLUMN grouper_attribute_def_scope.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_attribute_def_scope.last_updated IS 'number of millis since 1970 when this attribute was last updated';

COMMENT ON COLUMN grouper_attribute_def_scope.scope_string IS 'describes where this can be assigned depending on the type of this constraint';

COMMENT ON COLUMN grouper_attribute_def_scope.scope_string2 IS 'describes where this can be assigned depending on the type of this constraint';

COMMENT ON COLUMN grouper_attribute_def_scope.hibernate_version_number IS 'optimistic locking column used by hibernate for updates and deletes';

COMMENT ON TABLE grouper_attribute_def_name_set IS 'table that holds immediate and effective relationships for attribute names that are permissions for inheritance... e.g. artsAndSciences implies english';

COMMENT ON COLUMN grouper_attribute_def_name_set.context_id IS 'uuid of the audit entry for the last change of this record';

COMMENT ON COLUMN grouper_attribute_def_name_set.created_on IS 'number of millis since 1970 when this record was created';

COMMENT ON COLUMN grouper_attribute_def_name_set.depth IS 'number of hops from one node to another: 0 is self, 1 is immediate, etc';

COMMENT ON COLUMN grouper_attribute_def_name_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_attribute_def_name_set.if_has_attribute_def_name_id IS 'left hand side of this relationship: if it has this uuid of foreign key of grouper_attribute_def_name then it implies the then_has column';

COMMENT ON COLUMN grouper_attribute_def_name_set.last_updated IS 'number of millis since 1970 when this row was last updated';

COMMENT ON COLUMN grouper_attribute_def_name_set.parent_attr_def_name_set_id IS 'link to the relationship above this one (hops-1)';

COMMENT ON COLUMN grouper_attribute_def_name_set.then_has_attribute_def_name_id IS 'right hand side of this relationship: if it has the if_has then it implies this uuid of the foreign key of the grouper_attribute_def_name';

COMMENT ON COLUMN grouper_attribute_def_name_set.type IS 'AttributeDefAssignmentType enum: effective, immediate, self';

COMMENT ON COLUMN grouper_attribute_def_name_set.hibernate_version_number IS 'column for hibernate optimistic locking for updates and deletes';

COMMENT ON TABLE grouper_pit_attr_def_name_set IS 'point in time: table that holds immediate and effective relationships for attribute names that are permissions for inheritance... e.g. artsAndSciences implies english';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.context_id IS 'uuid of the audit entry for the last change of this record';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.active IS 'T or F if this row is active based on start and end times';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.start_time IS 'number of millis since 1970 when this row was inserted';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.end_time IS 'number of millis since 1970 when this row was deleted';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.depth IS 'number of hops from one node to another: 0 is self, 1 is immediate, etc';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.if_has_attribute_def_name_id IS 'left hand side of this relationship: if it has this uuid of foreign key of grouper_pit_attr_def_name then it implies the then_has column';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.parent_attr_def_name_set_id IS 'link to the relationship above this one (hops-1)';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.then_has_attribute_def_name_id IS 'right hand side of this relationship: if it has the if_has then it implies this uuid of the foreign key of the grouper_pit_attr_def_name';

COMMENT ON COLUMN grouper_pit_attr_def_name_set.hibernate_version_number IS 'column for hibernate optimistic locking for updates and deletes';

COMMENT ON TABLE grouper_attribute_assign_value IS 'value assignment on an attribute assignment';

COMMENT ON COLUMN grouper_attribute_assign_value.attribute_assign_id IS 'foreign key to the attribute assignment grouper_attribute_assign for this assignment';

COMMENT ON COLUMN grouper_attribute_assign_value.context_id IS 'uuid of the audit entry for the last action on this row';

COMMENT ON COLUMN grouper_attribute_assign_value.created_on IS 'number of millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_attribute_assign_value.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_attribute_assign_value.last_updated IS 'number of millis since 1970 when this row was last updated';

COMMENT ON COLUMN grouper_attribute_assign_value.value_floating IS 'if this is a floating type attribute definition, this is the value';

COMMENT ON COLUMN grouper_attribute_assign_value.value_integer IS 'if this is an integer type attribute definition, this is the value';

COMMENT ON COLUMN grouper_attribute_assign_value.value_member_id IS 'if this is a member type attribute definition, this is the value';

COMMENT ON COLUMN grouper_attribute_assign_value.value_string IS 'if this is a string type attribute definition, this is the value';

COMMENT ON COLUMN grouper_attribute_assign_value.hibernate_version_number IS 'hibernate optimistic locking column for updates and deletes';

COMMENT ON TABLE grouper_pit_attr_assn_value IS 'point in time history of value assignment on an attribute assignment';

COMMENT ON COLUMN grouper_pit_attr_assn_value.attribute_assign_id IS 'foreign key to the attribute assignment grouper_pit_attribute_assign for this assignment';

COMMENT ON COLUMN grouper_pit_attr_assn_value.context_id IS 'uuid of the audit entry for the last action on this row';

COMMENT ON COLUMN grouper_pit_attr_assn_value.start_time IS 'number of millis since 1970 when this row started in point in time';

COMMENT ON COLUMN grouper_pit_attr_assn_value.end_time IS 'number of millis since 1970 when this row ended in point in time';

COMMENT ON COLUMN grouper_pit_attr_assn_value.active IS 'T or F if this is an active record based on start and end dates';

COMMENT ON COLUMN grouper_pit_attr_assn_value.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_pit_attr_assn_value.end_time IS 'number of millis since 1970 when this row row ended in point in time';

COMMENT ON COLUMN grouper_pit_attr_assn_value.value_floating IS 'if this is a floating type attribute definition, this is the value';

COMMENT ON COLUMN grouper_pit_attr_assn_value.value_integer IS 'if this is an integer type attribute definition, this is the value';

COMMENT ON COLUMN grouper_pit_attr_assn_value.value_member_id IS 'if this is a member type attribute definition, this is the value';

COMMENT ON COLUMN grouper_pit_attr_assn_value.value_string IS 'if this is a string type attribute definition, this is the value';

COMMENT ON COLUMN grouper_pit_attr_assn_value.hibernate_version_number IS 'hibernate optimistic locking column for updates and deletes';

COMMENT ON TABLE grouper_change_log_type IS 'type of this change log entry, e.g. an insert into grouper groups';

COMMENT ON COLUMN grouper_change_log_type.action_name IS 'action name, e.g. addGroup, deleteMember';

COMMENT ON COLUMN grouper_change_log_type.change_log_category IS 'action category, e.g. group, member';

COMMENT ON COLUMN grouper_change_log_type.context_id IS 'uuid of the change log entry for this row';

COMMENT ON COLUMN grouper_change_log_type.created_on IS 'number of millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_change_log_type.hibernate_version_number IS 'hibernate version for optimistic locking for updates and deletes';

COMMENT ON COLUMN grouper_change_log_type.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_change_log_type.label_string01 IS 'label of the 01 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string02 IS 'label of the 02 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string03 IS 'label of the 03 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string04 IS 'label of the 04 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string05 IS 'label of the 05 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string06 IS 'label of the 06 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string07 IS 'label of the 07 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string08 IS 'label of the 08 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string09 IS 'label of the 09 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string10 IS 'label of the 10 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string11 IS 'label of the 11 string entry';

COMMENT ON COLUMN grouper_change_log_type.label_string12 IS 'label of the 12 string entry';

COMMENT ON COLUMN grouper_change_log_type.last_updated IS 'number of millis since 1970 when this row was last changed';

COMMENT ON TABLE grouper_change_log_consumer IS 'table keeps track of change log consumers so if they stop, they will start at the place where they left off in processing change log entries';

COMMENT ON COLUMN grouper_change_log_consumer.created_on IS 'number of millis since 1970 when this record was created';

COMMENT ON COLUMN grouper_change_log_consumer.hibernate_version_number IS 'hibernate optimistic locking versioning column for updates and deletes';

COMMENT ON COLUMN grouper_change_log_consumer.id IS 'UUID of this row';

COMMENT ON COLUMN grouper_change_log_consumer.last_sequence_processed IS 'index of the change log row which was last processed by this consumer';

COMMENT ON COLUMN grouper_change_log_consumer.last_updated IS 'millis since 1970 that this row was last updated';

COMMENT ON COLUMN grouper_change_log_consumer.name IS 'name of the consumer';

COMMENT ON TABLE grouper_change_log_entry_temp IS 'rows are inserted here in the transaction of the actual action, e.g. an add member.  The change log daemon will move records from here to the change log entry table in order, to be processed by consumers';

COMMENT ON COLUMN grouper_change_log_entry_temp.change_log_type_id IS 'foreign key to the grouper_change_log_type table';

COMMENT ON COLUMN grouper_change_log_entry_temp.context_id IS 'uuid to the grouper_audit_entry table';

COMMENT ON COLUMN grouper_change_log_entry_temp.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_change_log_entry_temp.string01 IS 'value of the string for value 01 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string02 IS 'value of the string for value 02 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string03 IS 'value of the string for value 03 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string04 IS 'value of the string for value 04 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string05 IS 'value of the string for value 05 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string06 IS 'value of the string for value 06 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string07 IS 'value of the string for value 07 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string08 IS 'value of the string for value 08 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string09 IS 'value of the string for value 09 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string10 IS 'value of the string for value 10 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string11 IS 'value of the string for value 11 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.string12 IS 'value of the string for value 12 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry_temp.created_on IS 'number of thousandths of millis from 1970 when this row was created';

COMMENT ON TABLE grouper_change_log_entry IS 'The change log daemon will move records from grouper_change_log_entry_temp to this table in time order, to be processed by consumers';

COMMENT ON COLUMN grouper_change_log_entry.change_log_type_id IS 'foreign key to the grouper_change_log_type table, the type of action';

COMMENT ON COLUMN grouper_change_log_entry.context_id IS 'uuid referencing the grouper_audit_entry table';

COMMENT ON COLUMN grouper_change_log_entry.created_on IS 'number of thousandths of millis from 1970 when this row was created';

COMMENT ON COLUMN grouper_change_log_entry.sequence_number IS 'integer which is in order which these records should be processed by change log consumers';

COMMENT ON COLUMN grouper_change_log_entry.string01 IS 'value of the string for value 01 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string02 IS 'value of the string for value 02 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string03 IS 'value of the string for value 03 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string04 IS 'value of the string for value 04 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string05 IS 'value of the string for value 05 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string06 IS 'value of the string for value 06 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string07 IS 'value of the string for value 07 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string08 IS 'value of the string for value 08 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string09 IS 'value of the string for value 09 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string10 IS 'value of the string for value 10 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string11 IS 'value of the string for value 11 which corresponds to the grouper_change_log_type table label';

COMMENT ON COLUMN grouper_change_log_entry.string12 IS 'value of the string for value 12 which corresponds to the grouper_change_log_type table label';

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

COMMENT ON TABLE grouper_group_set IS 'This table holds relationships for memberships or privileges on groups, stems, attributes.  This allows quick joining of who is in a group effectively';

COMMENT ON COLUMN grouper_group_set.context_id IS 'uuid that links to the grouper_audit_entry';

COMMENT ON COLUMN grouper_group_set.create_time IS 'number of millis since 1970 that this row was created';

COMMENT ON COLUMN grouper_group_set.creator_id IS 'uuid of grouper_members of who created this row';

COMMENT ON COLUMN grouper_group_set.depth IS '0 for self, 1 for immediate, or more for effective.  this is the number of hops between nodes';

COMMENT ON COLUMN grouper_group_set.field_id IS 'uuid foreign key from grouper_fields which is the list of the membership, normally members or the privilege in question';

COMMENT ON COLUMN grouper_group_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_group_set.member_attr_def_id IS 'foreign key of grouper_attribute_def of the member record';

COMMENT ON COLUMN grouper_group_set.member_field_id IS 'uuid foreign key from grouper_fields which is the list of the membership, normally members';

COMMENT ON COLUMN grouper_group_set.member_group_id IS 'uuid to the grouper_groups table which is the group that is a member of the owner';

COMMENT ON COLUMN grouper_group_set.member_id IS 'whether this is groups, stems, or attribute definitions, this is the member';

COMMENT ON COLUMN grouper_group_set.member_stem_id IS 'uuid to the grouper_stems table which is the stem that is implied by the owner';

COMMENT ON COLUMN grouper_group_set.mship_type IS 'MembershipType enum, effective or immediate';

COMMENT ON COLUMN grouper_group_set.owner_attr_def_id IS 'uuid to the grouper_attribute_def table which is the owner of this record, which implies a relationship to the member, if null, it will have (NULL) which helps with some DB vendors';

COMMENT ON COLUMN grouper_group_set.owner_attr_def_id_null IS 'uuid to the grouper_attribute_def table which is the owner of this record, which implies a relationship to the member, if null, it will be null';

COMMENT ON COLUMN grouper_group_set.owner_group_id IS 'uuid to the grouper_groups table which is the owner of this record, which implies a membership to the member uuid, if null, it will be (NULL) which helps with some DB vendors';

COMMENT ON COLUMN grouper_group_set.owner_group_id_null IS 'uuid to the grouper_groups table which is the owner of this record, which implies a membership to the member uuid, if null, it will be null';

COMMENT ON COLUMN grouper_group_set.owner_id IS 'whether this is ';

COMMENT ON COLUMN grouper_group_set.owner_stem_id IS 'uuid to the grouper_stems table which is the owner of this record, which implies a privilege to the member uuid, if null, it will be (NULL) which helps with some DB vendors';

COMMENT ON COLUMN grouper_group_set.owner_stem_id_null IS 'uuid to the grouper_stems table which is the owner of this record, which implies a privilege to the member uuid, if null, it will be null';

COMMENT ON COLUMN grouper_group_set.parent_id IS 'this is the link back to the grouper_group_set table which is the one one hop away and related to this one...';

COMMENT ON COLUMN grouper_group_set.via_group_id IS 'same as member_group_id if depth is not 0 otherwise null';

COMMENT ON COLUMN grouper_group_set.hibernate_version_number IS 'optimistic locking column for hibernate used for updates and deletes';

COMMENT ON TABLE grouper_pit_group_set IS 'point in time: This table holds relationships for memberships or privileges on groups, stems, attributes.  This allows quick joining of who is in a group effectively';

COMMENT ON COLUMN grouper_pit_group_set.context_id IS 'uuid that links to the grouper_audit_entry';

COMMENT ON COLUMN grouper_pit_group_set.active IS 'T or F for if this is active, based on start and end time';

COMMENT ON COLUMN grouper_pit_group_set.start_time IS 'number of millis since 1970 that this row was created';

COMMENT ON COLUMN grouper_pit_group_set.end_time IS 'number of millis since 1970 that this row was deleted';

COMMENT ON COLUMN grouper_pit_group_set.depth IS '0 for self, 1 for immediate, or more for effective.  this is the number of hops between nodes';

COMMENT ON COLUMN grouper_pit_group_set.field_id IS 'uuid foreign key from grouper_pit_fields which is the list of the membership, normally members or the privilege in question';

COMMENT ON COLUMN grouper_pit_group_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_pit_group_set.member_attr_def_id IS 'foreign key of grouper_pit_attribute_def of the member record';

COMMENT ON COLUMN grouper_pit_group_set.member_field_id IS 'uuid foreign key from grouper_pit_fields which is the list of the membership, normally members';

COMMENT ON COLUMN grouper_pit_group_set.member_group_id IS 'uuid to the grouper_pit_groups table which is the group that is a member of the owner';

COMMENT ON COLUMN grouper_pit_group_set.member_id IS 'whether this is groups, stems, or attribute definitions, this is the member';

COMMENT ON COLUMN grouper_pit_group_set.member_stem_id IS 'uuid to the grouper_pit_stems table which is the stem that is implied by the owner';

COMMENT ON COLUMN grouper_pit_group_set.owner_attr_def_id IS 'uuid to the grouper_pit_attribute_def table which is the owner of this record, which implies a relationship to the member, if null, it will have (NULL) which helps with some DB vendors';

COMMENT ON COLUMN grouper_pit_group_set.owner_group_id IS 'uuid to the grouper_pit_groups table which is the owner of this record, which implies a membership to the member uuid, if null, it will be (NULL) which helps with some DB vendors';

COMMENT ON COLUMN grouper_pit_group_set.owner_id IS 'this is the owner id regardless of the type of owner';

COMMENT ON COLUMN grouper_pit_group_set.owner_stem_id IS 'uuid to the grouper_pit_stems table which is the owner of this record, which implies a privilege to the member uuid, if null, it will be (NULL) which helps with some DB vendors';

COMMENT ON COLUMN grouper_pit_group_set.parent_id IS 'this is the link back to the grouper_pit_group_set table which is the one one hop away and related to this one...';

COMMENT ON COLUMN grouper_pit_group_set.hibernate_version_number IS 'optimistic locking column for hibernate used for updates and deletes';

COMMENT ON TABLE grouper_role_set IS 'This table holds relationships between roles if one role inherits permissions from another role';

COMMENT ON COLUMN grouper_role_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_role_set.context_id IS 'links to the grouper_audit_entry for the last change of this row';

COMMENT ON COLUMN grouper_role_set.created_on IS 'millis since 1970 that this row was created';

COMMENT ON COLUMN grouper_role_set.depth IS 'number of hops across the relationship, 0 means self, 1 is immediate, more is effective';

COMMENT ON COLUMN grouper_role_set.if_has_role_id IS 'this is the foreign key uuid in grouper_groups where if the user has this role then they get the permissions assigned to another role then_has';

COMMENT ON COLUMN grouper_role_set.last_updated IS 'millis since 1970 when this row was last updated';

COMMENT ON COLUMN grouper_role_set.parent_role_set_id IS 'this is the foreign key to the uuid in this table grouper_role_set which is the next closest to the underlying assignment';

COMMENT ON COLUMN grouper_role_set.then_has_role_id IS 'this is the foreign key uuid in grouper_gropus where if the user has the if_has role then the user gets the permissions assigned to this then_has role';

COMMENT ON COLUMN grouper_role_set.type IS 'RoleHierarchyType enum: self, immediate, effective';

COMMENT ON COLUMN grouper_role_set.hibernate_version_number IS 'optimistic logging integer used by hibernate during updates and deletes';

COMMENT ON TABLE grouper_pit_role_set IS 'point in time: This table holds relationships between roles if one role inherits permissions from another role';

COMMENT ON COLUMN grouper_pit_role_set.context_id IS 'links to the grouper_audit_entry for the last change of this row';

COMMENT ON COLUMN grouper_pit_role_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_pit_role_set.active IS 'T or F for if this row is active based on start time and end time';

COMMENT ON COLUMN grouper_pit_role_set.start_time IS 'number of millis since 1970 that this row was inserted';

COMMENT ON COLUMN grouper_pit_role_set.end_time IS 'number of millis since 1970 that this row was deleted';

COMMENT ON COLUMN grouper_pit_role_set.depth IS 'number of hops across the relationship, 0 means self, 1 is immediate, more is effective';

COMMENT ON COLUMN grouper_pit_role_set.if_has_role_id IS 'this is the foreign key uuid in grouper_pit_groups where if the user has this role then they get the permissions assigned to another role then_has';

COMMENT ON COLUMN grouper_pit_role_set.parent_role_set_id IS 'this is the foreign key to the uuid in this table grouper_pit_role_set which is the next closest to the underlying assignment';

COMMENT ON COLUMN grouper_pit_role_set.then_has_role_id IS 'this is the foreign key uuid in grouper_pit_gropus where if the user has the if_has role then the user gets the permissions assigned to this then_has role';

COMMENT ON COLUMN grouper_pit_role_set.hibernate_version_number IS 'optimistic logging integer used by hibernate during updates and deletes';

COMMENT ON TABLE grouper_audit_entry IS 'holds one record for each audit entry record which is a high level action that ties together lower level actions';

COMMENT ON COLUMN grouper_audit_entry.act_as_member_id IS 'Member id (foreign key) of the user who is being acted as';

COMMENT ON COLUMN grouper_audit_entry.audit_type_id IS 'foreign key to the grouper_audit_type table which is the type of this entry';

COMMENT ON COLUMN grouper_audit_entry.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_audit_entry.created_on IS 'When this audit entry record was created';

COMMENT ON COLUMN grouper_audit_entry.description IS 'Description is a sentence form expression of what is being audited';

COMMENT ON COLUMN grouper_audit_entry.env_name IS 'environment label of the system running, from grouper.properties';

COMMENT ON COLUMN grouper_audit_entry.grouper_engine IS 'Grouper engine is e.g. UI, WS, GSH, loader, etc';

COMMENT ON COLUMN grouper_audit_entry.grouper_version IS 'Grouper version of the API executing';

COMMENT ON COLUMN grouper_audit_entry.hibernate_version_number IS 'hibernate version number keeps track of if multiple sessions step on toes';

COMMENT ON COLUMN grouper_audit_entry.id IS 'db id of this audit entry record';

COMMENT ON COLUMN grouper_audit_entry.int01 IS 'The int 01 value';

COMMENT ON COLUMN grouper_audit_entry.int02 IS 'The int 02 value';

COMMENT ON COLUMN grouper_audit_entry.int03 IS 'The int 03 value';

COMMENT ON COLUMN grouper_audit_entry.int04 IS 'The int 04 value';

COMMENT ON COLUMN grouper_audit_entry.int05 IS 'The int 05 value';

COMMENT ON COLUMN grouper_audit_entry.last_updated IS 'When this audit entry was last updated';

COMMENT ON COLUMN grouper_audit_entry.logged_in_member_id IS 'Member id (foreign key) of the user logged in';

COMMENT ON COLUMN grouper_audit_entry.server_host IS 'Host of the system running the grouper API';

COMMENT ON COLUMN grouper_audit_entry.string01 IS 'The string 01 value';

COMMENT ON COLUMN grouper_audit_entry.string02 IS 'The string 02 value';

COMMENT ON COLUMN grouper_audit_entry.string03 IS 'The string 03 value';

COMMENT ON COLUMN grouper_audit_entry.string04 IS 'The string 04 value';

COMMENT ON COLUMN grouper_audit_entry.string05 IS 'The string 05 value';

COMMENT ON COLUMN grouper_audit_entry.string06 IS 'The string 06 value';

COMMENT ON COLUMN grouper_audit_entry.string07 IS 'The string 07 value';

COMMENT ON COLUMN grouper_audit_entry.string08 IS 'The string 08 value';

COMMENT ON COLUMN grouper_audit_entry.user_ip_address IS 'IP address of the user connecting to the system (e.g. from UI or WS)';

COMMENT ON COLUMN grouper_audit_entry.duration_microseconds IS 'Duration of the context, in microseconds (millionths of a second)';

COMMENT ON COLUMN grouper_audit_entry.query_count IS 'Number of database queries required for this context';

COMMENT ON COLUMN grouper_audit_entry.server_user_name IS 'Username of the OS user running the API.  This might identify who ran a GSH call';

COMMENT ON TABLE grouper_audit_type IS 'audit type is a category and an action that organizes audits.  Also holds labels for all the misc string and int fields';

COMMENT ON COLUMN grouper_audit_type.action_name IS 'The action in this audit category to differentiate from others';

COMMENT ON COLUMN grouper_audit_type.audit_category IS 'The category of this audit in logical grouping';

COMMENT ON COLUMN grouper_audit_type.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_audit_type.created_on IS 'When this audit type was created';

COMMENT ON COLUMN grouper_audit_type.hibernate_version_number IS 'Hibernate version number makes sure multiple sessions do not step on toes';

COMMENT ON COLUMN grouper_audit_type.id IS 'Unique id of this audit entry';

COMMENT ON COLUMN grouper_audit_type.label_int01 IS 'The int 01 value';

COMMENT ON COLUMN grouper_audit_type.label_int02 IS 'The int 02 value';

COMMENT ON COLUMN grouper_audit_type.label_int03 IS 'The int 03 value';

COMMENT ON COLUMN grouper_audit_type.label_int04 IS 'The int 04 value';

COMMENT ON COLUMN grouper_audit_type.label_int05 IS 'The int 05 value';

COMMENT ON COLUMN grouper_audit_type.label_string01 IS 'The label of the string field 01 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string02 IS 'The label of the string field 02 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string03 IS 'The label of the string field 03 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string04 IS 'The label of the string field 04 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string05 IS 'The label of the string field 05 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string06 IS 'The label of the string field 06 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string07 IS 'The label of the string field 07 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.label_string08 IS 'The label of the string field 08 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_type.last_updated IS 'When this audit type was last updated';

COMMENT ON TABLE grouper_composites IS 'records the composite group, and its factors';

COMMENT ON COLUMN grouper_composites.id IS 'db id of this composite record';

COMMENT ON COLUMN grouper_composites.owner IS 'group uuid of the composite group';

COMMENT ON COLUMN grouper_composites.left_factor IS 'left factor of the composite group';

COMMENT ON COLUMN grouper_composites.right_factor IS 'right factor of the composite group';

COMMENT ON COLUMN grouper_composites.type IS 'e.g. union, complement, intersection';

COMMENT ON COLUMN grouper_composites.creator_id IS 'member uuid of who created this';

COMMENT ON COLUMN grouper_composites.create_time IS 'number of millis since 1970 until when created';

COMMENT ON COLUMN grouper_composites.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_composites.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_ext_subj IS 'external subjects stored in grouper';

COMMENT ON COLUMN grouper_ext_subj.context_id IS 'context id links back to an auditing record';

COMMENT ON COLUMN grouper_ext_subj.create_time IS 'when this record was created in millis from 1970';

COMMENT ON COLUMN grouper_ext_subj.creator_member_id IS 'member id of who created this record';

COMMENT ON COLUMN grouper_ext_subj.description IS 'description field of the subject object';

COMMENT ON COLUMN grouper_ext_subj.email IS 'email address of subject (optional)';

COMMENT ON COLUMN grouper_ext_subj.identifier IS 'identifier of subject, e.g. the eppn.';

COMMENT ON COLUMN grouper_ext_subj.institution IS 'institution name where the subject is from';

COMMENT ON COLUMN grouper_ext_subj.modifier_member_id IS 'member id of who last edited the record';

COMMENT ON COLUMN grouper_ext_subj.modify_time IS 'when the record was last modified';

COMMENT ON COLUMN grouper_ext_subj.name IS 'name field of the subject object';

COMMENT ON COLUMN grouper_ext_subj.search_string_lower IS 'subject searches will use this field, it should contain most of the other fields, lower case';

COMMENT ON COLUMN grouper_ext_subj.uuid IS 'unique identifier for row';

COMMENT ON COLUMN grouper_ext_subj.enabled IS 'T or F for if this subject is enabled';

COMMENT ON COLUMN grouper_ext_subj.disabled_time IS 'number of millis since 1970 when this row was disabled';

COMMENT ON COLUMN grouper_ext_subj.hibernate_version_number IS 'hibernate optimistic locking value for updates and deletes';

COMMENT ON COLUMN grouper_ext_subj.vetted_email_addresses IS 'comma separated email addresses that this user has responded to';

COMMENT ON TABLE grouper_ext_subj_attr IS 'external subjects stored in grouper';

COMMENT ON COLUMN grouper_ext_subj_attr.attribute_system_name IS 'system name of the attribute, should not change, used as column name in view';

COMMENT ON COLUMN grouper_ext_subj_attr.attribute_value IS 'value of the attribute';

COMMENT ON COLUMN grouper_ext_subj_attr.context_id IS 'context id links back to an auditing record';

COMMENT ON COLUMN grouper_ext_subj_attr.create_time IS 'when this record was created in millis from 1970';

COMMENT ON COLUMN grouper_ext_subj_attr.creator_member_id IS 'member id of who created this record';

COMMENT ON COLUMN grouper_ext_subj_attr.modifier_member_id IS 'member id of who last edited the record';

COMMENT ON COLUMN grouper_ext_subj_attr.modify_time IS 'when the record was last modified';

COMMENT ON COLUMN grouper_ext_subj_attr.subject_uuid IS 'foreign key back to external subject';

COMMENT ON COLUMN grouper_ext_subj_attr.uuid IS 'unique identifier for row';

COMMENT ON COLUMN grouper_ext_subj_attr.hibernate_version_number IS 'hibernate optimistic locking value for updates and deletes';

COMMENT ON TABLE grouper_fields IS 'describes fields related to types';

COMMENT ON COLUMN grouper_fields.id IS 'db id of this field record';

COMMENT ON COLUMN grouper_fields.hibernate_version_number IS 'hibernate optimistic locking version number for updates and deletes';

COMMENT ON COLUMN grouper_fields.name IS 'name of the field';

COMMENT ON COLUMN grouper_fields.read_privilege IS 'which privilege is required to read this field';

COMMENT ON COLUMN grouper_fields.type IS 'type of field (e.g. attribute, list, access, naming)';

COMMENT ON COLUMN grouper_fields.write_privilege IS 'which privilege is required to write this attribute';

COMMENT ON COLUMN grouper_fields.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_pit_fields IS 'point in time history that describes fields related to types';

COMMENT ON COLUMN grouper_pit_fields.id IS 'db id of this field record';

COMMENT ON COLUMN grouper_pit_fields.active IS 'T or F if this record is currently active';

COMMENT ON COLUMN grouper_pit_fields.start_time IS 'number of millis since 1970 that this record was inserted';

COMMENT ON COLUMN grouper_pit_fields.end_time IS 'number of millis since 1970 that this record was deleted';

COMMENT ON COLUMN grouper_pit_fields.hibernate_version_number IS 'hibernate optimistic locking id for updates and deletes';

COMMENT ON COLUMN grouper_pit_fields.name IS 'name of the field';

COMMENT ON COLUMN grouper_pit_fields.type IS 'type of field (e.g. attribute, list, access, naming)';

COMMENT ON COLUMN grouper_pit_fields.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_groups IS 'holds the groups in the grouper system';

COMMENT ON COLUMN grouper_groups.id IS 'db id of this group record';

COMMENT ON COLUMN grouper_groups.parent_stem IS 'uuid of the stem that this group refers to';

COMMENT ON COLUMN grouper_groups.creator_id IS 'member uuid of the creator of this group';

COMMENT ON COLUMN grouper_groups.create_time IS 'number of millis since 1970 that this group was created';

COMMENT ON COLUMN grouper_groups.modifier_id IS 'member uuid of the last modifier of this group';

COMMENT ON COLUMN grouper_groups.modify_time IS 'number of millis since 1970 that this group was modified';

COMMENT ON COLUMN grouper_groups.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_groups.name IS 'group name is the fully qualified extension of group and all parent stems.  It shouldnt change much, and can be used to reference group from external systems';

COMMENT ON COLUMN grouper_groups.display_name IS 'group display name is the fully qualified display extension of group and all parent stems.  It can change as needed, and can not be used to reference group from external systems';

COMMENT ON COLUMN grouper_groups.extension IS 'group extension is the label for this group inside a stem.  It shouldnt change much, and can be used to reference group from external systems (in conjunction with parent stem id)';

COMMENT ON COLUMN grouper_groups.display_extension IS 'group display extension is the display label for this group inside a stem.  It cant change as needed, and can not be used to reference group from external systems';

COMMENT ON COLUMN grouper_groups.description IS 'group description is an optional text blurb that can be used to describe the group';

COMMENT ON COLUMN grouper_groups.last_membership_change IS 'If configured to keep track, this is the last membership change for this group';

COMMENT ON COLUMN grouper_groups.last_imm_membership_change IS 'If configured to keep track, this is the last immediate membership change for this group';

COMMENT ON COLUMN grouper_groups.alternate_name IS 'An alternate name for this group';

COMMENT ON COLUMN grouper_groups.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_groups.type_of_group IS 'if this is a group or role';

COMMENT ON TABLE grouper_pit_groups IS 'point in time info about groups in the grouper system';

COMMENT ON COLUMN grouper_pit_groups.id IS 'db id of this group record';

COMMENT ON COLUMN grouper_pit_groups.start_time IS 'millis since 1970 when this record was inserted';

COMMENT ON COLUMN grouper_pit_groups.end_time IS 'millis since 1970 when this record was deleted';

COMMENT ON COLUMN grouper_pit_groups.active IS 'T or F if this record is currently active';

COMMENT ON COLUMN grouper_pit_groups.stem_id IS 'uuid of the stem that this group refers to';

COMMENT ON COLUMN grouper_pit_groups.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_pit_groups.name IS 'group name is the fully qualified extension of group and all parent stems.  It shouldnt change much, and can be used to reference group from external systems';

COMMENT ON COLUMN grouper_pit_groups.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_members IS 'keeps track of subjects used in grouper.  Records are never deleted from this table';

COMMENT ON COLUMN grouper_members.id IS 'db id of this row';

COMMENT ON COLUMN grouper_members.subject_id IS 'subject id is the id from the subject source';

COMMENT ON COLUMN grouper_members.subject_source IS 'id of the source from subject.properties';

COMMENT ON COLUMN grouper_members.subject_type IS 'type of subject, e.g. person';

COMMENT ON COLUMN grouper_members.sort_string0 IS 'string that can be used to sort results';

COMMENT ON COLUMN grouper_members.sort_string1 IS 'string that can be used to sort results';

COMMENT ON COLUMN grouper_members.sort_string2 IS 'string that can be used to sort results';

COMMENT ON COLUMN grouper_members.sort_string3 IS 'string that can be used to sort results';

COMMENT ON COLUMN grouper_members.sort_string4 IS 'string that can be used to sort results';

COMMENT ON COLUMN grouper_members.search_string0 IS 'string that can be used to filter results';

COMMENT ON COLUMN grouper_members.search_string1 IS 'string that can be used to filter results';

COMMENT ON COLUMN grouper_members.search_string2 IS 'string that can be used to filter results';

COMMENT ON COLUMN grouper_members.search_string3 IS 'string that can be used to filter results';

COMMENT ON COLUMN grouper_members.search_string4 IS 'string that can be used to filter results';

COMMENT ON COLUMN grouper_members.name IS 'name of subject';

COMMENT ON COLUMN grouper_members.description IS 'description of subject';

COMMENT ON COLUMN grouper_members.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_members.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_pit_members IS 'keeps track of subjects used in grouper.  Records are never deleted from this table';

COMMENT ON COLUMN grouper_pit_members.id IS 'db id of this row';

COMMENT ON COLUMN grouper_pit_members.subject_id IS 'subject id is the id from the subject source';

COMMENT ON COLUMN grouper_pit_members.subject_source IS 'id of the source from subject.properties';

COMMENT ON COLUMN grouper_pit_members.subject_type IS 'type of subject, e.g. person';

COMMENT ON COLUMN grouper_pit_members.active IS 'T or F if this is an active record based on start and end dates';

COMMENT ON COLUMN grouper_pit_members.start_time IS 'millis from 1970 when this record was inserted';

COMMENT ON COLUMN grouper_pit_members.end_time IS 'millis from 1970 when this record was deleted';

COMMENT ON COLUMN grouper_pit_members.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_pit_members.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_memberships IS 'keeps track of memberships and permissions';

COMMENT ON COLUMN grouper_memberships.id IS 'db id of this row';

COMMENT ON COLUMN grouper_memberships.owner_group_id IS 'group of the membership if applicable';

COMMENT ON COLUMN grouper_memberships.owner_stem_id IS 'stem of the membership if applicable';

COMMENT ON COLUMN grouper_memberships.member_id IS 'member of the memership';

COMMENT ON COLUMN grouper_memberships.owner_id IS 'owner of the memership';

COMMENT ON COLUMN grouper_memberships.field_id IS 'foreign key to field by id';

COMMENT ON COLUMN grouper_memberships.mship_type IS 'type of membership, immediate or composite';

COMMENT ON COLUMN grouper_memberships.via_composite_id IS 'for composite, this is the composite uuid';

COMMENT ON COLUMN grouper_memberships.creator_id IS 'member uuid of the creator of this record';

COMMENT ON COLUMN grouper_memberships.create_time IS 'number of millis since 1970 that this record was created';

COMMENT ON COLUMN grouper_memberships.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_memberships.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_memberships.enabled IS 'T or F to indicate if the membership is enabled';

COMMENT ON COLUMN grouper_memberships.enabled_timestamp IS 'When the membership will be enabled if the time is in the future.';

COMMENT ON COLUMN grouper_memberships.disabled_timestamp IS 'When the membership will be disabled if the time is in the future.';

COMMENT ON COLUMN grouper_memberships.owner_attr_def_id IS 'For attribute definition privileges, this is the foreign key to the grouper_attribute_def table';

COMMENT ON TABLE grouper_pit_memberships IS 'keeps track of memberships and permissions';

COMMENT ON COLUMN grouper_pit_memberships.id IS 'db id of this row';

COMMENT ON COLUMN grouper_pit_memberships.owner_group_id IS 'group of the membership if applicable';

COMMENT ON COLUMN grouper_pit_memberships.owner_stem_id IS 'stem of the membership if applicable';

COMMENT ON COLUMN grouper_pit_memberships.owner_attr_def_id IS 'attribute def of the membership if applicable';

COMMENT ON COLUMN grouper_pit_memberships.member_id IS 'member of the memership';

COMMENT ON COLUMN grouper_pit_memberships.owner_id IS 'owner of the memership';

COMMENT ON COLUMN grouper_pit_memberships.field_id IS 'foreign key to field by id';

COMMENT ON COLUMN grouper_pit_memberships.active IS 'T or F if this row is active based on start_time and end_time';

COMMENT ON COLUMN grouper_pit_memberships.start_time IS 'number of millis since 1970 when this record was inserted';

COMMENT ON COLUMN grouper_pit_memberships.end_time IS 'number of millis since 1970 when this record was deleted';

COMMENT ON COLUMN grouper_pit_memberships.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_pit_memberships.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_group_set IS 'keeps track of the set of immediate and effective group members for all groups and stems';

COMMENT ON COLUMN grouper_group_set.id IS 'db id of this row';

COMMENT ON COLUMN grouper_group_set.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_group_set.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_group_set.field_id IS 'field represented by this group set';

COMMENT ON COLUMN grouper_group_set.mship_type IS 'type of membership represented by this group set, immediate or composite or effective';

COMMENT ON COLUMN grouper_group_set.via_group_id IS 'same as member_group_id if depth is greater than 0, otherwise null.';

COMMENT ON COLUMN grouper_group_set.depth IS 'number of hops in directed graph';

COMMENT ON COLUMN grouper_group_set.parent_id IS 'parent group set';

COMMENT ON COLUMN grouper_group_set.creator_id IS 'member uuid of the creator of this record';

COMMENT ON COLUMN grouper_group_set.create_time IS 'number of millis since 1970 that this record was created';

COMMENT ON COLUMN grouper_group_set.owner_id IS 'owner id';

COMMENT ON COLUMN grouper_group_set.owner_attr_def_id IS 'owner attr def if applicable';

COMMENT ON COLUMN grouper_group_set.owner_attr_def_id_null IS 'same as owner_attr_def_id except nulls are replaced with the string <NULL>';

COMMENT ON COLUMN grouper_group_set.owner_group_id IS 'owner group if applicable';

COMMENT ON COLUMN grouper_group_set.owner_group_id_null IS 'same as owner_group_id except nulls are replaced with the string <NULL>';

COMMENT ON COLUMN grouper_group_set.owner_stem_id IS 'owner stem if applicable';

COMMENT ON COLUMN grouper_group_set.owner_stem_id_null IS 'same as owner_stem_id except nulls are replaced with the string <NULL>';

COMMENT ON COLUMN grouper_group_set.member_attr_def_id IS 'member attr def if applicable';

COMMENT ON COLUMN grouper_group_set.member_group_id IS 'member group if applicable';

COMMENT ON COLUMN grouper_group_set.member_stem_id IS 'member stem if applicable';

COMMENT ON COLUMN grouper_group_set.member_id IS 'member id';

COMMENT ON COLUMN grouper_group_set.member_field_id IS 'used to join with the field_id column in the grouper_memberships table';

COMMENT ON TABLE grouper_stems IS 'entries for stems and their attributes';

COMMENT ON COLUMN grouper_stems.id IS 'db id of this row';

COMMENT ON COLUMN grouper_stems.parent_stem IS 'stem uuid of parent stem or empty if under root';

COMMENT ON COLUMN grouper_stems.name IS 'full name (id) path of stem';

COMMENT ON COLUMN grouper_stems.display_name IS 'full dislpay name path of stem';

COMMENT ON COLUMN grouper_stems.creator_id IS 'member_id of who created this stem';

COMMENT ON COLUMN grouper_stems.create_time IS 'number of millis since 1970 since this was created';

COMMENT ON COLUMN grouper_stems.modifier_id IS 'member_id of modifier who last edited';

COMMENT ON COLUMN grouper_stems.modify_time IS 'number of millis since 1970 since this was edited';

COMMENT ON COLUMN grouper_stems.display_extension IS 'display extension (not full path) of stem';

COMMENT ON COLUMN grouper_stems.extension IS 'extension (id) (not full path) of this stem';

COMMENT ON COLUMN grouper_stems.description IS 'description of stem';

COMMENT ON COLUMN grouper_stems.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_stems.last_membership_change IS 'If configured to keep track, this is the last membership change for this stem';

COMMENT ON COLUMN grouper_stems.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON TABLE grouper_pit_stems IS 'entries for stems and their attributes';

COMMENT ON COLUMN grouper_pit_stems.id IS 'db id of this row';

COMMENT ON COLUMN grouper_pit_stems.parent_stem_id IS 'stem uuid of parent stem or empty if under root';

COMMENT ON COLUMN grouper_pit_stems.name IS 'full name (id) path of stem';

COMMENT ON COLUMN grouper_pit_stems.hibernate_version_number IS 'hibernate uses this to version rows';

COMMENT ON COLUMN grouper_pit_stems.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_pit_stems.active IS 'T or F if this row is active by start and end time';

COMMENT ON COLUMN grouper_pit_stems.start_time IS 'millis sinve 1970 that this row was inserted';

COMMENT ON COLUMN grouper_pit_stems.end_time IS 'millis since 1970 that this row was deleted';

COMMENT ON TABLE grouper_loader_log IS 'log table with a row for each grouper loader job run';

COMMENT ON COLUMN grouper_loader_log.id IS 'uuid of this log record';

COMMENT ON COLUMN grouper_loader_log.job_name IS 'Could be group name (friendly) or just config name';

COMMENT ON COLUMN grouper_loader_log.status IS 'STARTED, RUNNING, SUCCESS, ERROR, WARNING, CONFIG_ERROR';

COMMENT ON COLUMN grouper_loader_log.started_time IS 'When the job was started';

COMMENT ON COLUMN grouper_loader_log.ended_time IS 'When the job ended (might be blank if daemon died)';

COMMENT ON COLUMN grouper_loader_log.millis IS 'Milliseconds this process took';

COMMENT ON COLUMN grouper_loader_log.millis_get_data IS 'Milliseconds this process took to get the data from the source';

COMMENT ON COLUMN grouper_loader_log.millis_load_data IS 'Milliseconds this process took to load the data to grouper';

COMMENT ON COLUMN grouper_loader_log.job_type IS 'GrouperLoaderJobType enum value';

COMMENT ON COLUMN grouper_loader_log.job_schedule_type IS 'GrouperLoaderJobscheduleType enum value';

COMMENT ON COLUMN grouper_loader_log.job_description IS 'More information about the job';

COMMENT ON COLUMN grouper_loader_log.job_message IS 'Could be a status or error message or stack';

COMMENT ON COLUMN grouper_loader_log.host IS 'Host that this job ran on';

COMMENT ON COLUMN grouper_loader_log.group_uuid IS 'If this job involves one group, this is uuid';

COMMENT ON COLUMN grouper_loader_log.job_schedule_quartz_cron IS 'Quartz cron string for this col';

COMMENT ON COLUMN grouper_loader_log.job_schedule_interval_seconds IS 'How many seconds this is supposed to wait between runs';

COMMENT ON COLUMN grouper_loader_log.last_updated IS 'When this record was last updated';

COMMENT ON COLUMN grouper_loader_log.unresolvable_subject_count IS 'The number of records which were not subject resolvable';

COMMENT ON COLUMN grouper_loader_log.insert_count IS 'The number of records inserted';

COMMENT ON COLUMN grouper_loader_log.update_count IS 'The number of records updated';

COMMENT ON COLUMN grouper_loader_log.delete_count IS 'The number of records deleted';

COMMENT ON COLUMN grouper_loader_log.total_count IS 'The total number of records (e.g. total number of members)';

COMMENT ON COLUMN grouper_loader_log.parent_job_name IS 'If this job is a subjob of another job, then put the parent job name here';

COMMENT ON COLUMN grouper_loader_log.parent_job_id IS 'If this job is a subjob of another job, then put the parent job id here';

COMMENT ON COLUMN grouper_loader_log.and_group_names IS 'If this group query is anded with another group or groups, they are listed here comma separated';

COMMENT ON COLUMN grouper_loader_log.job_schedule_priority IS 'Priority of this job (5 is unprioritized, higher the better)';

COMMENT ON COLUMN grouper_loader_log.context_id IS 'link to the audit entry table';

COMMENT ON TABLE grouper_stem_set IS 'This table holds the relationship between stems by easily indicating all the ancestors of a stem.';

COMMENT ON COLUMN grouper_stem_set.context_id IS 'uuid for the audit table';

COMMENT ON COLUMN grouper_stem_set.created_on IS 'millis since 1970 when this row was created';

COMMENT ON COLUMN grouper_stem_set.depth IS 'number of hops from one node to another, immediate is one';

COMMENT ON COLUMN grouper_stem_set.id IS 'uuid of this row';

COMMENT ON COLUMN grouper_stem_set.if_has_stem_id IS 'uuid foreign key of left hand side of this relationship. If an object is in this stem, it is also in the then_has stem.';

COMMENT ON COLUMN grouper_stem_set.last_updated IS 'millis since 1970 when this was last updated';

COMMENT ON COLUMN grouper_stem_set.parent_stem_set_id IS 'link back to this table for the parent entry of this stem set';

COMMENT ON COLUMN grouper_stem_set.then_has_stem_id IS 'uuid foreign key of the right hand side of this relationship.  If an object is in the if_has stem, it is also in this stem.';

COMMENT ON COLUMN grouper_stem_set.type IS 'from enum StemHierarchyType: self, immediate, effective';

COMMENT ON COLUMN grouper_stem_set.hibernate_version_number IS 'hibernate optimistic locking number for updates and deletes';

CREATE VIEW grouper_audit_entry_v (created_on, audit_category, action_name, logged_in_subject_id, act_as_subject_id, label_string01, string01, label_string02, string02, label_string03, string03, label_string04, string04, label_string05, string05, label_string06, string06, label_string07, string07, label_string08, string08, label_int01, int01, label_int02, int02, label_int03, int03, label_int04, int04, label_int05, int05, context_id, grouper_engine, description, logged_in_source_id, act_as_source_id, logged_in_member_id, act_as_member_id, audit_type_id, user_ip_address, server_host, audit_entry_last_updated, audit_entry_id, grouper_version, env_name) AS select gae.created_on, gat.audit_category, gat.action_name, (select gm.subject_id from grouper_members gm where gm.id = gae.logged_in_member_id) as logged_in_subject_id, (select gm.subject_id from grouper_members gm where gm.id = gae.act_as_member_id) as act_as_subject_id, gat.label_string01, gae.string01, gat.label_string02, gae.string02, gat.label_string03, gae.string03, gat.label_string04, gae.string04, gat.label_string05, gae.string05, gat.label_string06, gae.string06, gat.label_string07, gae.string07, gat.label_string08, gae.string08, gat.label_int01, gae.int01, gat.label_int02, gae.int02, gat.label_int03, gae.int03, gat.label_int04, gae.int04, gat.label_int05, gae.int05, gae.context_id, gae.grouper_engine, gae.description, (select gm.subject_source from grouper_members gm where gm.id = gae.logged_in_member_id) as logged_in_source_id, (select gm.subject_source from grouper_members gm where gm.id = gae.act_as_member_id) as act_as_source_id, gae.logged_in_member_id, gae.act_as_member_id, gat.id as audit_type_id, gae.user_ip_address, gae.server_host, gae.last_updated, gae.id as audit_entry_id, gae.grouper_version, gae.env_name from grouper_audit_type gat, grouper_audit_entry gae where gat.id = gae.audit_type_id ;

COMMENT ON TABLE grouper_audit_entry_v IS 'Join of audit entry and audit type, and converts member ids to subject ids';

COMMENT ON COLUMN grouper_audit_entry_v.created_on IS 'When this audit entry record was created';

COMMENT ON COLUMN grouper_audit_entry_v.audit_category IS 'The category of this audit from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.action_name IS 'The action in this audit category from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.logged_in_subject_id IS 'The subject id of the logged in subject, e.g. from WS or UI';

COMMENT ON COLUMN grouper_audit_entry_v.act_as_subject_id IS 'The subject id of the user using the system if they are acting as another user, e.g. from WS';

COMMENT ON COLUMN grouper_audit_entry_v.label_string01 IS 'The label of the string field 01 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string01 IS 'The string 01 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string02 IS 'The label of the string field 02 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string02 IS 'The string 02 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string03 IS 'The label of the string field 03 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string03 IS 'The string 03 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string04 IS 'The label of the string field 04 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string04 IS 'The string 04 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string05 IS 'The label of the string field 05 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string05 IS 'The string 05 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string06 IS 'The label of the string field 06 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string06 IS 'The string 06 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string07 IS 'The label of the string field 07 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string07 IS 'The string 07 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_string08 IS 'The label of the string field 08 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.string08 IS 'The string 08 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_int01 IS 'The label of the int field 01 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.int01 IS 'The int 01 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_int02 IS 'The label of the int field 02 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.int02 IS 'The int 02 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_int03 IS 'The label of the int field 03 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.int03 IS 'The int 03 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_int04 IS 'The label of the int field 04 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.int04 IS 'The int 04 value';

COMMENT ON COLUMN grouper_audit_entry_v.label_int05 IS 'The label of the int field 05 from grouper_audit_type';

COMMENT ON COLUMN grouper_audit_entry_v.int05 IS 'The int 05 value';

COMMENT ON COLUMN grouper_audit_entry_v.context_id IS 'Context id links together multiple operations into one high level action';

COMMENT ON COLUMN grouper_audit_entry_v.grouper_engine IS 'Grouper engine is e.g. UI, WS, GSH, loader, etc';

COMMENT ON COLUMN grouper_audit_entry_v.description IS 'Description is a sentence form expression of what is being audited';

COMMENT ON COLUMN grouper_audit_entry_v.logged_in_source_id IS 'Source id of the user who is logged in';

COMMENT ON COLUMN grouper_audit_entry_v.act_as_source_id IS 'Source id of the user who is being acted as (e.g. in WS)';

COMMENT ON COLUMN grouper_audit_entry_v.logged_in_member_id IS 'Member id (foreign key) of the user logged in';

COMMENT ON COLUMN grouper_audit_entry_v.act_as_member_id IS 'Member id (foreign key) of the user who is being acted as';

COMMENT ON COLUMN grouper_audit_entry_v.audit_type_id IS 'ID of the audit type row';

COMMENT ON COLUMN grouper_audit_entry_v.user_ip_address IS 'IP address of the user connecting to the system (e.g. from UI or WS)';

COMMENT ON COLUMN grouper_audit_entry_v.server_host IS 'Host of the system running the grouper API';

COMMENT ON COLUMN grouper_audit_entry_v.audit_entry_last_updated IS 'When this audit entry was last updated';

COMMENT ON COLUMN grouper_audit_entry_v.audit_entry_id IS 'ID of this audit entry';

COMMENT ON COLUMN grouper_audit_entry_v.grouper_version IS 'Grouper version of the API executing';

COMMENT ON COLUMN grouper_audit_entry_v.env_name IS 'environment label of the system running, from grouper.properties';

CREATE VIEW grouper_change_log_entry_v (created_on, change_log_category, action_name, sequence_number, label_string01, string01, label_string02, string02, label_string03, string03, label_string04, string04, label_string05, string05, label_string06, string06, label_string07, string07, label_string08, string08, label_string09, string09, label_string10, string10, label_string11, string11, label_string12, string12, context_id, change_log_type_id) AS SELECT gcle.created_on, gclt.change_log_category, gclt.action_name, gcle.sequence_number,        gclt.label_string01, gcle.string01, gclt.label_string02, gcle.string02,        gclt.label_string03, gcle.string03, gclt.label_string04, gcle.string04,        gclt.label_string05, gcle.string05, gclt.label_string06, gcle.string06,        gclt.label_string07, gcle.string07, gclt.label_string08, gcle.string08,        gclt.label_string09, gcle.string09, gclt.label_string10, gcle.string10,        gclt.label_string11, gcle.string11, gclt.label_string12, gcle.string12,        gcle.context_id, gcle.change_log_type_id   FROM grouper_change_log_type gclt, grouper_change_log_entry gcle  WHERE gclt.id = gcle.change_log_type_id;

COMMENT ON TABLE grouper_change_log_entry_v IS 'Join of change log entry and change log type';

COMMENT ON COLUMN grouper_change_log_entry_v.created_on IS 'created_on: when this change happened, number of millis since 1970';

COMMENT ON COLUMN grouper_change_log_entry_v.change_log_category IS 'change_log_category: category of this change';

COMMENT ON COLUMN grouper_change_log_entry_v.action_name IS 'action_name: action of this change';

COMMENT ON COLUMN grouper_change_log_entry_v.sequence_number IS 'sequence_number: increasing integer of each change';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string01 IS 'label_string01: label of first string';

COMMENT ON COLUMN grouper_change_log_entry_v.string01 IS 'string01: value of first string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string02 IS 'label_string02: label of second string';

COMMENT ON COLUMN grouper_change_log_entry_v.string02 IS 'string02: value of second string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string03 IS 'label_string03: label of third string';

COMMENT ON COLUMN grouper_change_log_entry_v.string03 IS 'string03: value of third string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string04 IS 'label_string04: label of fourth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string04 IS 'string04: value of fourth string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string05 IS 'label_string05: label of fifth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string05 IS 'string05: value of fifth string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string06 IS 'label_string06: label of sixth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string06 IS 'string06: value of sixth string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string07 IS 'label_string07: label of seventh string';

COMMENT ON COLUMN grouper_change_log_entry_v.string07 IS 'string07: value of seventh string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string08 IS 'label_string08: label of eighth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string08 IS 'string08: value of eighth string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string09 IS 'label_string09: label of ninth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string09 IS 'string09: value of ninth string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string10 IS 'label_string10: label of tenth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string10 IS 'string10: value of tenth string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string11 IS 'label_string11: label of eleventh string';

COMMENT ON COLUMN grouper_change_log_entry_v.string11 IS 'string11: value of eleventh string';

COMMENT ON COLUMN grouper_change_log_entry_v.label_string12 IS 'label_string12: label of twelfth string';

COMMENT ON COLUMN grouper_change_log_entry_v.string12 IS 'string12: value of twelfth string';

COMMENT ON COLUMN grouper_change_log_entry_v.context_id IS 'context_id: links this record with an audit record';

COMMENT ON COLUMN grouper_change_log_entry_v.change_log_type_id IS 'change_log_type_id: id of this category and name';

CREATE VIEW grouper_composites_v (OWNER_GROUP_NAME, COMPOSITE_TYPE, LEFT_FACTOR_GROUP_NAME, RIGHT_FACTOR_GROUP_NAME, OWNER_GROUP_DISPLAYNAME, LEFT_FACTOR_GROUP_DISPLAYNAME, RIGHT_FACTOR_GROUP_DISPLAYNAME, owner_group_id, LEFT_FACTOR_GROUP_ID, RIGHT_FACTOR_GROUP_ID, COMPOSITE_ID, CREATE_TIME, CREATOR_ID, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select  (select gg.name from grouper_groups gg  where gg.id = gc.owner) as owner_group_name,  gc.TYPE as composite_type,  (select gg.name from grouper_groups gg  where gg.id =  gc.left_factor) as left_factor_group_name,  (select gg.name from grouper_groups gg  where gg.id = gc.right_factor) as right_factor_group_name,  (select gg.display_name from grouper_groups gg  where gg.id = gc.owner) as owner_group_displayname,  (select gg.display_name from grouper_groups gg  where gg.id = gc.left_factor) as left_factor_group_displayname,  (select gg.display_name from grouper_groups gg  where gg.id = gc.right_factor) as right_factor_group_displayname,  gc.OWNER as owner_group_id,  gc.LEFT_FACTOR as left_factor_group_id,  gc.RIGHT_FACTOR as right_factor_group_id,  gc.ID as composite_id,  gc.CREATE_TIME,  gc.CREATOR_ID,  gc.HIBERNATE_VERSION_NUMBER, gc.context_id from grouper_composites gc  ;

COMMENT ON TABLE grouper_composites_v IS 'Grouper_composites_v is a view of composite relationships with friendly names.  A composite is a joining of two groups with a group math operator of: union, intersection, or complement.';

COMMENT ON COLUMN grouper_composites_v.OWNER_GROUP_NAME IS 'OWNER_GROUP_NAME: Name of the group which is the result of the composite operation, e.g. school:stem1:allPeople';

COMMENT ON COLUMN grouper_composites_v.COMPOSITE_TYPE IS 'COMPOSITE_TYPE: union (all members), intersection (only members in both), or complement (in first, not in second)';

COMMENT ON COLUMN grouper_composites_v.LEFT_FACTOR_GROUP_NAME IS 'LEFT_FACTOR_GROUP_NAME: Name of group which is the first of two groups in the composite operation, e.g. school:stem1:part1';

COMMENT ON COLUMN grouper_composites_v.RIGHT_FACTOR_GROUP_NAME IS 'RIGHT_FACTOR_GROUP_NAME: Name of group which is the second of two groups in the composite operation, e.g. school:stem1:part2';

COMMENT ON COLUMN grouper_composites_v.OWNER_GROUP_DISPLAYNAME IS 'OWNER_GROUP_DISPLAYNAME: Display name of result group of composite operation, e.g. My school:The stem1:All people';

COMMENT ON COLUMN grouper_composites_v.LEFT_FACTOR_GROUP_DISPLAYNAME IS 'LEFT_FACTOR_GROUP_DISPLAYNAME: Display name of group which is the first of two groups in the composite operation, e.g. My school:The stem1:Part 1';

COMMENT ON COLUMN grouper_composites_v.RIGHT_FACTOR_GROUP_DISPLAYNAME IS 'RIGHT_FACTOR_GROUP_DISPLAYNAME: Display name of group which is the second of two groups in the composite operation, e.g. My school:The stem1:Part 1';

COMMENT ON COLUMN grouper_composites_v.owner_group_id IS 'OWNER_GROUP_ID: UUID of the result group';

COMMENT ON COLUMN grouper_composites_v.LEFT_FACTOR_GROUP_ID IS 'LEFT_FACTOR_GROUP_ID: UUID of the first group of the composite operation';

COMMENT ON COLUMN grouper_composites_v.RIGHT_FACTOR_GROUP_ID IS 'RIGHT_FACTOR_GROUP_ID: UUID of the second group of the composite operation';

COMMENT ON COLUMN grouper_composites_v.COMPOSITE_ID IS 'COMPOSITE_ID: UUID of the composite relationship among the three groups';

COMMENT ON COLUMN grouper_composites_v.CREATE_TIME IS 'CREATE_TIME: number of millis since 1970 that the composite was created';

COMMENT ON COLUMN grouper_composites_v.CREATOR_ID IS 'CREATOR_ID: member id of the subject that created the composite relationship';

COMMENT ON COLUMN grouper_composites_v.HIBERNATE_VERSION_NUMBER IS 'HIBERNATE_VERSION_NUMBER: increments with each update, starts at 0';

COMMENT ON COLUMN grouper_composites_v.CONTEXT_ID IS 'CONTEXT_ID: Context id links together multiple operations into one high level action';

CREATE VIEW grouper_ext_subj_v (uuid, name, identifier, description, institution, email, search_string_lower) AS SELECT ges.uuid, ges.name, ges.identifier, ges.description , ges.institution , ges.email , ges.search_string_lower  FROM grouper_ext_subj ges WHERE ges.enabled = 'T';

COMMENT ON TABLE grouper_ext_subj_v IS 'grouper_ext_subj_v is a view of external subjects, and they attributes';

COMMENT ON COLUMN grouper_ext_subj_v.uuid IS 'uuid: universally unique id of subject';

COMMENT ON COLUMN grouper_ext_subj_v.name IS 'name: name field of the subject object';

COMMENT ON COLUMN grouper_ext_subj_v.identifier IS 'identifier: identifier of subject, e.g. the eppn';

COMMENT ON COLUMN grouper_ext_subj_v.description IS 'description: description field of the subject object';

COMMENT ON COLUMN grouper_ext_subj_v.institution IS 'institution: where the subject comes from';

COMMENT ON COLUMN grouper_ext_subj_v.email IS 'email: email address of the subject';

COMMENT ON COLUMN grouper_ext_subj_v.search_string_lower IS 'search_string_lower: lower case list of strings that the search will return results for subject';

CREATE VIEW grouper_memberships_all_v (MEMBERSHIP_ID, IMMEDIATE_MEMBERSHIP_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, IMMEDIATE_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, VIA_GROUP_ID, VIA_COMPOSITE_ID, DEPTH, MSHIP_TYPE, IMMEDIATE_MSHIP_ENABLED, IMMEDIATE_MSHIP_ENABLED_TIME, IMMEDIATE_MSHIP_DISABLED_TIME, GROUP_SET_PARENT_ID, MEMBERSHIP_CREATOR_ID, MEMBERSHIP_CREATE_TIME, GROUP_SET_CREATOR_ID, GROUP_SET_CREATE_TIME, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select ms.id || ':' || gs.id as membership_id, ms.id as immediate_membership_id, gs.id as group_set_id, ms.member_id, gs.field_id, ms.field_id, gs.owner_id, gs.owner_attr_def_id, gs.owner_group_id, gs.owner_stem_id, gs.via_group_id, ms.via_composite_id, gs.depth, gs.mship_type, ms.enabled, ms.enabled_timestamp, ms.disabled_timestamp, gs.parent_id as group_set_parent_id, ms.creator_id as membership_creator_id, ms.create_time as membership_create_time, gs.creator_id as group_set_creator_id, gs.create_time as group_set_create_time, ms.hibernate_version_number, ms.context_id from grouper_memberships ms, grouper_group_set gs where ms.owner_id = gs.member_id and ms.field_id = gs.member_field_id;

COMMENT ON TABLE grouper_memberships_all_v IS 'Grouper_memberships_all_v holds one record for each immediate, composite and effective membership or privilege in the system for members to groups or stems (for privileges).';

COMMENT ON COLUMN grouper_memberships_all_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: uuid unique id of this membership';

COMMENT ON COLUMN grouper_memberships_all_v.IMMEDIATE_MEMBERSHIP_ID IS 'IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_memberships_all_v.GROUP_SET_ID IS 'GROUP_SET_ID: uuid of the group set that causes this membership';

COMMENT ON COLUMN grouper_memberships_all_v.MEMBER_ID IS 'MEMBER_ID: id in the grouper_members table';

COMMENT ON COLUMN grouper_memberships_all_v.FIELD_ID IS 'FIELD_ID: id in the grouper_fields table';

COMMENT ON COLUMN grouper_memberships_all_v.IMMEDIATE_FIELD_ID IS 'IMMEDIATE_FIELD_ID: id in the grouper_fields table for the immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_memberships_all_v.OWNER_ID IS 'OWNER_ID: owner id';

COMMENT ON COLUMN grouper_memberships_all_v.OWNER_ATTR_DEF_ID IS 'OWNER_ATTR_DEF_ID: owner attribute def id if applicable';

COMMENT ON COLUMN grouper_memberships_all_v.OWNER_GROUP_ID IS 'OWNER_GROUP_ID: owner group if applicable';

COMMENT ON COLUMN grouper_memberships_all_v.OWNER_STEM_ID IS 'OWNER_STEM_ID: owner stem if applicable';

COMMENT ON COLUMN grouper_memberships_all_v.VIA_GROUP_ID IS 'VIA_GROUP_ID: membership is due to this group if effective';

COMMENT ON COLUMN grouper_memberships_all_v.VIA_COMPOSITE_ID IS 'VIA_COMPOSITE_ID: membership is due to this composite if applicable';

COMMENT ON COLUMN grouper_memberships_all_v.DEPTH IS 'DEPTH: number of hops in a directed graph';

COMMENT ON COLUMN grouper_memberships_all_v.MSHIP_TYPE IS 'MSHIP_TYPE: type of membership, immediate or effective or composite';

COMMENT ON COLUMN grouper_memberships_all_v.IMMEDIATE_MSHIP_ENABLED IS 'IMMEDIATE_MSHIP_ENABLED: T or F to indicate if this membership is enabled';

COMMENT ON COLUMN grouper_memberships_all_v.IMMEDIATE_MSHIP_ENABLED_TIME IS 'IMMEDIATE_MSHIP_ENABLED_TIME: when the membership will be enabled if the time is in the future';

COMMENT ON COLUMN grouper_memberships_all_v.IMMEDIATE_MSHIP_DISABLED_TIME IS 'IMMEDIATE_MSHIP_DISABLED_TIME: when the membership will be disabled if the time is in the future.';

COMMENT ON COLUMN grouper_memberships_all_v.GROUP_SET_PARENT_ID IS 'GROUP_SET_PARENT_ID: parent group set';

COMMENT ON COLUMN grouper_memberships_all_v.MEMBERSHIP_CREATOR_ID IS 'MEMBERSHIP_CREATOR_ID: member uuid of the creator of the immediate or composite membership';

COMMENT ON COLUMN grouper_memberships_all_v.MEMBERSHIP_CREATE_TIME IS 'MEMBERSHIP_CREATOR_TIME: number of millis since 1970 the immedate or composite membership was created';

COMMENT ON COLUMN grouper_memberships_all_v.GROUP_SET_CREATOR_ID IS 'GROUP_SET_CREATOR_ID: member uuid of the creator of the group set';

COMMENT ON COLUMN grouper_memberships_all_v.GROUP_SET_CREATE_TIME IS 'GROUP_SET_CREATE_TIME: number of millis since 1970 the group set was created';

COMMENT ON COLUMN grouper_memberships_all_v.HIBERNATE_VERSION_NUMBER IS 'HIBERNATE_VERSION_NUMBER: hibernate uses this to version rows';

COMMENT ON COLUMN grouper_memberships_all_v.CONTEXT_ID IS 'CONTEXT_ID: Context id links together multiple operations into one high level action';

CREATE VIEW grouper_pit_memberships_all_v (ID, MEMBERSHIP_ID, MEMBERSHIP_SOURCE_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, MEMBERSHIP_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, GROUP_SET_ACTIVE, GROUP_SET_START_TIME, GROUP_SET_END_TIME, MEMBERSHIP_ACTIVE, MEMBERSHIP_START_TIME, MEMBERSHIP_END_TIME, DEPTH, GROUP_SET_PARENT_ID) AS select ms.id || ':' || gs.id as membership_id, ms.id as immediate_membership_id, ms.source_id as membership_source_id, gs.id as group_set_id, ms.member_id, gs.field_id, ms.field_id, gs.owner_id, gs.owner_attr_def_id, gs.owner_group_id, gs.owner_stem_id, gs.active, gs.start_time, gs.end_time, ms.active, ms.start_time, ms.end_time, gs.depth, gs.parent_id as group_set_parent_id from grouper_pit_memberships ms, grouper_pit_group_set gs where ms.owner_id = gs.member_id and ms.field_id = gs.member_field_id;

COMMENT ON TABLE grouper_pit_memberships_all_v IS 'Grouper_pit_memberships_all_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).';

COMMENT ON COLUMN grouper_pit_memberships_all_v.ID IS 'ID: id of this membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: id of the immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBERSHIP_SOURCE_ID IS 'MEMBERSHIP_SOURCE_ID: id of the actual (non-pit) immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.GROUP_SET_ID IS 'GROUP_SET_ID: id of the group set that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBER_ID IS 'MEMBER_ID: member id';

COMMENT ON COLUMN grouper_pit_memberships_all_v.FIELD_ID IS 'FIELD_ID: field id';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBERSHIP_FIELD_ID IS 'MEMBERSHIP_FIELD_ID: field id of the immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.OWNER_ID IS 'OWNER_ID: owner id';

COMMENT ON COLUMN grouper_pit_memberships_all_v.OWNER_ATTR_DEF_ID IS 'OWNER_ATTR_DEF_ID: owner attribute def id if applicable';

COMMENT ON COLUMN grouper_pit_memberships_all_v.OWNER_GROUP_ID IS 'OWNER_GROUP_ID: owner group id if applicable';

COMMENT ON COLUMN grouper_pit_memberships_all_v.OWNER_STEM_ID IS 'OWNER_STEM_ID: owner stem id if applicable';

COMMENT ON COLUMN grouper_pit_memberships_all_v.GROUP_SET_ACTIVE IS 'GROUP_SET_ACTIVE: whether the group set is active';

COMMENT ON COLUMN grouper_pit_memberships_all_v.GROUP_SET_START_TIME IS 'GROUP_SET_START_TIME: start time of the group set';

COMMENT ON COLUMN grouper_pit_memberships_all_v.GROUP_SET_END_TIME IS 'GROUP_SET_END_TIME: end time of the group set';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBERSHIP_ACTIVE IS 'MEMBERSHIP_ACTIVE: whether the immediate (or composite) membership is active';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBERSHIP_START_TIME IS 'MEMBERSHIP_START_TIME: start time of the immediate (or composite) membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.MEMBERSHIP_END_TIME IS 'MEMBERSHIP_END_TIME: end time of the immediate (or composite) membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.DEPTH IS 'DEPTH: depth of this membership';

COMMENT ON COLUMN grouper_pit_memberships_all_v.GROUP_SET_PARENT_ID IS 'GROUP_SET_PARENT_ID: parent group set';

CREATE VIEW grouper_memberships_lw_v (SUBJECT_ID, SUBJECT_SOURCE, GROUP_NAME, LIST_NAME, LIST_TYPE, GROUP_ID, MEMBER_ID) AS select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gg.name as group_name, gfl.NAME as list_name, gfl.TYPE as list_type, gg.ID as group_id, gm.ID as member_id  from grouper_memberships_all_v gms, grouper_members gm, grouper_groups gg, grouper_fields gfl where gms.OWNER_GROUP_ID = gg.id and gms.FIELD_ID = gfl.ID and gms.MEMBER_ID = gm.ID and gms.IMMEDIATE_MSHIP_ENABLED = 'T';

COMMENT ON TABLE grouper_memberships_lw_v IS 'Grouper_memberships_lw_v unique membership records that can be read from a SQL interface outside of grouper.  Immediate and effective memberships are represented here (distinct)';

COMMENT ON COLUMN grouper_memberships_lw_v.SUBJECT_ID IS 'SUBJECT_ID: of the member of the group';

COMMENT ON COLUMN grouper_memberships_lw_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: of the member of the group';

COMMENT ON COLUMN grouper_memberships_lw_v.GROUP_NAME IS 'GROUP_NAME: system name of the group';

COMMENT ON COLUMN grouper_memberships_lw_v.LIST_NAME IS 'LIST_NAME: name of the list, e.g. members';

COMMENT ON COLUMN grouper_memberships_lw_v.LIST_TYPE IS 'LIST_TYPE: type of list e.g. access or list';

COMMENT ON COLUMN grouper_memberships_lw_v.GROUP_ID IS 'GROUP_ID: uuid of the group';

COMMENT ON COLUMN grouper_memberships_lw_v.MEMBER_ID IS 'MEMBER_ID: uuid of the member';

CREATE VIEW grouper_mship_stem_lw_v (SUBJECT_ID, SUBJECT_SOURCE, STEM_NAME, LIST_NAME, LIST_TYPE, STEM_ID) AS select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gs.name as stem_name, gfl.NAME as list_name, gfl.TYPE as list_type, gs.ID as stem_id from grouper_memberships_all_v gms, grouper_members gm, grouper_stems gs, grouper_fields gfl where gms.OWNER_STEM_ID = gs.id and gms.FIELD_ID = gfl.ID and gms.MEMBER_ID = gm.ID;

COMMENT ON TABLE grouper_mship_stem_lw_v IS 'grouper_mship_stem_lw_v unique membership records that can be read from a SQL interface outside of grouper for stems.  Immediate and effective memberships are represented here (distinct)';

COMMENT ON COLUMN grouper_mship_stem_lw_v.SUBJECT_ID IS 'SUBJECT_ID: of the member of the stem';

COMMENT ON COLUMN grouper_mship_stem_lw_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: of the member of the stem';

COMMENT ON COLUMN grouper_mship_stem_lw_v.STEM_NAME IS 'STEM_NAME: system name of the stem';

COMMENT ON COLUMN grouper_mship_stem_lw_v.LIST_NAME IS 'LIST_NAME: name of the list, e.g. members';

COMMENT ON COLUMN grouper_mship_stem_lw_v.LIST_TYPE IS 'LIST_TYPE: type of list e.g. access or list';

COMMENT ON COLUMN grouper_mship_stem_lw_v.STEM_ID IS 'STEM_ID: uuid of the stem';

CREATE VIEW grouper_mship_attrdef_lw_v (SUBJECT_ID, SUBJECT_SOURCE, ATTRIBUTE_DEF_NAME, LIST_NAME, LIST_TYPE, ATTRIBUTE_DEF_ID) AS select distinct gm.SUBJECT_ID, gm.SUBJECT_SOURCE, gad.name as attribute_def_name, gfl.NAME as list_name, gfl.TYPE as list_type, gad.id as attribute_def_id from grouper_memberships_all_v gms, grouper_members gm, grouper_attribute_def gad, grouper_fields gfl where gms.OWNER_ATTR_DEF_ID = gad.id and gms.FIELD_ID = gfl.ID and gms.MEMBER_ID = gm.ID;

COMMENT ON TABLE grouper_mship_attrdef_lw_v IS 'grouper_mship_attrdef_lw_v unique membership records of attr defs that can be read from a SQL interface outside of grouper.  Immediate and effective memberships are represented here (distinct)';

COMMENT ON COLUMN grouper_mship_attrdef_lw_v.SUBJECT_ID IS 'SUBJECT_ID: of the member of the group';

COMMENT ON COLUMN grouper_mship_attrdef_lw_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: of the member of the attributeDef';

COMMENT ON COLUMN grouper_mship_attrdef_lw_v.ATTRIBUTE_DEF_NAME IS 'ATTRIBUTE_DEF_NAME: system name of the attributeDef';

COMMENT ON COLUMN grouper_mship_attrdef_lw_v.LIST_NAME IS 'LIST_NAME: name of the list, e.g. members';

COMMENT ON COLUMN grouper_mship_attrdef_lw_v.LIST_TYPE IS 'LIST_TYPE: type of list e.g. access or list';

COMMENT ON COLUMN grouper_mship_attrdef_lw_v.ATTRIBUTE_DEF_ID IS 'ATTRIBUTE_DEF_ID: uuid of the attributeDef';

CREATE VIEW grouper_memberships_v (GROUP_NAME, GROUP_DISPLAYNAME, STEM_NAME, STEM_DISPLAYNAME, SUBJECT_ID, SUBJECT_SOURCE, MEMBER_ID, LIST_TYPE, LIST_NAME, MEMBERSHIP_TYPE, COMPOSITE_PARENT_GROUP_NAME, DEPTH, CREATOR_SOURCE, CREATOR_SUBJECT_ID, MEMBERSHIP_ID, IMMEDIATE_MEMBERSHIP_ID, GROUP_SET_ID, STEM_ID, GROUP_ID, CREATE_TIME, CREATOR_ID, FIELD_ID, CONTEXT_ID) AS select  (select gg.name from grouper_groups gg  where gg.id = gms.owner_group_id) as group_name,  (select gg.display_name from grouper_groups gg  where gg.id = gms.owner_group_id) as group_displayname,  (select gs.NAME from grouper_stems gs  where gs.ID = gms.owner_stem_id) as stem_name,  (select gs.display_NAME from grouper_stems gs  where gs.ID = gms.owner_stem_id) as stem_displayname,  gm.subject_id, gm.subject_source, gms.member_id, gf.TYPE as list_type,  gf.NAME as list_name,  gms.MSHIP_TYPE as membership_type,  (select gg.name from grouper_groups gg, grouper_composites gc  where gg.id = gms.VIA_composite_ID and gg.id = gc.OWNER) as composite_parent_group_name,  depth,   (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gms.membership_creator_ID) as creator_source,  (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gms.membership_creator_ID) as creator_subject_id,  gms.membership_id as membership_id,   gms.immediate_membership_id as immediate_membership_id,   gms.GROUP_SET_ID as group_set_id,  (select gs.id from grouper_stems gs where gs.ID = gms.owner_stem_id) as stem_id,  (select gg.id from grouper_groups gg where gg.id = gms.owner_group_id) as group_id,  gms.membership_create_time,  gms.membership_creator_id,  gms.field_id, gms.context_id   from grouper_memberships_all_v gms, grouper_members gm, grouper_fields gf   where gms.MEMBER_ID = gm.ID and gms.field_id = gf.id  ;

COMMENT ON TABLE grouper_memberships_v IS 'Grouper_memberships_v holds one record for each membership or privilege in the system for members to groups or stems (for privileges).  This is denormalized so there are records for the actual immediate relationships, and the cascaded effective relationships.  This has friendly names.';

COMMENT ON COLUMN grouper_memberships_v.GROUP_NAME IS 'GROUP_NAME: name of the group if this is a group membership, e.g. school:stem1:theGroup';

COMMENT ON COLUMN grouper_memberships_v.GROUP_DISPLAYNAME IS 'GROUP_DISPLAYNAME: display name of the group if this is a group membership, e.g. My school:The stem1:The group';

COMMENT ON COLUMN grouper_memberships_v.STEM_NAME IS 'STEM_NAME: name of the stem if this is a stem privilege, e.g. school:stem1';

COMMENT ON COLUMN grouper_memberships_v.STEM_DISPLAYNAME IS 'STEM_DISPLAYNAME: display name of the stems if this is a stem privilege, e.g. My school:The stem1';

COMMENT ON COLUMN grouper_memberships_v.SUBJECT_ID IS 'SUBJECT_ID: e.g. a school id of a person in the membership e.g. 12345';

COMMENT ON COLUMN grouper_memberships_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: source where the subject in the membership is from e.g. mySchoolPeople';

COMMENT ON COLUMN grouper_memberships_v.MEMBER_ID IS 'MEMBER_ID: id in the grouper_members table';

COMMENT ON COLUMN grouper_memberships_v.LIST_TYPE IS 'LIST_TYPE: list: members of a group, access: privilege of a group, naming: privilege of a stem';

COMMENT ON COLUMN grouper_memberships_v.LIST_NAME IS 'LIST_NAME: subset of list type.  which list if a list membership.  which privilege if a privilege.  e.g. members';

COMMENT ON COLUMN grouper_memberships_v.MEMBERSHIP_TYPE IS 'MEMBERSHIP_TYPE: either immediate (direct membership or privilege), of effective (membership due to a composite or a group being a member of another group)';

COMMENT ON COLUMN grouper_memberships_v.COMPOSITE_PARENT_GROUP_NAME IS 'COMPOSITE_PARENT_GROUP_NAME: name of group if this membership relates to a composite relationship, e.g. school:stem:allStudents';

COMMENT ON COLUMN grouper_memberships_v.DEPTH IS 'DEPTH: 0 for composite, if not then it is the 0 indexed count of number of group hops between member and group';

COMMENT ON COLUMN grouper_memberships_v.CREATOR_SOURCE IS 'CREATOR_SOURCE: subject source where the creator of the group is from';

COMMENT ON COLUMN grouper_memberships_v.CREATOR_SUBJECT_ID IS 'CREATOR_SUBJECT_ID: subject id of the creator of the group, e.g. 12345';

COMMENT ON COLUMN grouper_memberships_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: uuid unique id of this membership';

COMMENT ON COLUMN grouper_memberships_v.IMMEDIATE_MEMBERSHIP_ID IS 'IMMEDIATE_MEMBERSHIP_ID: uuid of the immediate membership that causes this membership';

COMMENT ON COLUMN grouper_memberships_v.GROUP_SET_ID IS 'GROUP_SET_ID: uuid of the group set that causes this membership';

COMMENT ON COLUMN grouper_memberships_v.STEM_ID IS 'STEM_ID: if this is a stem privilege, this is the stem uuid unique id';

COMMENT ON COLUMN grouper_memberships_v.GROUP_ID IS 'GROUP_ID: if this is a group list or privilege, this is the group uuid unique id';

COMMENT ON COLUMN grouper_memberships_v.CREATE_TIME IS 'CREATE_TIME: number of millis since 1970 since this membership was created';

COMMENT ON COLUMN grouper_memberships_v.CREATOR_ID IS 'CREATOR_ID: member_id of the creator, foreign key into grouper_members';

COMMENT ON COLUMN grouper_memberships_v.FIELD_ID IS 'FIELD_ID: uuid unique id of the field.  foreign key to grouper_fields.  This represents the list_type and list_name';

COMMENT ON COLUMN grouper_memberships_v.CONTEXT_ID IS 'CONTEXT_ID: Context id links together multiple operations into one high level action';

CREATE VIEW grouper_stems_v (EXTENSION, NAME, DISPLAY_EXTENSION, DISPLAY_NAME, DESCRIPTION, PARENT_STEM_NAME, PARENT_STEM_DISPLAYNAME, CREATOR_SOURCE, CREATOR_SUBJECT_ID, MODIFIER_SOURCE, MODIFIER_SUBJECT_ID, CREATE_TIME, CREATOR_ID, STEM_ID, MODIFIER_ID, MODIFY_TIME, PARENT_STEM, HIBERNATE_VERSION_NUMBER, CONTEXT_ID) AS select gs.extension, gs.NAME, gs.DISPLAY_EXTENSION, gs.DISPLAY_NAME, gs.DESCRIPTION, (select gs_parent.NAME from grouper_stems gs_parent where gs_parent.id = gs.PARENT_STEM) as parent_stem_name, (select gs_parent.DISPLAY_NAME from grouper_stems gs_parent where gs_parent.id = gs.PARENT_STEM) as parent_stem_displayname, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gs.creator_ID) as creator_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gs.creator_ID) as creator_subject_id, (select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gs.MODIFIER_ID) as modifier_source, (select gm.SUBJECT_ID from grouper_members gm where gm.ID = gs.MODIFIER_ID) as modifier_subject_id, gs.CREATE_TIME, gs.CREATOR_ID,  gs.ID as stem_id, gs.MODIFIER_ID, gs.MODIFY_TIME, gs.PARENT_STEM, gs.HIBERNATE_VERSION_NUMBER, gs.context_id from grouper_stems gs ;

COMMENT ON TABLE grouper_stems_v IS 'GROUPER_STEMS_V: holds one record for each stem (folder) in grouper, with friendly names';

COMMENT ON COLUMN grouper_stems_v.EXTENSION IS 'EXTENSION: name of the stem without the parent stem names, e.g. stem1';

COMMENT ON COLUMN grouper_stems_v.NAME IS 'NAME: name of the stem including parent stem names, e.g. school:stem1';

COMMENT ON COLUMN grouper_stems_v.DISPLAY_EXTENSION IS 'DISPLAY_EXTENSION: display name of the stem without parent stem names, e.g. The stem 1';

COMMENT ON COLUMN grouper_stems_v.DISPLAY_NAME IS 'DISPLAY_NAME: display name of the stem including parent stem names, e.g. My school: The stem 1';

COMMENT ON COLUMN grouper_stems_v.DESCRIPTION IS 'DESCRIPTION: description entered in about the stem, for example including why the stem exists and who has access';

COMMENT ON COLUMN grouper_stems_v.PARENT_STEM_NAME IS 'PARENT_STEM_NAME: name of the stem (folder) that this stem is in.  e.g. school';

COMMENT ON COLUMN grouper_stems_v.PARENT_STEM_DISPLAYNAME IS 'PARENT_STEM_DISPLAYNAME: display name of the stem (folder) that this stem is in.  e.g. My school';

COMMENT ON COLUMN grouper_stems_v.CREATOR_SOURCE IS 'CREATOR_SOURCE: subject source where the subject that created this stem is from, e.g. mySchoolPeople';

COMMENT ON COLUMN grouper_stems_v.CREATOR_SUBJECT_ID IS 'CREATOR_SUBJECT_ID: e.g. the school id of the subject that created this stem, e.g. 12345';

COMMENT ON COLUMN grouper_stems_v.MODIFIER_SOURCE IS 'MODIFIER_SOURCE: subject source where the subject that last modified this stem is from, e.g. mySchoolPeople';

COMMENT ON COLUMN grouper_stems_v.MODIFIER_SUBJECT_ID IS 'MODIFIER_SUBJECT_ID: e.g. the school id of the subject who last modified this stem, e.g. 12345';

COMMENT ON COLUMN grouper_stems_v.CREATE_TIME IS 'CREATE_TIME: number of millis since 1970 that this stem was created';

COMMENT ON COLUMN grouper_stems_v.CREATOR_ID IS 'CREATOR_ID: member id of the subject who created this stem, foreign key to grouper_members';

COMMENT ON COLUMN grouper_stems_v.STEM_ID IS 'STEM_ID: uuid unique id of this stem';

COMMENT ON COLUMN grouper_stems_v.MODIFIER_ID IS 'MODIFIER_ID: member id of the subject who last modified this stem, foreign key to grouper_members';

COMMENT ON COLUMN grouper_stems_v.MODIFY_TIME IS 'MODIFY_TIME: number of millis since 1970 since this stem was last modified';

COMMENT ON COLUMN grouper_stems_v.PARENT_STEM IS 'PARENT_STEM: stem_id uuid unique id of the stem (folder) that this stem is in';

COMMENT ON COLUMN grouper_stems_v.HIBERNATE_VERSION_NUMBER IS 'HIBERNATE_VERSION_NUMBER: increments by one for each update from hibernate';

COMMENT ON COLUMN grouper_stems_v.CONTEXT_ID IS 'CONTEXT_ID: Context id links together multiple operations into one high level action';

CREATE VIEW grouper_rpt_composites_v (COMPOSITE_TYPE, THE_COUNT) AS select gc.TYPE as composite_type, count(*) as the_count from grouper_composites gc group by gc.type ;

COMMENT ON TABLE grouper_rpt_composites_v IS 'GROUPER_RPT_COMPOSITES_V: report on the three composite types: union, intersection, complement and how many of each exist';

COMMENT ON COLUMN grouper_rpt_composites_v.COMPOSITE_TYPE IS 'COMPOSITE_TYPE: either union: all members from both factors, intersection: only members in both factors, complement: members in first but not second factor';

COMMENT ON COLUMN grouper_rpt_composites_v.THE_COUNT IS 'THE_COUNT: nubmer of composites of this type in the system';

CREATE VIEW grouper_rpt_group_field_v (GROUP_NAME, GROUP_DISPLAYNAME, FIELD_TYPE, FIELD_NAME, MEMBER_COUNT) AS select gg.name as group_name, gg.display_name as group_displayName, gf.type as field_type, gf.name as field_name, count(distinct gms.member_id) as member_count from grouper_memberships_all_v gms, grouper_groups gg, grouper_fields gf where gms.FIELD_ID = gf.ID and gg.id = gms.OWNER_group_ID group by gg.name, gg.display_name, gf.type, gf.name ;

COMMENT ON TABLE grouper_rpt_group_field_v IS 'GROUPER_RPT_GROUP_FIELD_V: report on how many unique members are in each group based on field (or list) name and type';

COMMENT ON COLUMN grouper_rpt_group_field_v.GROUP_NAME IS 'GROUP_NAME: name of the group where the list and members are, e.g. school:stem1:myGroup';

COMMENT ON COLUMN grouper_rpt_group_field_v.GROUP_DISPLAYNAME IS 'GROUP_DISPLAYNAME: display name of the group where the list and members are, e.g. My school:The stem1:My group';

COMMENT ON COLUMN grouper_rpt_group_field_v.FIELD_TYPE IS 'FIELD_TYPE: membership field type, e.g. list or access';

COMMENT ON COLUMN grouper_rpt_group_field_v.FIELD_NAME IS 'FIELD_NAME: membership field name, e.g. members, admins, readers';

COMMENT ON COLUMN grouper_rpt_group_field_v.MEMBER_COUNT IS 'MEMBER_COUNT: number of unique members in the group/field';

CREATE VIEW grouper_rpt_groups_v (GROUP_NAME, GROUP_DISPLAYNAME, TYPE_OF_GROUP, IMMEDIATE_MEMBERSHIP_COUNT, MEMBERSHIP_COUNT, ISA_COMPOSITE_FACTOR_COUNT, ISA_MEMBER_COUNT, GROUP_ID) AS select  gg.name as group_name, gg.display_name as group_displayname, gg.type_of_group, (select count(distinct gms.MEMBER_ID) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id and gms.MSHIP_TYPE = 'immediate') as immediate_membership_count, (select count(distinct gms.MEMBER_ID) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id) as membership_count, (select count(*) from grouper_composites gc where gc.LEFT_FACTOR = gg.id or gc.RIGHT_FACTOR = gg.id) as isa_composite_factor_count, (select count(distinct gms.OWNER_group_ID) from grouper_memberships_all_v gms, grouper_members gm where gm.SUBJECT_ID = gg.ID and gms.MEMBER_ID = gm.ID ) as isa_member_count, gg.ID as group_id from grouper_groups gg ;

COMMENT ON TABLE grouper_rpt_groups_v IS 'GROUPER_RPT_GROUPS_V: report with a line for each group and some counts of immediate and effective members etc';

COMMENT ON COLUMN grouper_rpt_groups_v.GROUP_NAME IS 'GROUP_NAME: name of group which has the stats, e.g. school:stem1:theGroup';

COMMENT ON COLUMN grouper_rpt_groups_v.GROUP_DISPLAYNAME IS 'GROUP_DISPLAYNAME: display name of the group which has the stats, e.g. My school:The stem1:The group';

COMMENT ON COLUMN grouper_rpt_groups_v.TYPE_OF_GROUP IS 'TYPE_OF_GROUP: group if it is a group, role if it is a role';

COMMENT ON COLUMN grouper_rpt_groups_v.IMMEDIATE_MEMBERSHIP_COUNT IS 'IMMEDIATE_MEMBERSHIP_COUNT: number of unique immediate members, directly assigned to this group';

COMMENT ON COLUMN grouper_rpt_groups_v.MEMBERSHIP_COUNT IS 'MEMBERSHIP_COUNT: total number of unique members, immediate or effective';

COMMENT ON COLUMN grouper_rpt_groups_v.ISA_COMPOSITE_FACTOR_COUNT IS 'ISA_COMPOSITE_FACTOR_COUNT: number of composites this group is a factor of';

COMMENT ON COLUMN grouper_rpt_groups_v.ISA_MEMBER_COUNT IS 'ISA_MEMBER_COUNT: number of groups this group is an immediate or effective member of';

COMMENT ON COLUMN grouper_rpt_groups_v.GROUP_ID IS 'GROUP_ID: uuid unique id of this group';

CREATE VIEW grouper_rpt_roles_v (ROLE_NAME, ROLE_DISPLAYNAME, IMMEDIATE_MEMBERSHIP_COUNT, MEMBERSHIP_COUNT, ISA_COMPOSITE_FACTOR_COUNT, ISA_MEMBER_COUNT, ROLE_ID) AS select  gg.name as role_name, gg.display_name as role_displayname, (select count(distinct gms.member_id) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id and gms.mship_type = 'immediate') as immediate_membership_count, (select count(distinct gms.member_id) from grouper_memberships_all_v gms where gms.OWNER_group_ID = gg.id) as membership_count, (select count(*) from grouper_composites gc where gc.LEFT_FACTOR = gg.id or gc.RIGHT_FACTOR = gg.id) as isa_composite_factor_count, (select count(distinct gms.OWNER_group_ID) from grouper_memberships_all_v gms, grouper_members gm where gm.SUBJECT_ID = gg.ID and gms.MEMBER_ID = gm.ID ) as isa_member_count, gg.ID as role_id from grouper_groups gg  where gg.type_of_group = 'role' ;

COMMENT ON TABLE grouper_rpt_roles_v IS 'GROUPER_RPT_ROLES_V: report with a line for each role and some counts of immediate and effective members etc';

COMMENT ON COLUMN grouper_rpt_roles_v.ROLE_NAME IS 'ROLE_NAME: name of group which has the stats, e.g. school:stem1:theGroup';

COMMENT ON COLUMN grouper_rpt_roles_v.ROLE_DISPLAYNAME IS 'ROLE_DISPLAYNAME: display name of the group which has the stats, e.g. My school:The stem1:The group';

COMMENT ON COLUMN grouper_rpt_roles_v.IMMEDIATE_MEMBERSHIP_COUNT IS 'IMMEDIATE_MEMBERSHIP_COUNT: number of unique immediate members, directly assigned to this group';

COMMENT ON COLUMN grouper_rpt_roles_v.MEMBERSHIP_COUNT IS 'MEMBERSHIP_COUNT: total number of unique members, immediate or effective';

COMMENT ON COLUMN grouper_rpt_roles_v.ISA_COMPOSITE_FACTOR_COUNT IS 'ISA_COMPOSITE_FACTOR_COUNT: number of composites this group is a factor of';

COMMENT ON COLUMN grouper_rpt_roles_v.ISA_MEMBER_COUNT IS 'ISA_MEMBER_COUNT: number of groups this group is an immediate or effective member of';

COMMENT ON COLUMN grouper_rpt_roles_v.ROLE_ID IS 'ROLE_ID: uuid unique id of this group';

CREATE VIEW grouper_rpt_members_v (SUBJECT_ID, SUBJECT_SOURCE, MEMBERSHIP_COUNT, MEMBER_ID) AS select gm.SUBJECT_ID, gm.SUBJECT_SOURCE, (select count(distinct gms.owner_group_id) from grouper_memberships gms where gms.MEMBER_ID = gm.ID) as membership_count, gm.ID as member_id from grouper_members gm ;

COMMENT ON TABLE grouper_rpt_members_v IS 'GROUPER_RPT_MEMBERS_V: report for each member in grouper_members and some stats like how many groups they are in';

COMMENT ON COLUMN grouper_rpt_members_v.SUBJECT_ID IS 'SUBJECT_ID: e.g. the school person id of the person e.g. 12345';

COMMENT ON COLUMN grouper_rpt_members_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: subject source where the subject is from, e.g. schoolAllPeople';

COMMENT ON COLUMN grouper_rpt_members_v.MEMBERSHIP_COUNT IS 'MEMBERSHIP_COUNT: number of distinct groups or stems this member has a membership with';

COMMENT ON COLUMN grouper_rpt_members_v.MEMBER_ID IS 'MEMBER_ID: uuid unique id of the member in grouper_members';

CREATE VIEW grouper_rpt_stems_v (STEM_NAME, STEM_DISPLAYNAME, GROUP_IMMEDIATE_COUNT, STEM_IMMEDIATE_COUNT, GROUP_COUNT, STEM_COUNT, THIS_STEM_MEMBERSHIP_COUNT, CHILD_GROUP_MEMBERSHIP_COUNT, GROUP_MEMBERSHIP_COUNT, STEM_ID) AS select gs.name as stem_name, gs.display_name as stem_displayname, (select count(*) from grouper_groups gg where gg.parent_stem = gs.ID) as group_immediate_count, (select count(*) from grouper_stems gs2 where gs.id = gs2.parent_stem ) as stem_immediate_count, (select count(*) from grouper_groups gg where gg.name like gs.name || '%') as group_count, (select count(*) from grouper_stems gs2 where gs2.name like gs.name || '%') as stem_count, (select count(distinct gm.member_id) from grouper_memberships_all_v gm where gm.owner_stem_id = gs.id) as this_stem_membership_count,  (select count(distinct gm.member_id) from grouper_memberships_all_v gm, grouper_groups gg where gg.parent_stem = gs.id and gm.owner_stem_id = gg.id) as child_group_membership_count,  (select count(distinct gm.member_id) from grouper_memberships_all_v gm, grouper_groups gg where gm.owner_group_id = gg.id and gg.name like gs.name || '%') as group_membership_count, gs.ID as stem_id from grouper_stems gs ;

COMMENT ON TABLE grouper_rpt_stems_v IS 'GROUPER_RPT_STEMS_V: report with a row for each stem and stats on many groups or members are inside';

COMMENT ON COLUMN grouper_rpt_stems_v.STEM_NAME IS 'STEM_NAME: name of the stem in report, e.g. school:stem1';

COMMENT ON COLUMN grouper_rpt_stems_v.STEM_DISPLAYNAME IS 'STEM_DISPLAYNAME: display name of the stem in report, e.g. My school:The stem 1';

COMMENT ON COLUMN grouper_rpt_stems_v.GROUP_IMMEDIATE_COUNT IS 'GROUP_IMMEDIATE_COUNT: number of groups directly inside this stem';

COMMENT ON COLUMN grouper_rpt_stems_v.STEM_IMMEDIATE_COUNT IS 'STEM_IMMEDIATE_COUNT: number of stems directly inside this stem';

COMMENT ON COLUMN grouper_rpt_stems_v.GROUP_COUNT IS 'GROUP_COUNT: number of groups inside this stem, or in a stem inside this stem etc';

COMMENT ON COLUMN grouper_rpt_stems_v.STEM_COUNT IS 'STEM_COUNT: number of stems inside this stem or in a stem inside this stem etc';

COMMENT ON COLUMN grouper_rpt_stems_v.THIS_STEM_MEMBERSHIP_COUNT IS 'THIS_STEM_MEMBERSHIP_COUNT: number of access memberships related to this stem (e.g. how many people can create groups/stems inside)';

COMMENT ON COLUMN grouper_rpt_stems_v.CHILD_GROUP_MEMBERSHIP_COUNT IS 'CHILD_GROUP_MEMBERSHIP_COUNT: number of memberships in groups immediately in this stem';

COMMENT ON COLUMN grouper_rpt_stems_v.GROUP_MEMBERSHIP_COUNT IS 'GROUP_MEMBERSHIP_COUNT: number of memberships in groups in this stem or in stems in this stem etc';

COMMENT ON COLUMN grouper_rpt_stems_v.STEM_ID IS 'STEM_ID: uuid unique id of this stem';

CREATE VIEW grouper_role_set_v (if_has_role_name, then_has_role_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_role_id, then_has_role_id, parent_role_set_id) AS select ifHas.name as if_has_role_name, thenHas.name as then_has_role_name,  grs.depth,   grs.type, grParentIfHas.name as parent_if_has_name, grParentThenHas.name as parent_then_has_name,   grs.id, ifHas.id as if_has_role_id, thenHas.id as then_has_role_id,   grs.parent_role_set_id  from grouper_role_set grs,   grouper_role_set grsParent,   grouper_groups grParentIfHas,   grouper_groups grParentThenHas,   grouper_groups ifHas, grouper_groups thenHas   where  thenHas.id = grs.then_has_role_id   and ifHas.id = grs.if_has_role_id   and grs.parent_role_set_id = grsParent.id   and grParentIfHas.id = grsParent.if_has_role_id   and grParentThenHas.id = grsParent.then_has_role_id   ;

COMMENT ON TABLE grouper_role_set_v IS 'grouper_role_set_v: shows all role set relationships';

COMMENT ON COLUMN grouper_role_set_v.if_has_role_name IS 'if_has_role_name: name of the set role';

COMMENT ON COLUMN grouper_role_set_v.then_has_role_name IS 'then_has_role_name: name of the member role';

COMMENT ON COLUMN grouper_role_set_v.depth IS 'depth: number of hops in the directed graph';

COMMENT ON COLUMN grouper_role_set_v.type IS 'type: self, immediate, effective';

COMMENT ON COLUMN grouper_role_set_v.parent_if_has_name IS 'parent_if_has_name: name of the role set record which is the parent ifHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_role_set_v.parent_then_has_name IS 'parent_then_has_name: name of the role set record which is the parent thenHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_role_set_v.id IS 'id: id of the set record';

COMMENT ON COLUMN grouper_role_set_v.if_has_role_id IS 'if_has_role_id: id of the set role';

COMMENT ON COLUMN grouper_role_set_v.then_has_role_id IS 'then_has_role_id: id of the member role';

COMMENT ON COLUMN grouper_role_set_v.parent_role_set_id IS 'parent_role_set_id: id of the role set record which is the parent on effective path (everything but last hop)';

CREATE VIEW grouper_attr_def_name_set_v (if_has_attr_def_name_name, then_has_attr_def_name_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_attr_def_name_id, then_has_attr_def_name_id, parent_attr_def_name_set_id) AS select ifHas.name as if_has_attr_def_name_name, thenHas.name as then_has_attr_def_name_name,  gadns.depth,  gadns.type, gadnParentIfHas.name as parent_if_has_name, gadnParentThenHas.name as parent_then_has_name,  gadns.id,  ifHas.id as if_has_attr_def_name_id, thenHas.id as then_has_attr_def_name_id,  gadns.parent_attr_def_name_set_id from grouper_attribute_def_name_set gadns,  grouper_attribute_def_name_set gadnsParent,  grouper_attribute_def_name gadnParentIfHas,  grouper_attribute_def_name gadnParentThenHas,  grouper_attribute_def_name ifHas, grouper_attribute_def_name thenHas  where  thenHas.id = gadns.then_has_attribute_def_name_id  and ifHas.id = gadns.if_has_attribute_def_name_id  and gadns.parent_attr_def_name_set_id = gadnsParent.id  and gadnParentIfHas.id = gadnsParent.if_has_attribute_def_name_id  and gadnParentThenHas.id = gadnsParent.then_has_attribute_def_name_id  ;

COMMENT ON TABLE grouper_attr_def_name_set_v IS 'grouper_attr_def_name_set_v: shows all attribute def name set relationships';

COMMENT ON COLUMN grouper_attr_def_name_set_v.if_has_attr_def_name_name IS 'if_has_attr_def_name_name: name of the set attribute def name';

COMMENT ON COLUMN grouper_attr_def_name_set_v.then_has_attr_def_name_name IS 'then_has_attr_def_name_name: name of the member attribute def name';

COMMENT ON COLUMN grouper_attr_def_name_set_v.depth IS 'depth: number of hops in the directed graph';

COMMENT ON COLUMN grouper_attr_def_name_set_v.type IS 'type: self, immediate, effective';

COMMENT ON COLUMN grouper_attr_def_name_set_v.parent_if_has_name IS 'parent_if_has_name: name of the attribute def name set record which is the parent ifHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_attr_def_name_set_v.parent_then_has_name IS 'parent_then_has_name: name of the attribute def name set record which is the parent thenHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_attr_def_name_set_v.id IS 'id: id of the set record';

COMMENT ON COLUMN grouper_attr_def_name_set_v.if_has_attr_def_name_id IS 'if_has_attr_def_name_id: id of the set attribute def name';

COMMENT ON COLUMN grouper_attr_def_name_set_v.then_has_attr_def_name_id IS 'then_has_attr_def_name_id: id of the member attribute def name';

COMMENT ON COLUMN grouper_attr_def_name_set_v.parent_attr_def_name_set_id IS 'parent_attr_def_name_set_id: id of the attribute def name set record which is the parent on effective path (everything but last hop)';

CREATE VIEW grouper_attr_assn_action_set_v (if_has_attr_assn_action_name, then_has_attr_assn_action_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_attr_assn_action_id, then_has_attr_assn_action_id, parent_attr_assn_action_id) AS select ifHas.name as if_has_attr_assn_action_name , thenHas.name as then_has_attr_assn_action_name,   gaaas.depth,   gaaas.type, gaaaParentIfHas.name as parent_if_has_name, gaaaParentThenHas.name as parent_then_has_name,   gaaas.id,   ifHas.id as if_has_attr_assn_action_id, thenHas.id as then_has_attr_assn_action_id,   gaaas.parent_attr_assn_action_id  from grouper_attr_assign_action_set gaaas,   grouper_attr_assign_action_set gaaasParent,   grouper_attr_assign_action gaaaParentIfHas,   grouper_attr_assign_action gaaaParentThenHas,   grouper_attr_assign_action ifHas, grouper_attr_assign_action thenHas   where  thenHas.id = gaaas.then_has_attr_assn_action_id   and ifHas.id = gaaas.if_has_attr_assn_action_id   and gaaas.parent_attr_assn_action_id = gaaasParent.id   and gaaaParentIfHas.id = gaaasParent.if_has_attr_assn_action_id   and gaaaParentThenHas.id = gaaasParent.then_has_attr_assn_action_id   ;

COMMENT ON TABLE grouper_attr_assn_action_set_v IS 'grouper_attr_assn_action_set_v: shows all action set relationships';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.if_has_attr_assn_action_name IS 'if_has_attr_assn_action_name: name of the set attribute action';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.then_has_attr_assn_action_name IS 'then_has_attr_assn_action_name: name of the member attribute action';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.depth IS 'depth: number of hops in the directed graph';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.type IS 'type: self, immediate, effective';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.parent_if_has_name IS 'parent_if_has_name: name of the attribute def name set record which is the parent ifHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.parent_then_has_name IS 'parent_then_has_name: name of the attribute def name set record which is the parent thenHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.id IS 'id: id of the set record';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.if_has_attr_assn_action_id IS 'if_has_attr_assn_action_id: id of the set attribute assign name';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.then_has_attr_assn_action_id IS 'then_has_attr_assn_action_id: id of the member attribute action';

COMMENT ON COLUMN grouper_attr_assn_action_set_v.parent_attr_assn_action_id IS 'parent_attr_assn_action_id: id of the attribute action set record which is the parent on effective path (everything but last hop)';

CREATE VIEW grouper_attr_asn_group_v (group_name, action, attribute_def_name_name, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gg.name as group_name, gaaa.name as action, gadn.name as attribute_def_name_name, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa  where gaa.owner_group_id = gg.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_member_id is null and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_attr_asn_group_v IS 'grouper_attr_asn_group_v: attribute assigned to a group, with related columns';

COMMENT ON COLUMN grouper_attr_asn_group_v.group_name IS 'group_name: name of group assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_group_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_group_v.group_display_name IS 'group_display_name: display name of the group assigned an attribute';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_group_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_attr_asn_group_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_group_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_group_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_group_v.group_id IS 'group_id: group id of the group assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_group_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_asn_group_v.action_id IS 'action_id: id of the attribute assign action';

CREATE VIEW grouper_attr_asn_efmship_v (group_name, subject_source_id, subject_id, action, attribute_def_name_name, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, list_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, member_id, action_id) AS select distinct gg.name as group_name, gm.subject_source as subject_source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gf.name as list_name, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gm.id as member_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_attr_assign_action gaaa  where gaa.owner_group_id = gmav.owner_group_id and gaa.owner_member_id = gmav.member_id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gmav.immediate_mship_enabled = 'T' and gmav.owner_group_id = gg.id and gmav.field_id = gf.id and gf.type = 'list' and gmav.member_id = gm.id and gaa.owner_member_id is not null and gaa.owner_group_id is not null and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_attr_asn_efmship_v IS 'grouper_attr_asn_efmship_v: attribute assigned to an effective membership';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.group_name IS 'group_name: name of group assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.subject_source_id IS 'subject_source_id: source id of the subject being assigned';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.subject_id IS 'subject_id: subject id of the subject being assigned';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.group_display_name IS 'group_display_name: display name of the group assigned an attribute';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.list_name IS 'list_name: name of the membership list for this effective membership';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.group_id IS 'group_id: group id of the group assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.member_id IS 'member_id: id of the member assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_efmship_v.action_id IS 'action_id: attribute assign action id';

CREATE VIEW grouper_attr_asn_stem_v (stem_name, action, attribute_def_name_name, stem_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, enabled, enabled_time, disabled_time, stem_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gs.name as stem_name, gaaa.name as action, gadn.name as attribute_def_name_name, gs.display_name as stem_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gs.id as stem_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_stems gs, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa  where gaa.owner_stem_id = gs.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_attr_asn_stem_v IS 'grouper_attr_asn_stem_v: attribute assigned to a stem and related cols';

COMMENT ON COLUMN grouper_attr_asn_stem_v.stem_name IS 'stem_name: name of stem assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_stem_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_stem_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_stem_v.stem_display_name IS 'stem_display_name: display name of the stem assigned an attribute';

COMMENT ON COLUMN grouper_attr_asn_stem_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_stem_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_stem_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_stem_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_stem_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_stem_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_stem_v.stem_id IS 'stem_id: stem id of the stem assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_stem_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_stem_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_stem_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_asn_stem_v.action_id IS 'action_id: id of the attribute assign action';

CREATE VIEW grouper_attr_asn_member_v (source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_members gm, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa where gaa.owner_member_id = gm.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_group_id is null  and gaa.attribute_assign_action_id = gaaa.id;

COMMENT ON TABLE grouper_attr_asn_member_v IS 'grouper_attr_asn_member_v: attribute assigned to a member and related cols';

COMMENT ON COLUMN grouper_attr_asn_member_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_member_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_member_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_member_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_attr_asn_member_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_member_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_member_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_member_v.member_id IS 'member_id: member id of the member assigned the attribute (this is an internal grouper uuid)';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_member_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_asn_member_v.action_id IS 'action_id: id of the attribute assign action';

CREATE VIEW grouper_attr_asn_mship_v (group_name, source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, list_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, membership_id, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name, gadn.display_name as attribute_def_name_disp_name, gf.name as list_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa  where gaa.owner_membership_id = gms.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id  and gf.type = 'list' and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_attr_asn_mship_v IS 'grouper_attr_asn_mship_v: attribute assigned to an immediate memberships, and related cols';

COMMENT ON COLUMN grouper_attr_asn_mship_v.group_name IS 'group_name: name of group in membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_mship_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_mship_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_mship_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_mship_v.list_name IS 'list_name: name of list in membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_mship_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_attr_asn_mship_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_mship_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_mship_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_mship_v.group_id IS 'group_id: group id of the membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_mship_v.membership_id IS 'membership_id: membership id assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_mship_v.member_id IS 'member_id: internal grouper member uuid of the membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_mship_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_asn_mship_v.action_id IS 'action_id: id of the attribute assign action';

CREATE VIEW grouper_attr_asn_attrdef_v (name_of_attr_def_assigned_to, action, attribute_def_name_name, attribute_def_name_disp_name, name_of_attribute_def_assigned, attribute_assign_notes, enabled, enabled_time, disabled_time, id_of_attr_def_assigned_to, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id) AS select gad_assigned_to.name as name_of_attr_def_assigned_to, gaaa.name as action, gadn.name as attribute_def_name_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gad_assigned_to.id as id_of_attr_def_assigned_to, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id from grouper_attribute_assign gaa, grouper_attribute_def gad_assigned_to, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa  where gaa.owner_attribute_def_id = gad_assigned_to.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_attr_asn_attrdef_v IS 'grouper_attr_asn_attrdef_v: attribute assigned to an attribute definition, and related columns';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.name_of_attr_def_assigned_to IS 'name_of_attr_def_assigned_to: name of attribute def assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.name_of_attribute_def_assigned IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.id_of_attr_def_assigned_to IS 'id_of_attr_def_assigned_to: attrDef id of the attributeDef assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_asn_attrdef_v.action_id IS 'action_id: id of the attribute assign action';

CREATE VIEW grouper_attr_asn_asn_group_v (group_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, group_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gg.name as group_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gg.display_name as group_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2   where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gg.id = gaa1.owner_group_id and gaa1.owner_member_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id;

COMMENT ON TABLE grouper_attr_asn_asn_group_v IS 'grouper_attr_asn_asn_group_v: attribute assigned to an assignment of attribute to a group, and related cols';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.group_name IS 'group_name: name of group assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.group_display_name IS 'group_display_name: display name of the group assigned an attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.group_id IS 'group_id: group id of the group assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_group_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

CREATE VIEW grouper_attr_asn_asn_efmship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select distinct gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.owner_member_id = gmav.member_id and gaa1.owner_group_id = gmav.owner_group_id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gmav.immediate_mship_enabled = 'T' and gmav.field_id = gf.id and gmav.member_id = gm.id and gmav.owner_group_id = gg.id and gf.type = 'list' and gaa1.owner_member_id is not null  and gaa1.owner_group_id is not null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_attr_asn_asn_efmship_v IS 'grouper_attr_asn_asn_efmship_v: attribute assigned to an assignment of an attribute to an effective membership, and related cols';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.group_name IS 'group_name: name of group in membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the new attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the original attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the new attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.list_name IS 'list_name: name of list in membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the original attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the new attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.group_id IS 'group_id: group id of the membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.member_id IS 'member_id: internal grouper member uuid of the membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_efmship_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

CREATE VIEW grouper_attr_asn_asn_stem_v (stem_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, stem_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, stem_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gs.name as stem_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gs.display_name as stem_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gs.id as stem_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_stems gs, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gs.id = gaa1.owner_stem_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_attr_asn_asn_stem_v IS 'grouper_attr_asn_asn_stem_v: attribute assigned to an assignment of attribute to a stem, and related cols';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.stem_name IS 'stem_name: name of stem assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.stem_display_name IS 'stem_display_name: display name of the stem assigned an attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.stem_id IS 'stem_id: stem id of the stem assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_stem_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

CREATE VIEW grouper_attr_asn_asn_member_v (source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_members gm, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gm.id = gaa1.owner_member_id and gaa1.owner_group_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_attr_asn_asn_member_v IS 'grouper_attr_asn_asn_member_v: attribute assigned to an assignment of an attribute to a member, and related cols';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.source_id IS 'source_id: source id of the member assigned the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.subject_id IS 'subject_id: subject id of the member assigned the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.member_id IS 'member_id: member id of the member assigned the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_member_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

CREATE VIEW grouper_attr_asn_asn_mship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, membership_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.owner_membership_id = gms.id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T'  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id and gf.type = 'list' and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_attr_asn_asn_mship_v IS 'grouper_attr_asn_asn_mship_v: attribute assigned to an assignment of an attribute to a membership, and related cols';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.group_name IS 'group_name: name of group in membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the new attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the original attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the new attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.list_name IS 'list_name: name of list in membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the original attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the new attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.group_id IS 'group_id: group id of the membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.membership_id IS 'membership_id: membership id assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.member_id IS 'member_id: internal grouper member uuid of the membership assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_mship_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

CREATE VIEW grouper_attr_asn_asn_attrdef_v (name_of_attr_def_assigned_to, action1, action2, attribute_def_name_name1, attribute_def_name_name2, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, id_of_attr_def_assigned_to, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2) AS select gad.name as name_of_attr_def_assigned_to, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gad.id as id_of_attr_def_assigned_to, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2 from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_attribute_def gad, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2 , grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2 where gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gad.id = gaa1.owner_attribute_def_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_attr_asn_asn_attrdef_v IS 'grouper_attr_asn_asn_attrdef_v: attribute assigned to an assignment of an attribute to an attribute definition, and related cols';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.name_of_attr_def_assigned_to IS 'name_of_attr_def_assigned_to: name of attribute_def originally assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.id_of_attr_def_assigned_to IS 'id_of_attr_def_assigned_to: id of the attribute def assigned the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_attr_asn_asn_attrdef_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

CREATE VIEW grouper_aval_asn_group_v (group_name, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gg.name as group_name, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav   where gaav.attribute_assign_id = gaa.id  and gaa.owner_group_id = gg.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_member_id is null and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_aval_asn_group_v IS 'grouper_aval_asn_group_v: attribute assigned to a group with related columns and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_group_v.group_name IS 'group_name: name of group assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_group_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_group_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_group_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_group_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_group_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_group_v.group_display_name IS 'group_display_name: display name of the group assigned an attribute';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_group_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_aval_asn_group_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_group_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_group_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_group_v.group_id IS 'group_id: group id of the group assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_aval_asn_group_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_aval_asn_group_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

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

CREATE VIEW grouper_aval_asn_stem_v (stem_name, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, stem_display_name, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, enabled, enabled_time, disabled_time, stem_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gs.name as stem_name, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gs.display_name as stem_display_name, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gs.id as stem_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_stems gs, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_stem_id = gs.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_aval_asn_stem_v IS 'grouper_aval_asn_stem_v: attribute assigned to a stem and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_stem_v.stem_name IS 'stem_name: name of stem assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_stem_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_stem_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_stem_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_stem_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_stem_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_stem_v.stem_display_name IS 'stem_display_name: display name of the stem assigned an attribute';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_stem_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_stem_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_stem_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_stem_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_stem_v.stem_id IS 'stem_id: stem id of the stem assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_aval_asn_stem_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_aval_asn_stem_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_member_v (source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_members gm, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa.id  and gaa.owner_member_id = gm.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.owner_group_id is null  and gaa.attribute_assign_action_id = gaaa.id;

COMMENT ON TABLE grouper_aval_asn_member_v IS 'grouper_aval_asn_member_v: attribute assigned to a member and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_member_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_member_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_member_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_member_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_member_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_member_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_member_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_member_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_aval_asn_member_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_member_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_member_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_member_v.member_id IS 'member_id: member id of the member assigned the attribute (this is an internal grouper uuid)';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_aval_asn_member_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_aval_asn_member_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_mship_v (group_name, source_id, subject_id, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name, list_name, name_of_attribute_def, attribute_assign_notes, attribute_assign_delegatable, enabled, enabled_time, disabled_time, group_id, membership_id, member_id, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn.display_name as attribute_def_name_disp_name, gf.name as list_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_membership_id = gms.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id  and gf.type = 'list' and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_aval_asn_mship_v IS 'grouper_aval_asn_mship_v: attribute assigned to an immediate memberships, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_mship_v.group_name IS 'group_name: name of group in membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_mship_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_mship_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_mship_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_mship_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_mship_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_mship_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_mship_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_mship_v.list_name IS 'list_name: name of list in membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_mship_v.name_of_attribute_def IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_aval_asn_mship_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_mship_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_mship_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_mship_v.group_id IS 'group_id: group id of the membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_mship_v.membership_id IS 'membership_id: membership id assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_mship_v.member_id IS 'member_id: internal grouper member uuid of the membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_aval_asn_mship_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_aval_asn_mship_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_attrdef_v (name_of_attr_def_assigned_to, action, attribute_def_name_name, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name, name_of_attribute_def_assigned, attribute_assign_notes, enabled, enabled_time, disabled_time, id_of_attr_def_assigned_to, attribute_assign_id, attribute_def_name_id, attribute_def_id, action_id, attribute_assign_value_id) AS select gad_assigned_to.name as name_of_attr_def_assigned_to, gaaa.name as action, gadn.name as attribute_def_name_name,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn.display_name as attribute_def_name_disp_name, gad.name as name_of_attribute_def, gaa.notes as attribute_assign_notes, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gad_assigned_to.id as id_of_attr_def_assigned_to, gaa.id as attribute_assign_id, gadn.id as attribute_def_name_id, gad.id as attribute_def_id, gaaa.id as action_id,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa, grouper_attribute_def gad_assigned_to, grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  where gaav.attribute_assign_id = gaa.id  and gaa.owner_attribute_def_id = gad_assigned_to.id and gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id = gad.id and gaa.attribute_assign_action_id = gaaa.id ;

COMMENT ON TABLE grouper_aval_asn_attrdef_v IS 'grouper_aval_asn_attrdef_v: attribute assigned to an attribute definition, and related columns and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.name_of_attr_def_assigned_to IS 'name_of_attr_def_assigned_to: name of attribute def assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.name_of_attribute_def_assigned IS 'name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_assign_notes IS 'attribute_assign_notes: notes related to the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.id_of_attr_def_assigned_to IS 'id_of_attr_def_assigned_to: attrDef id of the attributeDef assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_aval_asn_attrdef_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_asn_group_v (group_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, group_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gg.name as group_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gg.display_name as group_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav   where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gg.id = gaa1.owner_group_id and gaa1.owner_member_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id;

COMMENT ON TABLE grouper_aval_asn_asn_group_v IS 'grouper_aval_asn_asn_group_v: attribute assigned to an assignment of attribute to a group, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.group_name IS 'group_name: name of group assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.group_display_name IS 'group_display_name: display name of the group assigned an attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.group_id IS 'group_id: group id of the group assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_group_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_asn_efmship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select distinct gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships_all_v gmav, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.owner_member_id = gmav.member_id and gaa1.owner_group_id = gmav.owner_group_id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gmav.immediate_mship_enabled = 'T' and gmav.field_id = gf.id and gmav.member_id = gm.id and gmav.owner_group_id = gg.id and gf.type = 'list' and gaa1.owner_member_id is not null  and gaa1.owner_group_id is not null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_aval_asn_asn_efmship_v IS 'grouper_aval_asn_asn_efmship_v: attribute assigned to an assignment of an attribute to an effective membership, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.group_name IS 'group_name: name of group in membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the new attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the original attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the new attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.list_name IS 'list_name: name of list in membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the original attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the new attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.group_id IS 'group_id: group id of the membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.member_id IS 'member_id: internal grouper member uuid of the membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_efmship_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_asn_stem_v (stem_name, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, stem_display_name, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, stem_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gs.name as stem_name, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gs.display_name as stem_display_name, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gs.id as stem_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_stems gs, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gs.id = gaa1.owner_stem_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_aval_asn_asn_stem_v IS 'grouper_aval_asn_asn_stem_v: attribute assigned to an assignment of attribute to a stem, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.stem_name IS 'stem_name: name of stem assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.stem_display_name IS 'stem_display_name: display name of the stem assigned an attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.stem_id IS 'stem_id: stem id of the stem assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_stem_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_asn_member_v (source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_members gm, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gm.id = gaa1.owner_member_id and gaa1.owner_group_id is null and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_aval_asn_asn_member_v IS 'grouper_aval_asn_asn_member_v: attribute assigned to an assignment of an attribute to a member, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.source_id IS 'source_id: source id of the member assigned the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.subject_id IS 'subject_id: subject id of the member assigned the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.member_id IS 'member_id: member id of the member assigned the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_member_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_asn_mship_v (group_name, source_id, subject_id, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, list_name, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, group_id, membership_id, member_id, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gg.name as group_name, gm.subject_source as source_id, gm.subject_id, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gf.name as list_name, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gg.id as group_id, gms.id as membership_id, gm.id as member_id, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_groups gg, grouper_memberships gms, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_members gm, grouper_fields gf, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.owner_membership_id = gms.id and gaa2.owner_attribute_assign_id = gaa1.id  and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T'  and gms.field_id = gf.id and gms.member_id = gm.id and gms.owner_group_id = gg.id and gf.type = 'list' and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_aval_asn_asn_mship_v IS 'grouper_aval_asn_asn_mship_v: attribute assigned to an assignment of an attribute to a membership, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.group_name IS 'group_name: name of group in membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.source_id IS 'source_id: source of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.subject_id IS 'subject_id: subject_id of the subject that belongs to the member';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the new attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the original attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the new attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.list_name IS 'list_name: name of list in membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the original attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the new attribute definition associated with the attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.group_id IS 'group_id: group id of the membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.membership_id IS 'membership_id: membership id assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.member_id IS 'member_id: internal grouper member uuid of the membership assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_mship_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_aval_asn_asn_attrdef_v (name_of_attr_def_assigned_to, action1, action2, attribute_def_name_name1, attribute_def_name_name2, value_string, value_integer, value_floating, value_member_id, attribute_def_name_disp_name1, attribute_def_name_disp_name2, name_of_attribute_def1, name_of_attribute_def2, attribute_assign_notes1, attribute_assign_notes2, enabled2, enabled_time2, disabled_time2, id_of_attr_def_assigned_to, attribute_assign_id1, attribute_assign_id2, attribute_def_name_id1, attribute_def_name_id2, attribute_def_id1, attribute_def_id2, action_id1, action_id2, attribute_assign_value_id) AS select gad.name as name_of_attr_def_assigned_to, gaaa1.name as action1, gaaa2.name as action2,  gadn1.name as attribute_def_name_name1, gadn2.name as attribute_def_name_name2,  gaav.value_string AS value_string,  gaav.value_integer AS value_integer,  gaav.value_floating AS value_floating,  gaav.value_member_id AS value_member_id, gadn1.display_name as attribute_def_name_disp_name1, gadn2.display_name as attribute_def_name_disp_name2, gad1.name as name_of_attribute_def1, gad2.name as name_of_attribute_def2, gaa1.notes as attribute_assign_notes1, gaa2.notes as attribute_assign_notes2, gaa2.enabled as enabled2, gaa2.enabled_time as enabled_time2, gaa2.disabled_time as disabled_time2, gad.id as id_of_attr_def_assigned_to, gaa1.id as attribute_assign_id1, gaa2.id as attribute_assign_id2, gadn1.id as attribute_def_name_id1, gadn2.id as attribute_def_name_id2, gad1.id as attribute_def_id1, gad2.id as attribute_def_id2, gaaa1.id as action_id1, gaaa2.id as action_id2,  gaav.id AS attribute_assign_value_id from grouper_attribute_assign gaa1, grouper_attribute_assign gaa2, grouper_attribute_def gad, grouper_attribute_def_name gadn1, grouper_attribute_def_name gadn2, grouper_attribute_def gad1, grouper_attribute_def gad2, grouper_attr_assign_action gaaa1, grouper_attr_assign_action gaaa2, grouper_attribute_assign_value gaav where gaav.attribute_assign_id = gaa2.id  and gaa1.id = gaa2.owner_attribute_assign_id and gaa1.attribute_def_name_id = gadn1.id and gaa2.attribute_def_name_id = gadn2.id and gadn1.attribute_def_id = gad1.id and gadn2.attribute_def_id = gad2.id and gaa1.enabled = 'T' and gad.id = gaa1.owner_attribute_def_id and gaa1.attribute_assign_action_id = gaaa1.id and gaa2.attribute_assign_action_id = gaaa2.id ;

COMMENT ON TABLE grouper_aval_asn_asn_attrdef_v IS 'grouper_aval_asn_asn_attrdef_v: attribute assigned to an assignment of an attribute to an attribute definition, and related cols and values (multiple rows if multiple values, no rows if no values)';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.name_of_attr_def_assigned_to IS 'name_of_attr_def_assigned_to: name of attribute_def originally assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.action1 IS 'action1: the action associated with the original attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.action2 IS 'action2: the action associated with this attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_name_name1 IS 'attribute_def_name_name1: name of the original attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_name_name2 IS 'attribute_def_name_name2: name of the current attribute definition name which is assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.value_string IS 'value_string: if this is a string attributeDef, then this is the string';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.value_integer IS 'value_integer: if this is an integer attributeDef, then this is the integer';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.value_floating IS 'value_floating: if this is a floating attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.value_member_id IS 'value_member_id: if this is a memberId attributeDef, then this is the value';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_name_disp_name1 IS 'attribute_def_name_disp_name1: display name of the attribute definition name assigned to the original attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_name_disp_name2 IS 'attribute_def_name_disp_name2: display name of the attribute definition name assigned to the new attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.name_of_attribute_def1 IS 'name_of_attribute_def1: name of the attribute definition associated with the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.name_of_attribute_def2 IS 'name_of_attribute_def2: name of the attribute definition associated with the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_assign_notes1 IS 'attribute_assign_notes1: notes related to the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_assign_notes2 IS 'attribute_assign_notes2: notes related to the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.enabled2 IS 'enabled2: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.enabled_time2 IS 'enabled_time2: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.disabled_time2 IS 'disabled_time2: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.id_of_attr_def_assigned_to IS 'id_of_attr_def_assigned_to: id of the attribute def assigned the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_assign_id1 IS 'attribute_assign_id1: id of the original attribute assignment to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_assign_id2 IS 'attribute_assign_id2: id of the new attribute assignment to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_name_id1 IS 'attribute_def_name_id1: id of the original attribute definition name assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_name_id2 IS 'attribute_def_name_id2: id of the new attribute definition name assigned to the assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_id1 IS 'attribute_def_id1: id of the original attribute definition assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_def_id2 IS 'attribute_def_id2: id of the new attribute definition assigned to the attribute';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.action_id1 IS 'action_id1: id of the attribute assign action of the original assignment';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.action_id2 IS 'action_id2: id of the attribute assign action assigned to the group';

COMMENT ON COLUMN grouper_aval_asn_asn_attrdef_v.attribute_assign_value_id IS 'attribute_assign_value_id: the id of the value';

CREATE VIEW grouper_attr_def_priv_v (subject_id, subject_source_id, field_name, attribute_def_name, attribute_def_description, attribute_def_type, attribute_def_stem_id, attribute_def_id, member_id, field_id, immediate_membership_id, membership_id) AS select distinct gm.subject_id, gm.subject_source as subject_source_id,  gf.name as field_name, gad.name as attribute_def_name, gad.description as attribute_def_description,  gad.attribute_def_type, gad.stem_id as attribute_def_stem_id, gad.id as attribute_def_id,  gm.id as member_id, gmav.field_id, gmav.immediate_membership_id, gmav.membership_id  from grouper_memberships_all_v gmav, grouper_attribute_def gad, grouper_fields gf, grouper_members gm where gmav.owner_attr_def_id = gad.id and gmav.field_id = gf.id and gmav.immediate_mship_enabled = 'T' and gmav.member_id = gm.id ;

COMMENT ON TABLE grouper_attr_def_priv_v IS 'grouper_attr_def_priv_v: shows all privileges internal to grouper of attribute defs';

COMMENT ON COLUMN grouper_attr_def_priv_v.subject_id IS 'subject_id: of who has the priv';

COMMENT ON COLUMN grouper_attr_def_priv_v.subject_source_id IS 'subject_source_id: source id of the subject with the priv';

COMMENT ON COLUMN grouper_attr_def_priv_v.field_name IS 'field_name: field name of priv, e.g. attrView, attrRead, attrAdmin, attrUpdate, attrOptin, attrOptout, attrDefAttrRead, attrDefAttrUpdate';

COMMENT ON COLUMN grouper_attr_def_priv_v.attribute_def_name IS 'attribute_def_name: name of attribute definition';

COMMENT ON COLUMN grouper_attr_def_priv_v.attribute_def_description IS 'attribute_def_description: description of the attribute def';

COMMENT ON COLUMN grouper_attr_def_priv_v.attribute_def_type IS 'attribute_def_type: type of attribute, e.g. attribute, privilege, domain';

COMMENT ON COLUMN grouper_attr_def_priv_v.attribute_def_stem_id IS 'attribute_def_stem_id: id of stem the attribute def is in';

COMMENT ON COLUMN grouper_attr_def_priv_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_attr_def_priv_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_attr_def_priv_v.field_id IS 'field_id: id of the field of membership';

COMMENT ON COLUMN grouper_attr_def_priv_v.immediate_membership_id IS 'immediate_membership_id: id of the membership in the memberships table';

COMMENT ON COLUMN grouper_attr_def_priv_v.membership_id IS 'membership_id: id of the membership in the membership all view';

CREATE VIEW grouper_perms_assigned_role_v (role_name, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, attribute_def_name_id, action_id, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, attribute_assign_id, assignment_notes, disallowed, permission_type) AS SELECT distinct gr.name AS role_name,      gaaa.name AS action,     gadn.name AS attribute_def_name_name,     gadn.display_name AS attribute_def_name_disp_name,     gr.display_name AS role_display_name,     gaa.attribute_assign_delegatable,      gaa.enabled,     gaa.enabled_time,      gaa.disabled_time,      gr.ID AS role_id,     gadn.attribute_def_id,     gadn.ID AS attribute_def_name_id,      gaaa.ID AS action_id,     grs.DEPTH AS role_set_depth,     gadns.DEPTH AS attr_def_name_set_depth,     gaaas.DEPTH AS attr_assign_action_set_depth,     gaa.ID AS attribute_assign_id,     gaa.notes AS assignment_notes,     gaa.disallowed,     'role' AS permission_type FROM grouper_groups gr,     grouper_role_set grs,     grouper_attribute_def gad,     grouper_attribute_assign gaa,     grouper_attribute_def_name gadn,     grouper_attribute_def_name_set gadns,     grouper_attr_assign_action gaaa,     grouper_attr_assign_action_set gaaas WHERE grs.if_has_role_id = gr.id and gr.type_of_group = 'role'  AND gadn.attribute_def_id = gad.id AND gad.attribute_def_type = 'perm' AND gaa.owner_group_id = grs.then_has_role_id AND gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id AND gadn.id = gadns.then_has_attribute_def_name_id AND gaa.attribute_assign_type = 'group' AND gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id AND gaaa.id = gaaas.then_has_attr_assn_action_id ;

COMMENT ON TABLE grouper_perms_assigned_role_v IS 'grouper_perms_assigned_role_v: shows all permissions assigned to roles';

COMMENT ON COLUMN grouper_perms_assigned_role_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_perms_assigned_role_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_perms_assigned_role_v.role_display_name IS 'role_display_name: display name of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_perms_assigned_role_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_perms_assigned_role_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_perms_assigned_role_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_perms_assigned_role_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_perms_assigned_role_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_perms_assigned_role_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_assigned_role_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_perms_assigned_role_v.assignment_notes IS 'assignment_notes: notes on this assignment';

COMMENT ON COLUMN grouper_perms_assigned_role_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

COMMENT ON COLUMN grouper_perms_assigned_role_v.permission_type IS 'permission_type: role since these are role assignments';

CREATE VIEW grouper_perms_role_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS select distinct gr.name as role_name,  gm.subject_source as subject_source_id,  gm.subject_id,  gaaa.name as action, gadn.name as attribute_def_name_name,  gadn.display_name as attribute_def_name_disp_name,  gr.display_name as role_display_name,  gaa.attribute_assign_delegatable, gaa.enabled, gaa.enabled_time, gaa.disabled_time, gr.id as role_id,  gadn.attribute_def_id,  gm.id as member_id,  gadn.id as attribute_def_name_id,  gaaa.id as action_id, gmav.depth AS membership_depth, grs.depth AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gaa.id AS attribute_assign_id, 'role' as permission_type, gaa.notes as assignment_notes, gmav.immediate_mship_enabled_time, gmav.immediate_mship_disabled_time, gaa.disallowed from grouper_groups gr,  grouper_memberships_all_v gmav,  grouper_members gm,  grouper_fields gf,  grouper_role_set grs,  grouper_attribute_def gad,  grouper_attribute_assign gaa,  grouper_attribute_def_name gadn,  grouper_attribute_def_name_set gadns, grouper_attr_assign_action gaaa, grouper_attr_assign_action_set gaaas where gmav.owner_group_id = gr.id  and gmav.field_id = gf.id  and gr.type_of_group = 'role' and gf.type = 'list'  and gf.name = 'members'  and gmav.immediate_mship_enabled = 'T'  and gmav.member_id = gm.id  and grs.if_has_role_id = gr.id  and gadn.attribute_def_id = gad.id  and gad.attribute_def_type = 'perm'  and gaa.owner_group_id = grs.then_has_role_id  and gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id  and gadn.id = gadns.then_has_attribute_def_name_id  and gaa.attribute_assign_type = 'group' and gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id and gaaa.id = gaaas.then_has_attr_assn_action_id ;

COMMENT ON TABLE grouper_perms_role_v IS 'grouper_perms_role_v: shows all permissions assigned to users due to the users being in a role, and the role being assigned the permission';

COMMENT ON COLUMN grouper_perms_role_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_perms_role_v.subject_source_id IS 'subject_source_id: source id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_perms_role_v.subject_id IS 'subject_id: subject id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_perms_role_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_perms_role_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_perms_role_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_perms_role_v.role_display_name IS 'role_display_name: display name of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_role_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_perms_role_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_perms_role_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_perms_role_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_perms_role_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_role_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_perms_role_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_perms_role_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_perms_role_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_perms_role_v.membership_depth IS 'membership_depth: depth of membership, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_v.membership_id IS 'membership_id: id of the underlying membership';

COMMENT ON COLUMN grouper_perms_role_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_perms_role_v.permission_type IS 'permission_type: role or role_subject for assignment to role or to role subject pair';

COMMENT ON COLUMN grouper_perms_role_v.assignment_notes IS 'assignment_notes: notes on this assignment';

COMMENT ON COLUMN grouper_perms_role_v.immediate_mship_enabled_time IS 'immediate_mship_enabled_time: time this membership was enabled';

COMMENT ON COLUMN grouper_perms_role_v.immediate_mship_disabled_time IS 'immediate_mship_disabled_time: time this membership will be disabled';

COMMENT ON COLUMN grouper_perms_role_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

CREATE VIEW grouper_perms_role_subject_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS SELECT DISTINCT gr.name AS role_name,   gm.subject_source AS subject_source_id,   gm.subject_id,   gaaa.name AS ACTION,  gadn.name AS attribute_def_name_name,   gadn.display_name AS attribute_def_name_disp_name,   gr.display_name AS role_display_name,   gaa.attribute_assign_delegatable,  gaa.enabled,  gaa.enabled_time,  gaa.disabled_time,  gr.id AS role_id,   gadn.attribute_def_id,   gm.id AS member_id,   gadn.id AS attribute_def_name_id,   gaaa.id AS action_id, gmav.depth AS membership_depth, -1 AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gaa.id as attribute_assign_id, 'role_subject' as permission_type, gaa.notes as assignment_notes, gmav.immediate_mship_enabled_time, gmav.immediate_mship_disabled_time, gaa.disallowed FROM grouper_groups gr,   grouper_memberships_all_v gmav,   grouper_members gm,   grouper_fields gf,   grouper_attribute_def gad,  grouper_attribute_assign gaa,   grouper_attribute_def_name gadn,   grouper_attribute_def_name_set gadns,   grouper_attr_assign_action gaaa,  grouper_attr_assign_action_set gaaas  WHERE gmav.owner_group_id = gr.id  and gr.type_of_group = 'role' and gmav.field_id = gf.id  and gmav.owner_group_id = gaa.owner_group_id  AND gmav.member_id = gaa.owner_member_id   AND gf.type = 'list'   AND gf.name = 'members'   AND gmav.immediate_mship_enabled = 'T'   AND gmav.member_id = gm.id   AND gadn.attribute_def_id = gad.id  AND gad.attribute_def_type = 'perm'  AND gaa.attribute_assign_type = 'any_mem'  AND gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id   AND gadn.id = gadns.then_has_attribute_def_name_id  AND gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id  AND gaaa.id = gaaas.then_has_attr_assn_action_id  ;

COMMENT ON TABLE grouper_perms_role_subject_v IS 'grouper_perms_role_subject_v: shows all permissions assigned to users directly while in a role';

COMMENT ON COLUMN grouper_perms_role_subject_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_perms_role_subject_v.subject_source_id IS 'subject_source_id: source id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_perms_role_subject_v.subject_id IS 'subject_id: subject id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_perms_role_subject_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_perms_role_subject_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_perms_role_subject_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_perms_role_subject_v.role_display_name IS 'role_display_name: display name of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_role_subject_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_perms_role_subject_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_perms_role_subject_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_perms_role_subject_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_perms_role_subject_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_role_subject_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_perms_role_subject_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_perms_role_subject_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_perms_role_subject_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_perms_role_subject_v.membership_depth IS 'membership_depth: depth of membership, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_subject_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_subject_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_subject_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_role_subject_v.membership_id IS 'membership_id: id of the underlying membership';

COMMENT ON COLUMN grouper_perms_role_subject_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_perms_role_subject_v.permission_type IS 'permission_type: role or role_subject for assignment to role or to role subject pair';

COMMENT ON COLUMN grouper_perms_role_subject_v.assignment_notes IS 'assignment_notes: notes on this assignment';

COMMENT ON COLUMN grouper_perms_role_subject_v.immediate_mship_enabled_time IS 'immediate_mship_enabled_time: time this membership was enabled';

COMMENT ON COLUMN grouper_perms_role_subject_v.immediate_mship_disabled_time IS 'immediate_mship_disabled_time: time this membership will be disabled';

COMMENT ON COLUMN grouper_perms_role_subject_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

CREATE VIEW grouper_perms_all_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, attribute_def_name_disp_name, role_display_name, attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed) AS select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  attribute_def_name_disp_name,  role_display_name,  attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed from grouper_perms_role_v  union  select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  attribute_def_name_disp_name,  role_display_name,  attribute_assign_delegatable, enabled, enabled_time, disabled_time, role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, attribute_assign_id, permission_type, assignment_notes, immediate_mship_enabled_time, immediate_mship_disabled_time, disallowed from grouper_perms_role_subject_v  ;

COMMENT ON TABLE grouper_perms_all_v IS 'grouper_perms_all_v: shows all permissions assigned to users directly while in a role, or assigned to roles (and users in the role)';

COMMENT ON COLUMN grouper_perms_all_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_perms_all_v.subject_source_id IS 'subject_source_id: source id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_perms_all_v.subject_id IS 'subject_id: subject id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_perms_all_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_perms_all_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_perms_all_v.attribute_def_name_disp_name IS 'attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute';

COMMENT ON COLUMN grouper_perms_all_v.role_display_name IS 'role_display_name: display name of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_all_v.attribute_assign_delegatable IS 'attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT';

COMMENT ON COLUMN grouper_perms_all_v.enabled IS 'enabled: if this assignment is enabled: T, F';

COMMENT ON COLUMN grouper_perms_all_v.enabled_time IS 'enabled_time: the time (seconds since 1970) that this assignment will be enabled';

COMMENT ON COLUMN grouper_perms_all_v.disabled_time IS 'disabled_time: the time (seconds since 1970) that this assignment will be disabled';

COMMENT ON COLUMN grouper_perms_all_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_perms_all_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_perms_all_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_perms_all_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_perms_all_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_perms_all_v.membership_depth IS 'membership_depth: depth of membership, 0 is immediate';

COMMENT ON COLUMN grouper_perms_all_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_all_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_all_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_perms_all_v.membership_id IS 'membership_id: id of the underlying membership';

COMMENT ON COLUMN grouper_perms_all_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_perms_all_v.permission_type IS 'permission_type: role or role_subject for assignment to role or to role subject pair';

COMMENT ON COLUMN grouper_perms_all_v.assignment_notes IS 'assignment_notes: notes on this assignment';

COMMENT ON COLUMN grouper_perms_all_v.immediate_mship_enabled_time IS 'immediate_mship_enabled_time: time this membership was enabled';

COMMENT ON COLUMN grouper_perms_all_v.immediate_mship_disabled_time IS 'immediate_mship_disabled_time: time this membership will be disabled';

COMMENT ON COLUMN grouper_perms_all_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

CREATE VIEW grouper_pit_perms_role_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS select distinct gr.name as role_name,  gm.subject_source as subject_source_id,  gm.subject_id,  gaaa.name as action, gadn.name as attribute_def_name_name,  gr.id as role_id,  gadn.attribute_def_id,  gm.id as member_id,  gadn.id as attribute_def_name_id,  gaaa.id as action_id, gmav.depth AS membership_depth, grs.depth AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gmav.group_set_id as group_set_id, grs.id as role_set_id, gadns.id as attribute_def_name_set_id, gaaas.id as action_set_id, gaa.id AS attribute_assign_id, 'role' as permission_type, gmav.group_set_active, gmav.group_set_start_time, gmav.group_set_end_time, gmav.membership_active, gmav.membership_start_time, gmav.membership_end_time, grs.active as role_set_active, grs.start_time as role_set_start_time, grs.end_time as role_set_end_time, gaaas.active as action_set_active, gaaas.start_time as action_set_start_time, gaaas.end_time as action_set_end_time, gadns.active as attr_def_name_set_active, gadns.start_time as attr_def_name_set_start_time, gadns.end_time as attr_def_name_set_end_time, gaa.active as attribute_assign_active, gaa.start_time as attribute_assign_start_time, gaa.end_time as attribute_assign_end_time, gaa.disallowed,gaaa.source_id as action_source_id, gr.source_id as role_source_id, gadn.source_id as attribute_def_name_source_id, gad.source_id as attribute_def_source_id, gm.source_id as member_source_id, gmav.membership_source_id as membership_source_id, gaa.source_id as attribute_assign_source_id from grouper_pit_groups gr,  grouper_pit_memberships_all_v gmav,  grouper_pit_members gm,  grouper_pit_fields gf,  grouper_pit_role_set grs,  grouper_pit_attribute_def gad,  grouper_pit_attribute_assign gaa,  grouper_pit_attr_def_name gadn,  grouper_pit_attr_def_name_set gadns, grouper_pit_attr_assn_actn gaaa, grouper_pit_attr_assn_actn_set gaaas where gmav.owner_group_id = gr.id  and gmav.field_id = gf.id  and gf.type = 'list'  and gf.name = 'members'  and gmav.member_id = gm.id  and grs.if_has_role_id = gr.id  and gadn.attribute_def_id = gad.id  and gad.attribute_def_type = 'perm'  and gaa.owner_group_id = grs.then_has_role_id  and gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id  and gadn.id = gadns.then_has_attribute_def_name_id  and gaa.attribute_assign_type = 'group' and gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id and gaaa.id = gaaas.then_has_attr_assn_action_id ;

COMMENT ON TABLE grouper_pit_perms_role_v IS 'grouper_pit_perms_role_v: shows all permissions assigned to users due to the users being in a role, and the role being assigned the permission';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_pit_perms_role_v.subject_source_id IS 'subject_source_id: source id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_pit_perms_role_v.subject_id IS 'subject_id: subject id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_pit_perms_role_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_pit_perms_role_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_pit_perms_role_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_pit_perms_role_v.membership_depth IS 'membership_depth: depth of membership, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_v.membership_id IS 'membership_id: id of the immediate or composite membership in grouper_pit_memberships';

COMMENT ON COLUMN grouper_pit_perms_role_v.group_set_id IS 'group_set_id: id of the group set';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_set_id IS 'role_set_id: id of the role set';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_def_name_set_id IS 'attribute_def_name_set_id: id of the attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_role_v.action_set_id IS 'action_set_id: id of the action set';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_pit_perms_role_v.permission_type IS 'permission_type: role or role_subject for assignment to role or to role subject pair';

COMMENT ON COLUMN grouper_pit_perms_role_v.group_set_active IS 'group_set_active: whether the group set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_v.group_set_start_time IS 'group_set_start_time: start time of group set';

COMMENT ON COLUMN grouper_pit_perms_role_v.group_set_end_time IS 'group_set_end_time: end time of group set';

COMMENT ON COLUMN grouper_pit_perms_role_v.membership_active IS 'membership_active: whether the membership is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_v.membership_start_time IS 'membership_start_time: start time of membership';

COMMENT ON COLUMN grouper_pit_perms_role_v.membership_end_time IS 'membership_end_time: end time of membership';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_set_active IS 'role_set_active: whether the role set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_set_start_time IS 'role_set_start_time: start time of role set';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_set_end_time IS 'role_set_end_time: end time of role set';

COMMENT ON COLUMN grouper_pit_perms_role_v.action_set_active IS 'action_set_active: whether the action set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_v.action_set_start_time IS 'action_set_start_time: start time of action set';

COMMENT ON COLUMN grouper_pit_perms_role_v.action_set_end_time IS 'action_set_end_time: end time of action set';

COMMENT ON COLUMN grouper_pit_perms_role_v.attr_def_name_set_active IS 'attr_def_name_set_active: whether the attribute def name set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_v.attr_def_name_set_start_time IS 'attr_def_name_set_start_time: start time of attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_role_v.attr_def_name_set_end_time IS 'attr_def_name_set_end_time: end time of attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_assign_active IS 'attribute_assign_active: whether the attribute assign is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_assign_start_time IS 'attribute_assign_start_time: start time of attribute assign';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_assign_end_time IS 'attribute_assign_end_time: end time of attribute assign';

COMMENT ON COLUMN grouper_pit_perms_role_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

COMMENT ON COLUMN grouper_pit_perms_role_v.action_source_id IS 'action_source_id: id of the actual (non-pit) attribute assign action';

COMMENT ON COLUMN grouper_pit_perms_role_v.role_source_id IS 'role_source_id: id of the actual (non-pit) role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_def_name_source_id IS 'attribute_def_name_source_id: id of the actual (non-pit) attribute definition name';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_def_source_id IS 'attribute_def_source_id: id of the actual (non-pit) attribute definition';

COMMENT ON COLUMN grouper_pit_perms_role_v.member_source_id IS 'member_source_id: id of the actual (non-pit) subject in the members table';

COMMENT ON COLUMN grouper_pit_perms_role_v.membership_source_id IS 'membership_source_id: id of the actual (non-pit) immediate or composite membership';

COMMENT ON COLUMN grouper_pit_perms_role_v.attribute_assign_source_id IS 'attribute_assign_source_id: id of the actual (non-pit) attribute assign';

CREATE VIEW grouper_pit_perms_role_subj_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS SELECT DISTINCT gr.name AS role_name,   gm.subject_source AS subject_source_id,   gm.subject_id,   gaaa.name AS ACTION,  gadn.name AS attribute_def_name_name,   gr.id AS role_id,   gadn.attribute_def_id,   gm.id AS member_id,   gadn.id AS attribute_def_name_id,   gaaa.id AS action_id, gmav.depth AS membership_depth, -1 AS role_set_depth, gadns.depth AS attr_def_name_set_depth, gaaas.depth AS attr_assign_action_set_depth, gmav.membership_id as membership_id, gmav.group_set_id as group_set_id, grs.id as role_set_id, gadns.id as attribute_def_name_set_id, gaaas.id as action_set_id, gaa.id as attribute_assign_id, 'role_subject' as permission_type, gmav.group_set_active, gmav.group_set_start_time, gmav.group_set_end_time, gmav.membership_active, gmav.membership_start_time, gmav.membership_end_time, grs.active as role_set_active, grs.start_time as role_set_start_time, grs.end_time as role_set_end_time, gaaas.active as action_set_active, gaaas.start_time as action_set_start_time, gaaas.end_time as action_set_end_time, gadns.active as attr_def_name_set_active, gadns.start_time as attr_def_name_set_start_time, gadns.end_time as attr_def_name_set_end_time, gaa.active as attribute_assign_active, gaa.start_time as attribute_assign_start_time, gaa.end_time as attribute_assign_end_time, gaa.disallowed, gaaa.source_id as action_source_id, gr.source_id as role_source_id, gadn.source_id as attribute_def_name_source_id, gad.source_id as attribute_def_source_id, gm.source_id as member_source_id, gmav.membership_source_id as membership_source_id, gaa.source_id as attribute_assign_source_id FROM grouper_pit_groups gr,   grouper_pit_memberships_all_v gmav,   grouper_pit_members gm,   grouper_pit_fields gf,   grouper_pit_role_set grs,  grouper_pit_attribute_def gad,  grouper_pit_attribute_assign gaa,   grouper_pit_attr_def_name gadn,   grouper_pit_attr_def_name_set gadns,   grouper_pit_attr_assn_actn gaaa,  grouper_pit_attr_assn_actn_set gaaas  WHERE gmav.owner_group_id = gr.id  and gmav.field_id = gf.id  and gmav.owner_group_id = gaa.owner_group_id  AND gmav.member_id = gaa.owner_member_id   AND gf.type = 'list'   AND gf.name = 'members'   AND gmav.member_id = gm.id   AND gadn.attribute_def_id = gad.id  AND gad.attribute_def_type = 'perm'  AND gaa.attribute_assign_type = 'any_mem'  AND gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id   AND gadn.id = gadns.then_has_attribute_def_name_id  AND gaa.attribute_assign_action_id = gaaas.if_has_attr_assn_action_id  AND gaaa.id = gaaas.then_has_attr_assn_action_id  AND grs.if_has_role_id = gr.id and grs.depth='0'  ;

COMMENT ON TABLE grouper_pit_perms_role_subj_v IS 'grouper_pit_perms_role_subj_v: shows all permissions assigned to users directly while in a role';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.subject_source_id IS 'subject_source_id: source id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.subject_id IS 'subject_id: subject id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.membership_depth IS 'membership_depth: depth of membership, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.membership_id IS 'membership_id: id of the immediate or composite membership in grouper_pit_memberships';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.group_set_id IS 'group_set_id: id of the group set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_set_id IS 'role_set_id: id of the role set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_def_name_set_id IS 'attribute_def_name_set_id: id of the attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action_set_id IS 'action_set_id: id of the action set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.permission_type IS 'permission_type: role or role_subject for assignment to role or to role subject pair';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.group_set_active IS 'group_set_active: whether the group set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.group_set_start_time IS 'group_set_start_time: start time of group set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.group_set_end_time IS 'group_set_end_time: end time of group set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.membership_active IS 'membership_active: whether the membership is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.membership_start_time IS 'membership_start_time: start time of membership';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.membership_end_time IS 'membership_end_time: end time of membership';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_set_active IS 'role_set_active: whether the role set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_set_start_time IS 'role_set_start_time: start time of role set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_set_end_time IS 'role_set_end_time: end time of role set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action_set_active IS 'action_set_active: whether the action set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action_set_start_time IS 'action_set_start_time: start time of action set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action_set_end_time IS 'action_set_end_time: end time of action set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attr_def_name_set_active IS 'attr_def_name_set_active: whether the attribute def name set is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attr_def_name_set_start_time IS 'attr_def_name_set_start_time: start time of attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attr_def_name_set_end_time IS 'attr_def_name_set_end_time: end time of attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_assign_active IS 'attribute_assign_active: whether the attribute assign is currently active';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_assign_start_time IS 'attribute_assign_start_time: start time of attribute assign';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_assign_end_time IS 'attribute_assign_end_time: end time of attribute assign';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.action_source_id IS 'action_source_id: id of the actual (non-pit) attribute assign action';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.role_source_id IS 'role_source_id: id of the actual (non-pit) role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_def_name_source_id IS 'attribute_def_name_source_id: id of the actual (non-pit) attribute definition name';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_def_source_id IS 'attribute_def_source_id: id of the actual (non-pit) attribute definition';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.member_source_id IS 'member_source_id: id of the actual (non-pit) subject in the members table';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.membership_source_id IS 'membership_source_id: id of the actual (non-pit) immediate or composite membership';

COMMENT ON COLUMN grouper_pit_perms_role_subj_v.attribute_assign_source_id IS 'attribute_assign_source_id: id of the actual (non-pit) attribute assign';

CREATE VIEW grouper_pit_perms_all_v (role_name, subject_source_id, subject_id, action, attribute_def_name_name, role_id, attribute_def_id, member_id, attribute_def_name_id, action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id) AS select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id from grouper_pit_perms_role_v  union  select role_name,  subject_source_id,  subject_id,  action,  attribute_def_name_name,  role_id,  attribute_def_id,  member_id,  attribute_def_name_id,  action_id, membership_depth, role_set_depth, attr_def_name_set_depth, attr_assign_action_set_depth, membership_id, group_set_id, role_set_id, attribute_def_name_set_id, action_set_id, attribute_assign_id, permission_type, group_set_active, group_set_start_time, group_set_end_time, membership_active, membership_start_time, membership_end_time, role_set_active, role_set_start_time, role_set_end_time, action_set_active, action_set_start_time, action_set_end_time, attr_def_name_set_active, attr_def_name_set_start_time, attr_def_name_set_end_time, attribute_assign_active, attribute_assign_start_time, attribute_assign_end_time, disallowed, action_source_id, role_source_id, attribute_def_name_source_id, attribute_def_source_id, member_source_id, membership_source_id, attribute_assign_source_id from grouper_pit_perms_role_subj_v  ;

COMMENT ON TABLE grouper_pit_perms_all_v IS 'grouper_pit_perms_all_v: shows all permissions assigned to users directly while in a role, or assigned to roles (and users in the role)';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_name IS 'role_name: name of the role that the user is in and that has the permission';

COMMENT ON COLUMN grouper_pit_perms_all_v.subject_source_id IS 'subject_source_id: source id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_pit_perms_all_v.subject_id IS 'subject_id: subject id of the subject which is in the role and thus has the permission';

COMMENT ON COLUMN grouper_pit_perms_all_v.action IS 'action: the action associated with the attribute assignment (default is assign)';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_def_name_name IS 'attribute_def_name_name: name of the attribute definition name which is assigned to the group';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_id IS 'role_id: id of role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_def_id IS 'attribute_def_id: id of the attribute definition';

COMMENT ON COLUMN grouper_pit_perms_all_v.member_id IS 'member_id: id of the subject in the members table';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_pit_perms_all_v.action_id IS 'action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_pit_perms_all_v.membership_depth IS 'membership_depth: depth of membership, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_set_depth IS 'role_set_depth: depth of role hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_all_v.attr_def_name_set_depth IS 'attr_def_name_set_depth: depth of attribute def name set hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_all_v.attr_assign_action_set_depth IS 'attr_assign_action_set_depth: depth of action hierarchy, 0 is immediate';

COMMENT ON COLUMN grouper_pit_perms_all_v.membership_id IS 'membership_id: id of the immediate or composite membership in grouper_pit_memberships';

COMMENT ON COLUMN grouper_pit_perms_all_v.group_set_id IS 'group_set_id: id of the group set';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_set_id IS 'role_set_id: id of the role set';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_def_name_set_id IS 'attribute_def_name_set_id: id of the attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_all_v.action_set_id IS 'action_set_id: id of the action set';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_assign_id IS 'attribute_assign_id: id of the underlying attribute assign';

COMMENT ON COLUMN grouper_pit_perms_all_v.permission_type IS 'permission_type: role or role_subject for assignment to role or to role subject pair';

COMMENT ON COLUMN grouper_pit_perms_all_v.group_set_active IS 'group_set_active: whether the group set is currently active';

COMMENT ON COLUMN grouper_pit_perms_all_v.group_set_start_time IS 'group_set_start_time: start time of group set';

COMMENT ON COLUMN grouper_pit_perms_all_v.group_set_end_time IS 'group_set_end_time: end time of group set';

COMMENT ON COLUMN grouper_pit_perms_all_v.membership_active IS 'membership_active: whether the membership is currently active';

COMMENT ON COLUMN grouper_pit_perms_all_v.membership_start_time IS 'membership_start_time: start time of membership';

COMMENT ON COLUMN grouper_pit_perms_all_v.membership_end_time IS 'membership_end_time: end time of membership';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_set_active IS 'role_set_active: whether the role set is currently active';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_set_start_time IS 'role_set_start_time: start time of role set';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_set_end_time IS 'role_set_end_time: end time of role set';

COMMENT ON COLUMN grouper_pit_perms_all_v.action_set_active IS 'action_set_active: whether the action set is currently active';

COMMENT ON COLUMN grouper_pit_perms_all_v.action_set_start_time IS 'action_set_start_time: start time of action set';

COMMENT ON COLUMN grouper_pit_perms_all_v.action_set_end_time IS 'action_set_end_time: end time of action set';

COMMENT ON COLUMN grouper_pit_perms_all_v.attr_def_name_set_active IS 'attr_def_name_set_active: whether the attribute def name set is currently active';

COMMENT ON COLUMN grouper_pit_perms_all_v.attr_def_name_set_start_time IS 'attr_def_name_set_start_time: start time of attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_all_v.attr_def_name_set_end_time IS 'attr_def_name_set_end_time: end time of attribute def name set';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_assign_active IS 'attribute_assign_active: whether the attribute assign is currently active';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_assign_start_time IS 'attribute_assign_start_time: start time of attribute assign';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_assign_end_time IS 'attribute_assign_end_time: end time of attribute assign';

COMMENT ON COLUMN grouper_pit_perms_all_v.disallowed IS 'disallowed: if permission is disallowed from a wider allow, null means false';

COMMENT ON COLUMN grouper_pit_perms_all_v.action_source_id IS 'action_source_id: id of the actual (non-pit) attribute assign action';

COMMENT ON COLUMN grouper_pit_perms_all_v.role_source_id IS 'role_source_id: id of the actual (non-pit) role the subject is in, and that the permissions are assigned to';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_def_name_source_id IS 'attribute_def_name_source_id: id of the actual (non-pit) attribute definition name';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_def_source_id IS 'attribute_def_source_id: id of the actual (non-pit) attribute definition';

COMMENT ON COLUMN grouper_pit_perms_all_v.member_source_id IS 'member_source_id: id of the actual (non-pit) subject in the members table';

COMMENT ON COLUMN grouper_pit_perms_all_v.membership_source_id IS 'membership_source_id: id of the actual (non-pit) immediate or composite membership';

COMMENT ON COLUMN grouper_pit_perms_all_v.attribute_assign_source_id IS 'attribute_assign_source_id: id of the actual (non-pit) attribute assign';

CREATE VIEW grouper_pit_attr_asn_value_v (attribute_assign_value_id, attribute_assign_id, attribute_def_name_id, attribute_assign_action_id, attribute_assign_type, owner_attribute_assign_id, owner_attribute_def_id, owner_group_id, owner_member_id, owner_membership_id, owner_stem_id, value_integer, value_floating, value_string, value_member_id, active, start_time, end_time) AS select gpaav.id as attribute_assign_value_id,  gpaa.id as attribute_assign_id,  gpaa.attribute_def_name_id,  gpaa.attribute_assign_action_id,  gpaa.attribute_assign_type,  gpaa.owner_attribute_assign_id,  gpaa.owner_attribute_def_id,  gpaa.owner_group_id,  gpaa.owner_member_id,  gpaa.owner_membership_id, gpaa.owner_stem_id, gpaav.value_integer, gpaav.value_floating, gpaav.value_string, gpaav.value_member_id, gpaav.active, gpaav.start_time, gpaav.end_time from grouper_pit_attribute_assign gpaa, grouper_pit_attr_assn_value gpaav where gpaa.id = gpaav.attribute_assign_id;

COMMENT ON TABLE grouper_pit_attr_asn_value_v IS 'grouper_pit_attr_asn_value_v: joins attribute values with their assignments';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.attribute_assign_value_id IS 'attribute_assign_value_id: id of the attribute assign value';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.attribute_assign_id IS 'attribute_assign_id: id of the attribute assignment';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.attribute_def_name_id IS 'attribute_def_name_id: id of the attribute definition name';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.attribute_assign_action_id IS 'attribute_assign_action_id: id of the attribute assign action';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.attribute_assign_type IS 'attribute_assign_type: type of assignment';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.owner_attribute_assign_id IS 'owner_attribute_assign_id: owner id of the attribute assignment if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.owner_attribute_def_id IS 'owner_attribute_def_id: owner id of the attribute definition if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.owner_group_id IS 'owner_group_id: owner id of the group if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.owner_member_id IS 'owner_member_id: owner id of the member if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.owner_membership_id IS 'owner_membership_id: owner id of the membership if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.owner_stem_id IS 'owner_stem_id: owner id of the stem if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.value_integer IS 'value_integer: integer value if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.value_floating IS 'value_floating: floating point value if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.value_string IS 'value_string: string value if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.value_member_id IS 'value_member_id: member id value if applicable';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.active IS 'active: whether the value is currently active';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.start_time IS 'start_time: start time of value';

COMMENT ON COLUMN grouper_pit_attr_asn_value_v.end_time IS 'end_time: end time of value';

CREATE VIEW grouper_stem_set_v (if_has_stem_name, then_has_stem_name, depth, type, parent_if_has_name, parent_then_has_name, id, if_has_stem_id, then_has_stem_id, parent_stem_set_id) AS select ifHas.name as if_has_stem_name , thenHas.name as then_has_stem_name,   gss.depth,   gss.type, gsParentIfHas.name as parent_if_has_name, gsParentThenHas.name as parent_then_has_name,   gss.id,   ifHas.id as if_has_stem_id, thenHas.id as then_has_stem_id,   gss.parent_stem_set_id  from grouper_stem_set gss,   grouper_stem_set gssParent,   grouper_stems gsParentIfHas,   grouper_stems gsParentThenHas,   grouper_stems ifHas, grouper_stems thenHas   where  thenHas.id = gss.then_has_stem_id   and ifHas.id = gss.if_has_stem_id   and gss.parent_stem_set_id = gssParent.id   and gsParentIfHas.id = gssParent.if_has_stem_id   and gsParentThenHas.id = gssParent.then_has_stem_id   ;

COMMENT ON TABLE grouper_stem_set_v IS 'grouper_stem_set_v: shows all stem set relationships';

COMMENT ON COLUMN grouper_stem_set_v.if_has_stem_name IS 'if_has_stem_name: name of the if_has stem';

COMMENT ON COLUMN grouper_stem_set_v.then_has_stem_name IS 'then_has_stem_name: name of the then_has stem';

COMMENT ON COLUMN grouper_stem_set_v.depth IS 'depth: number of hops in the directed graph';

COMMENT ON COLUMN grouper_stem_set_v.type IS 'type: self, immediate, effective';

COMMENT ON COLUMN grouper_stem_set_v.parent_if_has_name IS 'parent_if_has_name: name of the stem record which is the parent ifHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_stem_set_v.parent_then_has_name IS 'parent_then_has_name: name of the stem record which is the parent thenHas on effective path (everything but last hop)';

COMMENT ON COLUMN grouper_stem_set_v.id IS 'id: id of the set record';

COMMENT ON COLUMN grouper_stem_set_v.if_has_stem_id IS 'if_has_stem_id: id of the if_has stem';

COMMENT ON COLUMN grouper_stem_set_v.then_has_stem_id IS 'then_has_stem_id: id of the then_has stem';

COMMENT ON COLUMN grouper_stem_set_v.parent_stem_set_id IS 'parent_stem_set_id: id of the stem set record which is the parent on effective path (everything but last hop)';

CREATE VIEW grouper_ext_subj_invite_v (invite_id, invite_member_id, invite_date, email_address, invite_email_when_registered, invite_group_uuids, invite_expire_date, email_body, expire_attr_expire_date, expire_attr_enabled, assignment_expire_date, assignment_enabled, attribute_assign_id) AS SELECT (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteUuid'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_id, (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteMemberId'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_member_id,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteDate'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_date,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectEmailAddress'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS email_address, (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteEmailWhenRegistered'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_email_when_registered,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteGroupUuids'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_group_uuids,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteExpireDate'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS invite_expire_date,  (SELECT gaav.value_string  FROM grouper_attr_asn_asn_stem_v gaaasv, grouper_attribute_assign_value gaav  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteEmail'  AND gaav.attribute_assign_id = gaaasv.attribute_assign_id2  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS email_body,  (SELECT gaaasv.disabled_time2  FROM grouper_attr_asn_asn_stem_v gaaasv  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteExpireDate'  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS expire_attr_expire_date,  (SELECT gaaasv.enabled2  FROM grouper_attr_asn_asn_stem_v gaaasv  WHERE gaaasv.attribute_def_name_name2 = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInviteExpireDate'  AND gaaasv.attribute_assign_id1 = gaasv.attribute_assign_id) AS expire_attr_enabled,  gaasv.disabled_time AS assignment_expire_date,  gaasv.enabled AS assignment_enabled,  gaasv.attribute_assign_id  FROM grouper_attr_asn_stem_v gaasv  WHERE gaasv.attribute_def_name_name = 'etc:attribute:attrExternalSubjectInvite:externalSubjectInvite'  AND gaasv.enabled = 'T' ;

COMMENT ON TABLE grouper_ext_subj_invite_v IS 'External subject invites pending, waiting for someone to respond';

COMMENT ON COLUMN grouper_ext_subj_invite_v.invite_id IS 'invite_id: id of the invite, in the url of the link';

COMMENT ON COLUMN grouper_ext_subj_invite_v.invite_member_id IS 'invite_member_id: member id of who invited the user';

COMMENT ON COLUMN grouper_ext_subj_invite_v.invite_date IS 'invite_date: date of the invite';

COMMENT ON COLUMN grouper_ext_subj_invite_v.email_address IS 'email_address: email address where the invite went';

COMMENT ON COLUMN grouper_ext_subj_invite_v.invite_email_when_registered IS 'invite_email_when_registered: email sent to this address when person registered';

COMMENT ON COLUMN grouper_ext_subj_invite_v.invite_group_uuids IS 'invite_group_uuids: group uuids that the user should be provisioned to when accepting the invite';

COMMENT ON COLUMN grouper_ext_subj_invite_v.invite_expire_date IS 'invite_expire_date: when the invite expires, attribute value';

COMMENT ON COLUMN grouper_ext_subj_invite_v.email_body IS 'email_body: email body sent to user, might be truncated if too long';

COMMENT ON COLUMN grouper_ext_subj_invite_v.expire_attr_expire_date IS 'expire_attr_expire_date: expire date of the expire attribute assignment';

COMMENT ON COLUMN grouper_ext_subj_invite_v.expire_attr_enabled IS 'expire_attr_enabled: if the expire attribute is enabled';

COMMENT ON COLUMN grouper_ext_subj_invite_v.assignment_expire_date IS 'assignment_expire_date: expire date of the attribute assignment on the stem';

COMMENT ON COLUMN grouper_ext_subj_invite_v.assignment_enabled IS 'assignment_enabled: if the attribute assignment on the stem is enabled';

COMMENT ON COLUMN grouper_ext_subj_invite_v.attribute_assign_id IS 'attribute_assign_id: attribute assign id of the attribute assignment on the stem';

CREATE VIEW grouper_rules_v (assigned_to_type, assigned_to_group_name, assigned_to_stem_name, assigned_to_member_subject_id, assigned_to_attribute_def_name, rule_check_type, rule_check_owner_id, rule_check_owner_name, rule_check_stem_scope, rule_check_arg0, rule_check_arg1, rule_if_condition_el, rule_if_condition_enum, rule_if_condition_enum_arg0, rule_if_condition_enum_arg1, rule_if_owner_id, rule_if_owner_name, rule_if_stem_scope, rule_then_el, rule_then_enum, rule_then_enum_arg0, rule_then_enum_arg1, rule_then_enum_arg2, rule_valid, rule_run_daemon, rule_act_as_subject_id, rule_act_as_subject_identifier, rule_act_as_subject_source_id, assignment_enabled, attribute_assign_id) AS SELECT main_gaa.attribute_assign_type AS assigned_to_type,  (SELECT gg.name  FROM grouper_groups gg WHERE gg.id = main_gaa.owner_group_id  ) AS assigned_to_group_name,  (SELECT gs.name  FROM grouper_stems gs WHERE gs.id = main_gaa.owner_stem_id  ) AS assigned_to_stem_name,  (SELECT gm.subject_id  FROM grouper_members gm WHERE gm.id = main_gaa.owner_member_id  ) AS assigned_to_member_subject_id,  (SELECT gad.name  FROM grouper_attribute_def gad WHERE gad.id = main_gaa.owner_attribute_def_id  ) AS assigned_to_attribute_def_name,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckType'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_type,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckOwnerId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_owner_id,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckOwnerName'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_owner_name,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckStemScope'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_stem_scope,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckArg0'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_arg0,  (SELECT gaav.value_string  FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleCheckArg1'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_check_arg1,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEl'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_el,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEnum'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_enum,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEnumArg0'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_enum_arg0,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfConditionEnumArg1'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_condition_enum_arg1,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfOwnerId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_owner_id,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfOwnerName'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_owner_name,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleIfStemScope'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_if_stem_scope,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEl'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_el,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnum'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnumArg0'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum_arg0,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnumArg1'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum_arg1,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleThenEnumArg2'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_then_enum_arg2,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleValid'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_valid,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleRunDaemon'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_run_daemon,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleActAsSubjectId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_act_as_subject_id,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleActAsSubjectIdentifier'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_act_as_subject_identifier,  (SELECT gaav.value_string   FROM grouper_attribute_assign gaa, grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn   WHERE gadn.name = 'etc:attribute:rules:ruleActAsSubjectSourceId'   AND gaav.attribute_assign_id = gaa.id   AND gaa.attribute_def_name_id = gadn.id   AND gaa.owner_attribute_assign_id = main_gaa.id   AND gaa.enabled = 'T') AS rule_act_as_subject_source_id,   main_gaa.enabled AS assignment_enabled,  main_gaa.id AS attribute_assign_id  FROM grouper_attribute_assign main_gaa, grouper_attribute_def_name main_gadn  WHERE main_gadn.name = 'etc:attribute:rules:rule'  AND main_gaa.attribute_def_name_id = main_gadn.id ;

COMMENT ON TABLE grouper_rules_v IS 'Rules setup in grouper';

COMMENT ON COLUMN grouper_rules_v.assigned_to_type IS 'assigned_to_type: attribute_assign_type of rule assignment, e.g. group, stem, etc';

COMMENT ON COLUMN grouper_rules_v.assigned_to_group_name IS 'assigned_to_group_name: if rule is assigned to group, this is the group name';

COMMENT ON COLUMN grouper_rules_v.assigned_to_stem_name IS 'assigned_to_stem_name: if rule is assigned to stem, this is the stem name';

COMMENT ON COLUMN grouper_rules_v.assigned_to_member_subject_id IS 'assigned_to_member_subject_id: if rule is assigned to member, this is the subject id';

COMMENT ON COLUMN grouper_rules_v.assigned_to_attribute_def_name IS 'assigned_to_attribute_def_name: if rule is assigned to attribute_def, this is the name of the attribute_def';

COMMENT ON COLUMN grouper_rules_v.rule_check_type IS 'rule_check_type: RuleCheckType enum of when this rule is fired and how to decides which rules are affected';

COMMENT ON COLUMN grouper_rules_v.rule_check_owner_id IS 'rule_check_owner_id: If the owner is not the object where the rule is assigned, specify id here.  Check owner affects when rule is fired.  Mutually exclusive with rule_check_owner_name';

COMMENT ON COLUMN grouper_rules_v.rule_check_owner_name IS 'rule_check_owner_name: If the owner is not the object where the rule is assigned, specify name here.  Check owner affects when rule is fired.  Mutually exclusive with rule_check_owner_id';

COMMENT ON COLUMN grouper_rules_v.rule_check_stem_scope IS 'rule_check_stem_scope: If the owner type is stem, then this is the scope: ONE or SUB';

COMMENT ON COLUMN grouper_rules_v.rule_check_arg0 IS 'rule_check_arg0: If the rule check type has arguments, this is the first';

COMMENT ON COLUMN grouper_rules_v.rule_check_arg1 IS 'rule_check_arg1: If the rule check type has arguments, this is the second';

COMMENT ON COLUMN grouper_rules_v.rule_if_condition_el IS 'rule_if_condition_el: If there is expression language to decide if the rule should fire, it is here.  Mutually exclusive with if_condition_enum';

COMMENT ON COLUMN grouper_rules_v.rule_if_condition_enum IS 'rule_if_condition_enum: If the if condition is a built in enum, that IfConditionEnum should be here.  Mutually exclusive with if_condition_el';

COMMENT ON COLUMN grouper_rules_v.rule_if_condition_enum_arg0 IS 'rule_if_condition_enum_arg0: If the if_condition_enum has arguments, this is the first';

COMMENT ON COLUMN grouper_rules_v.rule_if_condition_enum_arg1 IS 'rule_if_condition_enum_arg1: If the if_condition_enum has arguments, this is the second';

COMMENT ON COLUMN grouper_rules_v.rule_if_owner_id IS 'rule_if_owner_id: If the if condition enum has an owner, the id is here.  Mutually exclusive with rule_if_owner_name';

COMMENT ON COLUMN grouper_rules_v.rule_if_owner_name IS 'rule_if_owner_name: If the if condition enum has an owner, the name is here.  Mutually exclusive with rule_if_owner_id';

COMMENT ON COLUMN grouper_rules_v.rule_if_stem_scope IS 'rule_if_stem_scope: If the if condition enum is a stem type, this is the scope, ONE or SUB';

COMMENT ON COLUMN grouper_rules_v.rule_then_el IS 'rule_then_el: Then condition expression language if the rule fires.  Mutually exclusive with rule_then_enum';

COMMENT ON COLUMN grouper_rules_v.rule_then_enum IS 'rule_then_enum: then condition built in enum: ThenConditionEnum.  Mutually exclusive with rule_then_el';

COMMENT ON COLUMN grouper_rules_v.rule_then_enum_arg0 IS 'rule_then_enum_arg0: If the then condition enum has arguments, this is the first';

COMMENT ON COLUMN grouper_rules_v.rule_then_enum_arg1 IS 'rule_then_enum_arg1: If the then condition enum has arguments, this is the second';

COMMENT ON COLUMN grouper_rules_v.rule_then_enum_arg2 IS 'rule_then_enum_arg2: If the then condition enum has arguments, this is the third';

COMMENT ON COLUMN grouper_rules_v.rule_valid IS 'rule_valid: If the rule is valid, this will be T, else it is the error message';

COMMENT ON COLUMN grouper_rules_v.rule_run_daemon IS 'rule_run_daemon: If this rule should run a daemon.  Needs to be daemonable...';

COMMENT ON COLUMN grouper_rules_v.rule_act_as_subject_id IS 'rule_act_as_subject_id: Who this rule should act as when firing.  Mutually exclusive with rule_act_as_subject_identifier';

COMMENT ON COLUMN grouper_rules_v.rule_act_as_subject_identifier IS 'rule_act_as_subject_identifier: Who this rule should act as when firing.  Mutually exclusive with rule_act_as_subject_id';

COMMENT ON COLUMN grouper_rules_v.rule_act_as_subject_source_id IS 'rule_act_as_subject_source_id: Optional, source id of who this rule should act as';

COMMENT ON COLUMN grouper_rules_v.assignment_enabled IS 'assignment_enabled: If the rule assignment is enabled';

COMMENT ON COLUMN grouper_rules_v.attribute_assign_id IS 'attribute_assign_id: The attribute assign id in the grouper_attribute_assign table for the main rule definition';

CREATE VIEW grouper_service_role_v (service_role, group_name, name_of_service_def_name, subject_source_id, subject_id, field_name, name_of_service_def, group_display_name, group_id, service_def_id, service_name_id, member_id, field_id, display_name_of_service_name, service_stem_id) AS select distinct (CASE gf.name WHEN 'admins' THEN 'admin' WHEN 'updaters' then 'admin' when 'members' then 'user' end ) as service_role, gg.name as group_name, gadn.name as name_of_service_def_name,  gm.subject_source as subject_source_id, gm.subject_id,  gf.name as field_name, gad.name as name_of_service_def,  gg.display_name as group_display_name, gg.id as group_id, gad.id as service_def_id, gadn.id as service_name_id, gm.id as member_id, gf.id as field_id, gadn.display_name as display_name_of_service_name, gaa.owner_stem_id as service_stem_id from grouper_attribute_def_name gadn, grouper_attribute_def gad, grouper_groups gg, grouper_memberships_all_v gmav, grouper_attribute_assign gaa, grouper_stem_set gss, grouper_members gm, grouper_fields gf where gadn.attribute_def_id = gad.id and gad.attribute_def_type='service' and gaa.attribute_def_name_id = gadn.id and gad.assign_to_stem='T' and gmav.field_id = gf.id and gmav.immediate_mship_enabled='T' and gmav.owner_group_id = gg.id and gaa.owner_stem_id = gss.then_has_stem_id and gg.parent_stem=gss.if_has_stem_id and gaa.enabled='T' and gmav.member_id = gm.id and gf.name in ('admins', 'members', 'readers', 'updaters') ;

COMMENT ON TABLE grouper_service_role_v IS 'grouper_service_role_v: shows service admin or user relationships to folders/groups';

COMMENT ON COLUMN grouper_service_role_v.service_role IS 'service_role: admin or user is the subject is an admin/updater/reader of a group in the service folder, or user is the subject is a member of a group in the service folder';

COMMENT ON COLUMN grouper_service_role_v.group_name IS 'group_name: group name in the service that the user is an admin or user of';

COMMENT ON COLUMN grouper_service_role_v.name_of_service_def_name IS 'name_of_service_def_name: name of the service dev name, this generally is the system name of the service';

COMMENT ON COLUMN grouper_service_role_v.subject_source_id IS 'subject_source_id: subject source id of the subject who is the admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.subject_id IS 'subject_id: subject id of the subject who is the admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.field_name IS 'field_name: field of the membership of the subject who is the admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.name_of_service_def IS 'name_of_service_def: name of the attribute definition of the service';

COMMENT ON COLUMN grouper_service_role_v.group_display_name IS 'group_display_name: display name of the group that the subject is an admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.group_id IS 'group_id: group id of the group that the subject is an admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.service_def_id IS 'service_def_id: id of the attribute definition that is related to the service';

COMMENT ON COLUMN grouper_service_role_v.service_name_id IS 'service_name_id: id of the attribute name that is the service';

COMMENT ON COLUMN grouper_service_role_v.member_id IS 'member_id: id in the member table of the subject who is an admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.field_id IS 'field_id: id of the field for the membership of the subject who an admin or user of the service';

COMMENT ON COLUMN grouper_service_role_v.display_name_of_service_name IS 'display_name_of_service_name: display name of the service definition name';

COMMENT ON COLUMN grouper_service_role_v.service_stem_id IS 'service_stem_id: id of the stem where the service tag is assigned';

CREATE VIEW grouper_pit_memberships_lw_v (ID, MEMBERSHIP_ID, MEMBERSHIP_SOURCE_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, MEMBERSHIP_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, GROUP_SET_ACTIVE, GROUP_SET_START_TIME, GROUP_SET_END_TIME, MEMBERSHIP_ACTIVE, MEMBERSHIP_START_TIME, MEMBERSHIP_END_TIME, DEPTH, GROUP_SET_PARENT_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE) AS select gpmship.id || ':' || gpgs.id as membership_id, gpmship.id as immediate_membership_id, gpmship.source_id as membership_source_id, gpgs.id as group_set_id, gpmship.member_id, gpgs.field_id, gpmship.field_id, gpgs.owner_id, gpgs.owner_attr_def_id, gpgs.owner_group_id, gpgs.owner_stem_id, gpgs.active, gpgs.start_time, gpgs.end_time, gpmship.active, gpmship.start_time, gpmship.end_time, gpgs.depth, gpgs.parent_id as group_set_parent_id,  (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F'  end) as the_active from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and ((gpmship.start_time >= gpgs.start_time and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)) or (gpgs.start_time >= gpmship.start_time and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)));

COMMENT ON TABLE grouper_pit_memberships_lw_v IS 'Grouper_pit_memberships_lw_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.ID IS 'ID: id of this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: id of the immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBERSHIP_SOURCE_ID IS 'MEMBERSHIP_SOURCE_ID: id of the actual (non-pit) immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.GROUP_SET_ID IS 'GROUP_SET_ID: id of the group set that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBER_ID IS 'MEMBER_ID: member id';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.FIELD_ID IS 'FIELD_ID: field id';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBERSHIP_FIELD_ID IS 'MEMBERSHIP_FIELD_ID: field id of the immediate (or composite) membership that causes this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.OWNER_ID IS 'OWNER_ID: owner id';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.OWNER_ATTR_DEF_ID IS 'OWNER_ATTR_DEF_ID: owner attribute def id if applicable';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.OWNER_GROUP_ID IS 'OWNER_GROUP_ID: owner group id if applicable';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.OWNER_STEM_ID IS 'OWNER_STEM_ID: owner stem id if applicable';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.GROUP_SET_ACTIVE IS 'GROUP_SET_ACTIVE: whether the group set is active';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.GROUP_SET_START_TIME IS 'GROUP_SET_START_TIME: start time of the group set';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.GROUP_SET_END_TIME IS 'GROUP_SET_END_TIME: end time of the group set';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBERSHIP_ACTIVE IS 'MEMBERSHIP_ACTIVE: whether the immediate (or composite) membership is active';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBERSHIP_START_TIME IS 'MEMBERSHIP_START_TIME: start time of the immediate (or composite) membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.MEMBERSHIP_END_TIME IS 'MEMBERSHIP_END_TIME: end time of the immediate (or composite) membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.DEPTH IS 'DEPTH: depth of this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.GROUP_SET_PARENT_ID IS 'GROUP_SET_PARENT_ID: parent group set';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.THE_START_TIME IS 'THE_START_TIME: the real start time of this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.THE_END_TIME IS 'THE_END_TIME: the real end time of this membership';

COMMENT ON COLUMN grouper_pit_memberships_lw_v.THE_ACTIVE IS 'THE_ACTIVE: if this memberships is still active';

CREATE VIEW grouper_pit_mship_group_lw_v (GROUP_NAME, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, GROUP_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gpg.name as group_name, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gpg.source_id as group_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, gpmship.source_id || ':' || gpgs.source_id as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_groups gpg, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gpg.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )    or    (       gpgs.start_time >= gpmship.start_time       and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) ) ;

COMMENT ON TABLE grouper_pit_mship_group_lw_v IS 'grouper_pit_mship_group_lw_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active.  Holds the group information for memberships or privileges';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.GROUP_NAME IS 'GROUP_NAME: group name is extension and ancestor folder extensions separated by colons';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.FIELD_NAME IS 'FIELD_NAME: members, admins, readers, etc';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: subject source id';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.SUBJECT_ID IS 'SUBJECT_ID: subject id in the source';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.MEMBER_ID IS 'MEMBER_ID: uuid in the grouper_members table (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.FIELD_ID IS 'FIELD_ID: uuid in the grouper_fields table (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.GROUP_ID IS 'GROUP_ID: uuid of the grouper group (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.THE_START_TIME IS 'THE_START_TIME: micros since 1970 UTC that the membership started';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.THE_END_TIME IS 'THE_END_TIME: micros since 1970 UTC that the membership ended or null if still active';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.THE_ACTIVE IS 'THE_ACTIVE: T or F for if this membership is still active';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: membership id and colon and group set id which is the effective membership id.  might not exist in grouper if from past';

COMMENT ON COLUMN grouper_pit_mship_group_lw_v.IMM_MEMBERSHIP_ID IS 'IMM_MEMBERSHIP_ID: membership id for this immediate membership.  might not exist in grouper if from past';

CREATE VIEW grouper_pit_mship_stem_lw_v (STEM_NAME, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, STEM_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gps.name as stem_name, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gps.source_id as stem_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, gpmship.source_id || ':' || gpgs.source_id as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_stems gps, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gps.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )   or   (      gpgs.start_time >= gpmship.start_time       and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) );

COMMENT ON TABLE grouper_pit_mship_stem_lw_v IS 'grouper_pit_mship_stem_lw_v holds one record for each immediate, composite and effective stem privilege in the system that currently exists or has existed in the past for members to stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.STEM_NAME IS 'STEM_NAME: stem name is extension and ancestor folder extensions separated by colons';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.FIELD_NAME IS 'FIELD_NAME: admins, creators, etc';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: subject source id';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.SUBJECT_ID IS 'SUBJECT_ID: subject id in the source';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.MEMBER_ID IS 'MEMBER_ID: uuid in the grouper_members table (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.FIELD_ID IS 'FIELD_ID: uuid in the grouper_fields table (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.STEM_ID IS 'STEM_ID: uuid of the grouper stem (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.THE_START_TIME IS 'THE_START_TIME: micros since 1970 UTC that the membership started';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.THE_END_TIME IS 'THE_END_TIME: micros since 1970 UTC that the membership ended or null if still active';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.THE_ACTIVE IS 'THE_ACTIVE: T or F for if this membership is still active';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: membership id and colon and group set id which is the effective membership id.  might not exist in grouper if from past';

COMMENT ON COLUMN grouper_pit_mship_stem_lw_v.IMM_MEMBERSHIP_ID IS 'IMM_MEMBERSHIP_ID: membership id for this immediate membership.  might not exist in grouper if from past';

CREATE VIEW grouper_pit_mship_attr_lw_v (NAME_OF_ATTRIBUTE_DEF, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, ATTRIBUTE_DEF_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gpa.name as name_of_attribute_def, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gpa.source_id as attribute_def_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, gpmship.source_id || ':' || gpgs.source_id as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_attribute_def gpa, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gpa.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )    or    (       gpgs.start_time >= gpmship.start_time      and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) ) ;

COMMENT ON TABLE grouper_pit_mship_attr_lw_v IS 'grouper_pit_mship_attr_lw_v holds one record for each immediate, composite and effective atribute def privilege in the system that currently exists or has existed in the past for members to attribute def (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.NAME_OF_ATTRIBUTE_DEF IS 'NAME_OF_ATTRIBUTE_DEF: name of attribute def is extension and ancestor folder extensions separated by colons';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.FIELD_NAME IS 'FIELD_NAME: admins, creators, etc';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.SUBJECT_SOURCE IS 'SUBJECT_SOURCE: subject source id';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.SUBJECT_ID IS 'SUBJECT_ID: subject id in the source';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.MEMBER_ID IS 'MEMBER_ID: uuid in the grouper_members table (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.FIELD_ID IS 'FIELD_ID: uuid in the grouper_fields table (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.ATTRIBUTE_DEF_ID IS 'ATTRIBUTE_DEF_ID: uuid of the grouper attribute def (note, could be different than real one if deleted and/or recreated)';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.THE_START_TIME IS 'THE_START_TIME: micros since 1970 UTC that the membership started';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.THE_END_TIME IS 'THE_END_TIME: micros since 1970 UTC that the membership ended or null if still active';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.THE_ACTIVE IS 'THE_ACTIVE: T or F for if this membership is still active';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.MEMBERSHIP_ID IS 'MEMBERSHIP_ID: membership id and colon and group set id which is the effective membership id.  might not exist in grouper if from past';

COMMENT ON COLUMN grouper_pit_mship_attr_lw_v.IMM_MEMBERSHIP_ID IS 'IMM_MEMBERSHIP_ID: membership id for this immediate membership.  might not exist in grouper if from past';

CREATE VIEW grouper_recent_mships_conf_v (group_name_from, group_uuid_from, recent_micros, group_uuid_to, group_name_to, include_eligible) AS select distinct   gg.name group_name_from,  gaaagv_groupUuidFrom.value_string group_uuid_from,  gaaagv_recentMembershipsMicros.value_integer recent_micros,  gaaagv_groupUuidFrom.group_id group_uuid_to,  gaaagv_groupUuidFrom.group_name group_name_to,   gaaagv_includeEligible.value_string include_eligible   from   grouper_aval_asn_asn_group_v gaaagv_recentMembershipsMicros,  grouper_aval_asn_asn_group_v gaaagv_groupUuidFrom,  grouper_aval_asn_asn_group_v gaaagv_includeEligible,  grouper_groups gg  where gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_groupUuidFrom.attribute_assign_id1  and gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_includeEligible.attribute_assign_id1  and gaaagv_recentMembershipsMicros.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsMicros'  and gaaagv_groupUuidFrom.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsGroupUuidFrom'  and gaaagv_includeEligible.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsIncludeCurrent'  and gaaagv_recentMembershipsMicros.value_integer > 0  and gaaagv_recentMembershipsMicros.value_integer is not null  and gaaagv_groupUuidFrom.value_string is not null  and gaaagv_includeEligible.value_string is not null  and (gaaagv_includeEligible.value_string = 'T' or gaaagv_includeEligible.value_string = 'F')  and gg.id = gaaagv_groupUuidFrom.value_string ;

COMMENT ON TABLE grouper_recent_mships_conf_v IS 'Contains one row for each recent membership configured on a group';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_name_from IS 'group_name_from: group name of the group where the recent memberships are sourced from';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_uuid_from IS 'group_uuid_from: group uuid of the group where the recent memberships are sourced from';

COMMENT ON COLUMN grouper_recent_mships_conf_v.recent_micros IS 'recent_micros: number of microseconds of recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_uuid_to IS 'group_uuid_to: uuid of the group which has the destination for the recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_name_to IS 'group_name_to: name of the group which has the destination for the recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf_v.include_eligible IS 'include_eligible: T or F if eligible subjects are included';

CREATE VIEW grouper_recent_mships_load_v (group_name, subject_source_id, subject_id) AS select grmc.group_name_to as group_name, gpmglv.subject_source as subject_source_id, gpmglv.subject_id as subject_id from grouper_recent_mships_conf grmc,  grouper_pit_mship_group_lw_v gpmglv, grouper_time gt, grouper_members gm where gm.id = gpmglv.member_id and gm.subject_resolution_deleted = 'F' and gt.time_label = 'now' and (gpmglv.group_id = grmc.group_uuid_from or gpmglv.group_name = grmc.group_name_from) and gpmglv.subject_source != 'g:gsa' and gpmglv.field_name = 'members' and (gpmglv.the_end_time is null or gpmglv.the_end_time >= gt.utc_micros_since_1970 - grmc.recent_micros) and ( grmc.include_eligible = 'T' or not exists (select 1 from grouper_memberships mship2, grouper_group_set gs2 WHERE mship2.owner_id = gs2.member_id AND mship2.field_id = gs2.member_field_id and gs2.field_id = mship2.field_id and mship2.member_id = gm.id and gs2.field_id = gpmglv.field_id and gs2.owner_id = grmc.group_uuid_from and mship2.enabled = 'T'));

COMMENT ON TABLE grouper_recent_mships_load_v IS 'Contains one row for each recent membership in a group for the loader';

COMMENT ON COLUMN grouper_recent_mships_load_v.group_name IS 'group_name: group name of the loaded group from recent memberships';

COMMENT ON COLUMN grouper_recent_mships_load_v.subject_source_id IS 'subject_source_id: subject source of subject in recent membership';

COMMENT ON COLUMN grouper_recent_mships_load_v.subject_id IS 'subject_id: subject id of subject in recent membership';

insert into grouper_ddl (id, object_name, db_version, last_updated, history) values 
('c08d3e076fdb4c41acdafe5992e5dc4d', 'Grouper', 36, to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), 
to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V0 to V36, ');
commit;

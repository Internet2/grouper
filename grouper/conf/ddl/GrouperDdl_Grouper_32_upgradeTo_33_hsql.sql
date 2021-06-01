ALTER TABLE GROUPER_MEMBERS
    ADD COLUMN subject_resolution_resolvable VARCHAR(1) DEFAULT 'T';

ALTER TABLE GROUPER_MEMBERS
    ADD COLUMN subject_resolution_deleted VARCHAR(1) DEFAULT 'F' BEFORE subject_resolution_resolvable;

CREATE INDEX member_resolvable_idx ON GROUPER_MEMBERS (subject_resolution_resolvable);

CREATE INDEX member_deleted_idx ON GROUPER_MEMBERS (subject_resolution_deleted);

update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;
update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;
commit;

CREATE TABLE grouper_time
(
    time_label VARCHAR(10) NOT NULL,
    the_utc_timestamp TIMESTAMP NOT NULL,
    this_tz_timestamp TIMESTAMP NOT NULL,
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

CREATE VIEW grouper_pit_memberships_lw_v (ID, MEMBERSHIP_ID, MEMBERSHIP_SOURCE_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, MEMBERSHIP_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, GROUP_SET_ACTIVE, GROUP_SET_START_TIME, GROUP_SET_END_TIME, MEMBERSHIP_ACTIVE, MEMBERSHIP_START_TIME, MEMBERSHIP_END_TIME, DEPTH, GROUP_SET_PARENT_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE) AS select gpmship.id || ':' || gpgs.id as membership_id, gpmship.id as immediate_membership_id, gpmship.source_id as membership_source_id, gpgs.id as group_set_id, gpmship.member_id, gpgs.field_id, gpmship.field_id, gpgs.owner_id, gpgs.owner_attr_def_id, gpgs.owner_group_id, gpgs.owner_stem_id, gpgs.active, gpgs.start_time, gpgs.end_time, gpmship.active, gpmship.start_time, gpmship.end_time, gpgs.depth, gpgs.parent_id as group_set_parent_id,  (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F'  end) as the_active from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and ((gpmship.start_time >= gpgs.start_time and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)) or (gpgs.start_time >= gpmship.start_time and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)));

CREATE VIEW grouper_pit_mship_group_lw_v (GROUP_NAME, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, GROUP_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gpg.name as group_name, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gpg.source_id as group_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, gpmship.source_id || ':' || gpgs.source_id as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_groups gpg, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gpg.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )    or    (       gpgs.start_time >= gpmship.start_time       and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) ) ;

CREATE VIEW grouper_pit_mship_stem_lw_v (STEM_NAME, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, STEM_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gps.name as stem_name, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gps.source_id as stem_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, gpmship.source_id || ':' || gpgs.source_id as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_stems gps, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gps.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )   or   (      gpgs.start_time >= gpmship.start_time       and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) );

CREATE VIEW grouper_pit_mship_attr_lw_v (NAME_OF_ATTRIBUTE_DEF, FIELD_NAME, SUBJECT_SOURCE, SUBJECT_ID, MEMBER_ID, FIELD_ID, ATTRIBUTE_DEF_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE, MEMBERSHIP_ID, IMM_MEMBERSHIP_ID) AS select gpa.name as name_of_attribute_def, gpf.name as field_name, gpm.subject_source, gpm.subject_id, gpm.source_id as member_id, gpf.source_id as field_id, gpa.source_id as attribute_def_id, (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, gpmship.source_id || ':' || gpgs.source_id as membership_id, gpmship.source_id as imm_membership_id from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_attribute_def gpa, grouper_pit_fields gpf where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and gpmship.member_id = gpm.ID and gpa.id = gpgs.owner_id and gpgs.FIELD_ID = gpf.ID and (    (       gpmship.start_time >= gpgs.start_time       and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)    )    or    (       gpgs.start_time >= gpmship.start_time      and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)    ) ) ;

CREATE VIEW grouper_recent_mships_conf_v (group_name_from, group_uuid_from, recent_micros, group_uuid_to, group_name_to, include_eligible) AS select distinct   gg.name group_name_from,  gaaagv_groupUuidFrom.value_string group_uuid_from,  gaaagv_recentMembershipsMicros.value_integer recent_micros,  gaaagv_groupUuidFrom.group_id group_uuid_to,  gaaagv_groupUuidFrom.group_name group_name_to,   gaaagv_includeEligible.value_string include_eligible   from   grouper_aval_asn_asn_group_v gaaagv_recentMembershipsMicros,  grouper_aval_asn_asn_group_v gaaagv_groupUuidFrom,  grouper_aval_asn_asn_group_v gaaagv_includeEligible,  grouper_groups gg  where gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_groupUuidFrom.attribute_assign_id1  and gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_includeEligible.attribute_assign_id1  and gaaagv_recentMembershipsMicros.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsMicros'  and gaaagv_groupUuidFrom.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsGroupUuidFrom'  and gaaagv_includeEligible.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsIncludeCurrent'  and gaaagv_recentMembershipsMicros.value_integer > 0  and gaaagv_recentMembershipsMicros.value_integer is not null  and gaaagv_groupUuidFrom.value_string is not null  and gaaagv_includeEligible.value_string is not null  and (gaaagv_includeEligible.value_string = 'T' or gaaagv_includeEligible.value_string = 'F')  and gg.id = gaaagv_groupUuidFrom.value_string ;

CREATE VIEW grouper_recent_mships_load_v (group_name, subject_source_id, subject_id) AS select grmc.group_name_to as group_name, gpmglv.subject_source as subject_source_id, gpmglv.subject_id as subject_id from grouper_recent_mships_conf grmc,  grouper_pit_mship_group_lw_v gpmglv, grouper_time gt, grouper_members gm where gm.id = gpmglv.member_id and gm.subject_resolution_deleted = 'F' and gt.time_label = 'now' and (gpmglv.group_id = grmc.group_uuid_from or gpmglv.group_name = grmc.group_name_from) and gpmglv.subject_source != 'g:gsa' and gpmglv.field_name = 'members' and (gpmglv.the_end_time is null or gpmglv.the_end_time >= gt.utc_micros_since_1970 - grmc.recent_micros) and ( grmc.include_eligible = 'T' or not exists (select 1 from grouper_memberships mship2, grouper_group_set gs2 WHERE mship2.owner_id = gs2.member_id AND mship2.field_id = gs2.member_field_id and gs2.field_id = mship2.field_id and mship2.member_id = gm.id and gs2.field_id = gpmglv.field_id and gs2.owner_id = grmc.group_uuid_from and mship2.enabled = 'T'));

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V33, ' || history) from 1 for 3500), db_version = 33 where object_name = 'Grouper';

commit;

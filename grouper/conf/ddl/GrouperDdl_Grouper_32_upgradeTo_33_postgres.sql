ALTER TABLE grouper_members ADD COLUMN subject_resolution_resolvable VARCHAR(1);
ALTER TABLE grouper_members ADD COLUMN subject_resolution_deleted VARCHAR(1);
CREATE INDEX member_resolvable_idx ON grouper_members (subject_resolution_resolvable);
CREATE INDEX member_deleted_idx ON grouper_members (subject_resolution_deleted);
update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;
update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;
commit;

ALTER TABLE grouper_members ALTER COLUMN subject_resolution_resolvable SET NOT NULL;
ALTER TABLE grouper_members ALTER COLUMN subject_resolution_resolvable SET DEFAULT 'T';
ALTER TABLE grouper_members ALTER COLUMN subject_resolution_deleted SET NOT NULL;
ALTER TABLE grouper_members ALTER COLUMN subject_resolution_deleted SET DEFAULT 'F';

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

CREATE VIEW grouper_pit_memberships_lw_v (ID, MEMBERSHIP_ID, MEMBERSHIP_SOURCE_ID, GROUP_SET_ID, MEMBER_ID, FIELD_ID, MEMBERSHIP_FIELD_ID, OWNER_ID, OWNER_ATTR_DEF_ID, OWNER_GROUP_ID, OWNER_STEM_ID, GROUP_SET_ACTIVE, GROUP_SET_START_TIME, GROUP_SET_END_TIME, MEMBERSHIP_ACTIVE, MEMBERSHIP_START_TIME, MEMBERSHIP_END_TIME, DEPTH, GROUP_SET_PARENT_ID, THE_START_TIME, THE_END_TIME, THE_ACTIVE) AS select gpmship.id || ':' || gpgs.id as membership_id, gpmship.id as immediate_membership_id, gpmship.source_id as membership_source_id, gpgs.id as group_set_id, gpmship.member_id, gpgs.field_id, gpmship.field_id, gpgs.owner_id, gpgs.owner_attr_def_id, gpgs.owner_group_id, gpgs.owner_stem_id, gpgs.active, gpgs.start_time, gpgs.end_time, gpmship.active, gpmship.start_time, gpmship.end_time, gpgs.depth, gpgs.parent_id as group_set_parent_id,  (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F'  end) as the_active from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and ((gpmship.start_time >= gpgs.start_time and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null)) or (gpgs.start_time >= gpmship.start_time and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)));

COMMENT ON VIEW grouper_pit_memberships_lw_v IS 'Grouper_pit_memberships_lw_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active';

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

COMMENT ON VIEW grouper_pit_mship_group_lw_v IS 'grouper_pit_mship_group_lw_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active.  Holds the group information for memberships or privileges';

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

COMMENT ON VIEW grouper_pit_mship_stem_lw_v IS 'grouper_pit_mship_stem_lw_v holds one record for each immediate, composite and effective stem privilege in the system that currently exists or has existed in the past for members to stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active';

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

COMMENT ON VIEW grouper_pit_mship_attr_lw_v IS 'grouper_pit_mship_attr_lw_v holds one record for each immediate, composite and effective atribute def privilege in the system that currently exists or has existed in the past for members to attribute def (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active';

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

COMMENT ON VIEW grouper_recent_mships_conf_v IS 'Contains one row for each recent membership configured on a group';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_name_from IS 'group_name_from: group name of the group where the recent memberships are sourced from';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_uuid_from IS 'group_uuid_from: group uuid of the group where the recent memberships are sourced from';

COMMENT ON COLUMN grouper_recent_mships_conf_v.recent_micros IS 'recent_micros: number of microseconds of recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_uuid_to IS 'group_uuid_to: uuid of the group which has the destination for the recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf_v.group_name_to IS 'group_name_to: name of the group which has the destination for the recent memberships';

COMMENT ON COLUMN grouper_recent_mships_conf_v.include_eligible IS 'include_eligible: T or F if eligible subjects are included';

CREATE VIEW grouper_recent_mships_load_v (group_name, subject_source_id, subject_id) AS select grmc.group_name_to as group_name, gpmglv.subject_source as subject_source_id, gpmglv.subject_id as subject_id from grouper_recent_mships_conf grmc,  grouper_pit_mship_group_lw_v gpmglv, grouper_time gt, grouper_members gm where gm.id = gpmglv.member_id and gm.subject_resolution_deleted = 'F' and gt.time_label = 'now' and (gpmglv.group_id = grmc.group_uuid_from or gpmglv.group_name = grmc.group_name_from) and gpmglv.subject_source != 'g:gsa' and gpmglv.field_name = 'members' and (gpmglv.the_end_time is null or gpmglv.the_end_time >= gt.utc_micros_since_1970 - grmc.recent_micros) and ( grmc.include_eligible = 'T' or not exists (select 1 from grouper_memberships mship2, grouper_group_set gs2 WHERE mship2.owner_id = gs2.member_id AND mship2.field_id = gs2.member_field_id and gs2.field_id = mship2.field_id and mship2.member_id = gm.id and gs2.field_id = gpmglv.field_id and gs2.owner_id = grmc.group_uuid_from and mship2.enabled = 'T'));

COMMENT ON VIEW grouper_recent_mships_load_v IS 'Contains one row for each recent membership in a group for the loader';

COMMENT ON COLUMN grouper_recent_mships_load_v.group_name IS 'group_name: group name of the loaded group from recent memberships';

COMMENT ON COLUMN grouper_recent_mships_load_v.subject_source_id IS 'subject_source_id: subject source of subject in recent membership';

COMMENT ON COLUMN grouper_recent_mships_load_v.subject_id IS 'subject_id: subject id of subject in recent membership';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V33, ' || history) from 1 for 3500), db_version = 33 where object_name = 'Grouper';
commit;

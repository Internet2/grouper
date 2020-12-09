ALTER TABLE grouper_sync_log ADD COLUMN description_clob varchar(10000000);

ALTER TABLE grouper_sync_log ADD COLUMN description_bytes BIGINT;
	
COMMENT ON COLUMN grouper_sync_log.description_clob IS 'description for large data';

COMMENT ON COLUMN grouper_sync_log.description_bytes IS 'size of description in bytes';

UPDATE grouper_sync_log set description_bytes = length(description);
commit;

DROP INDEX grouper_sync_mship_gr_idx;
CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_id, grouper_sync_group_id, grouper_sync_member_id);

ALTER TABLE grouper_sync
    ADD COLUMN last_full_metadata_sync_start TIMESTAMP;

ALTER TABLE grouper_sync
    ADD COLUMN last_full_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_group
    ADD COLUMN last_group_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_group
    ADD COLUMN last_group_metadata_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_job
    ADD COLUMN last_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_log
    ADD COLUMN sync_timestamp_start TIMESTAMP;

ALTER TABLE grouper_sync_member
    ADD COLUMN last_user_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_member
    ADD COLUMN last_user_metadata_sync_start TIMESTAMP;    
    
COMMENT ON COLUMN grouper_sync.last_full_sync_start IS 'start time of last successful full sync';

COMMENT ON COLUMN grouper_sync.last_full_metadata_sync_start IS 'start time of last successful full metadata sync';

COMMENT ON COLUMN grouper_sync_job.last_sync_start IS 'start time of this job';

COMMENT ON COLUMN grouper_sync_log.sync_timestamp_start IS 'start of sync operation for log';

COMMENT ON COLUMN grouper_sync_group.last_group_sync_start IS 'start of last successful group sync';

COMMENT ON COLUMN grouper_sync_group.last_group_metadata_sync_start IS 'start of last successful group metadata sync';

COMMENT ON COLUMN grouper_sync_member.last_user_sync_start IS 'start of last successful user sync';

COMMENT ON COLUMN grouper_sync_member.last_user_metadata_sync_start IS 'start of last successful user metadata sync';

create view grouper_sync_membership_v (g_group_name, g_group_id_index, u_source_id, u_subject_id, u_subject_identifier, m_in_target, m_id, m_in_target_insert_or_exists, m_in_target_start, m_in_target_end, m_last_updated, m_membership_id, m_membership_id2, m_metadata_updated, m_error_message, m_error_timestamp, s_id, s_sync_engine, s_provisioner_name, u_id, u_member_id, u_in_target, u_in_target_insert_or_exists, u_in_target_start, u_in_target_end, u_provisionable, u_provisionable_start, u_provisionable_end, u_last_updated, u_last_user_sync_start, u_last_user_sync, u_last_user_meta_sync_start, u_last_user_metadata_sync, u_member_from_id2, u_member_from_id3, u_member_to_id2, u_member_to_id3, u_metadata_updated, u_last_time_work_was_done, u_error_message, u_error_timestamp, g_id, g_group_id, g_provisionable, g_in_target, g_in_target_insert_or_exists, g_in_target_start, g_in_target_end, g_provisionable_start, g_provisionable_end, g_last_updated, g_last_group_sync_start, g_last_group_sync, g_last_group_meta_sync_start, g_last_group_metadata_sync, g_group_from_id2, g_group_from_id3, g_group_to_id2, g_group_to_id3, g_metadata_updated, g_error_message, g_error_timestamp, g_last_time_work_was_done) as select g.group_name as g_group_name, g.group_id_index as g_group_id_index, u.source_id as u_source_id, u.subject_id as u_subject_id, u.subject_identifier as u_subject_identifier, m.in_target as m_in_target, m.id as m_id, m.in_target_insert_or_exists as m_in_target_insert_or_exists, m.in_target_start as m_in_target_start, m.in_target_end as m_in_target_end, m.last_updated as m_last_updated, m.membership_id as m_membership_id, m.membership_id2 as m_membership_id2, m.metadata_updated as m_metadata_updated, m.error_message as m_error_message, m.error_timestamp as m_error_timestamp, s.id as s_id, s.sync_engine as s_sync_engine, s.provisioner_name as s_provisioner_name, u.id as u_id, u.member_id as u_member_id, u.in_target as u_in_target, u.in_target_insert_or_exists as u_in_target_insert_or_exists, u.in_target_start as u_in_target_start, u.in_target_end as u_in_target_end, u.provisionable as u_provisionable, u.provisionable_start as u_provisionable_start, u.provisionable_end as u_provisionable_end, u.last_updated as u_last_updated, u.last_user_sync_start as u_last_user_sync_start, u.last_user_sync as u_last_user_sync, u.last_user_metadata_sync_start as u_last_user_meta_sync_start, u.last_user_metadata_sync as u_last_user_metadata_sync, u.member_from_id2 as u_member_from_id2, u.member_from_id3 as u_member_from_id3, u.member_to_id2 as u_member_to_id2, u.member_to_id3 as u_member_to_id3, u.metadata_updated as u_metadata_updated, u.last_time_work_was_done as u_last_time_work_was_done, u.error_message as u_error_message, u.error_timestamp as u_error_timestamp, g.id as g_id, g.group_id as g_group_id, g.provisionable as g_provisionable, g.in_target as g_in_target, g.in_target_insert_or_exists as g_in_target_insert_or_exists, g.in_target_start as g_in_target_start, g.in_target_end as g_in_target_end, g.provisionable_start as g_provisionable_start, g.provisionable_end as g_provisionable_end, g.last_updated as g_last_updated, g.last_group_sync_start as g_last_group_sync_start, g.last_group_sync as g_last_group_sync, g.last_group_metadata_sync_start as g_last_group_meta_sync_start, g.last_group_metadata_sync as g_last_group_metadata_sync, g.group_from_id2 as g_group_from_id2, g.group_from_id3 as g_group_from_id3, g.group_to_id2 as g_group_to_id2, g.group_to_id3 as g_group_to_id3, g.metadata_updated as g_metadata_updated, g.error_message as g_error_message, g.error_timestamp as g_error_timestamp, g.last_time_work_was_done as g_last_time_work_was_done  from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id;

COMMENT ON VIEW grouper_sync_membership_v IS 'Memberships for provisioning joined with the group, member, and sync tables';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_NAME IS 'G_GROUP_NAME: grouper group system name';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_ID_INDEX IS 'G_GROUP_ID_INDEX: grouper group id index';

COMMENT ON COLUMN grouper_sync_membership_v.U_SOURCE_ID IS 'U_SOURCE_ID: subject source id';

COMMENT ON COLUMN grouper_sync_membership_v.U_SUBJECT_ID IS 'U_SUBJECT_ID: subject id';

COMMENT ON COLUMN grouper_sync_membership_v.U_SUBJECT_IDENTIFIER IS 'U_SUBJECT_IDENTIFIER: subject identifier0';

COMMENT ON COLUMN grouper_sync_membership_v.M_IN_TARGET IS 'M_IN_TARGET: T/F if provisioned to target';

COMMENT ON COLUMN grouper_sync_membership_v.M_ID IS 'M_ID: sync membership id';

COMMENT ON COLUMN grouper_sync_membership_v.M_IN_TARGET_INSERT_OR_EXISTS IS 'M_IN_TARGET_INSERT_OR_EXISTS: T/F if it was inserted into target or already existed';

COMMENT ON COLUMN grouper_sync_membership_v.M_IN_TARGET_START IS 'M_IN_TARGET_START: timestamp was inserted or detected to be in target';

COMMENT ON COLUMN grouper_sync_membership_v.M_IN_TARGET_END IS 'M_IN_TARGET_END: timestamp was removed from target or detected not there';

COMMENT ON COLUMN grouper_sync_membership_v.M_LAST_UPDATED IS 'M_LAST_UPDATED: when sync membership last updated';

COMMENT ON COLUMN grouper_sync_membership_v.M_MEMBERSHIP_ID IS 'M_MEMBERSHIP_ID: link membership id';

COMMENT ON COLUMN grouper_sync_membership_v.M_MEMBERSHIP_ID2 IS 'M_MEMBERSHIP_ID2: link membership id2';

COMMENT ON COLUMN grouper_sync_membership_v.M_METADATA_UPDATED IS 'M_METADATA_UPDATED: when metadata e.g. links was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.M_ERROR_MESSAGE IS 'M_ERROR_MESSAGE: error message when last operation occurred unless a success happened afterward';

COMMENT ON COLUMN grouper_sync_membership_v.M_ERROR_TIMESTAMP IS 'M_ERROR_TIMESTAMP: timestamp last error occurred unless a success happened afterward';

COMMENT ON COLUMN grouper_sync_membership_v.S_ID IS 'S_ID: sync id overall';

COMMENT ON COLUMN grouper_sync_membership_v.S_SYNC_ENGINE IS 'S_SYNC_ENGINE: sync engine';

COMMENT ON COLUMN grouper_sync_membership_v.S_PROVISIONER_NAME IS 'S_PROVISIONER_NAME: name of provisioner';

COMMENT ON COLUMN grouper_sync_membership_v.U_ID IS 'U_ID: sync member id';

COMMENT ON COLUMN grouper_sync_membership_v.U_MEMBER_ID IS 'U_MEMBER_ID: grouper member uuid for subject';

COMMENT ON COLUMN grouper_sync_membership_v.U_IN_TARGET IS 'U_IN_TARGET: T/F if entity is in target';

COMMENT ON COLUMN grouper_sync_membership_v.U_IN_TARGET_INSERT_OR_EXISTS IS 'U_IN_TARGET_INSERT_OR_EXISTS: T/F if grouper inserted the entity or if it already existed';

COMMENT ON COLUMN grouper_sync_membership_v.U_IN_TARGET_START IS 'U_IN_TARGET_START: when this entity started being in target or detected there';

COMMENT ON COLUMN grouper_sync_membership_v.U_IN_TARGET_END IS 'U_IN_TARGET_END: when this entity stopped being in target or detected not there';

COMMENT ON COLUMN grouper_sync_membership_v.U_PROVISIONABLE IS 'U_PROVISIONABLE: T/F if the entity is provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.U_PROVISIONABLE_START IS 'U_PROVISIONABLE_START: when this entity started being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.U_PROVISIONABLE_END IS 'U_PROVISIONABLE_END: when this entity stopped being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.U_LAST_UPDATED IS 'U_LAST_UPDATED: when the sync member was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.U_LAST_USER_SYNC_START IS 'U_LAST_USER_SYNC_START: when the user was last overall sync started';

COMMENT ON COLUMN grouper_sync_membership_v.U_LAST_USER_SYNC IS 'U_LAST_USER_SYNC: when the user was last overall synced';

COMMENT ON COLUMN grouper_sync_membership_v.U_LAST_USER_META_SYNC_START IS 'U_LAST_USER_META_SYNC_START: when the metadata was sync started';

COMMENT ON COLUMN grouper_sync_membership_v.U_LAST_USER_METADATA_SYNC IS 'U_LAST_USER_METADATA_SYNC: when the metadata was last synced';

COMMENT ON COLUMN grouper_sync_membership_v.U_MEMBER_FROM_ID2 IS 'U_MEMBER_FROM_ID2: link data from id2';

COMMENT ON COLUMN grouper_sync_membership_v.U_MEMBER_FROM_ID3 IS 'U_MEMBER_FROM_ID3: link data from id3';

COMMENT ON COLUMN grouper_sync_membership_v.U_MEMBER_TO_ID2 IS 'U_MEMBER_TO_ID2: link data to id2';

COMMENT ON COLUMN grouper_sync_membership_v.U_MEMBER_TO_ID3 IS 'U_MEMBER_TO_ID3: link data to id3';

COMMENT ON COLUMN grouper_sync_membership_v.U_METADATA_UPDATED IS 'U_METADATA_UPDATED: when metadata was last updated for entity';

COMMENT ON COLUMN grouper_sync_membership_v.U_LAST_TIME_WORK_WAS_DONE IS 'U_LAST_TIME_WORK_WAS_DONE: time last work was done on user object';

COMMENT ON COLUMN grouper_sync_membership_v.U_ERROR_MESSAGE IS 'U_ERROR_MESSAGE: error message last time work was done on user unless a success happened afterward';

COMMENT ON COLUMN grouper_sync_membership_v.U_ERROR_TIMESTAMP IS 'U_ERROR_TIMESTAMP: timestamp the last error occurred unless a success happened afterwards';

COMMENT ON COLUMN grouper_sync_membership_v.G_ID IS 'G_ID: sync group id';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_ID IS 'G_GROUP_ID: grouper group id';

COMMENT ON COLUMN grouper_sync_membership_v.G_PROVISIONABLE IS 'G_PROVISIONABLE: T/F if group is provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.G_IN_TARGET IS 'G_IN_TARGET: T/F if the group is in target';

COMMENT ON COLUMN grouper_sync_membership_v.G_IN_TARGET_INSERT_OR_EXISTS IS 'G_IN_TARGET_INSERT_OR_EXISTS: T/F if the group was inserted by grouper or already existed in target';

COMMENT ON COLUMN grouper_sync_membership_v.G_IN_TARGET_START IS 'G_IN_TARGET_START: when the group was detected to be in the target';

COMMENT ON COLUMN grouper_sync_membership_v.G_IN_TARGET_END IS 'G_IN_TARGET_END: when the group was detected to not be in the target anymore';

COMMENT ON COLUMN grouper_sync_membership_v.G_PROVISIONABLE_START IS 'G_PROVISIONABLE_START: when this group started being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.G_PROVISIONABLE_END IS 'G_PROVISIONABLE_END: when this group stopped being provisionable';

COMMENT ON COLUMN grouper_sync_membership_v.G_LAST_UPDATED IS 'G_LAST_UPDATED: when the sync group was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.G_LAST_GROUP_SYNC_START IS 'G_LAST_GROUP_SYNC_START: when the group was sync started';

COMMENT ON COLUMN grouper_sync_membership_v.G_LAST_GROUP_SYNC IS 'G_LAST_GROUP_SYNC: when the group was last synced';

COMMENT ON COLUMN grouper_sync_membership_v.G_LAST_GROUP_META_SYNC_START IS 'G_LAST_GROUP_META_SYNC_START: when the metadata sync started';

COMMENT ON COLUMN grouper_sync_membership_v.G_LAST_GROUP_METADATA_SYNC IS 'G_LAST_GROUP_METADATA_SYNC: when the metadata was last synced';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_FROM_ID2 IS 'G_GROUP_FROM_ID2: link data from id2';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_FROM_ID3 IS 'G_GROUP_FROM_ID3: link data from id3';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_TO_ID2 IS 'G_GROUP_TO_ID2: link data to id2';

COMMENT ON COLUMN grouper_sync_membership_v.G_GROUP_TO_ID3 IS 'G_GROUP_TO_ID3: link data to id3';

COMMENT ON COLUMN grouper_sync_membership_v.G_METADATA_UPDATED IS 'G_METADATA_UPDATED: when metadata e.g. link data was last updated';

COMMENT ON COLUMN grouper_sync_membership_v.G_ERROR_MESSAGE IS 'G_ERROR_MESSAGE: if there is an error message last time work was done it is here';

COMMENT ON COLUMN grouper_sync_membership_v.G_ERROR_TIMESTAMP IS 'G_ERROR_TIMESTAMP: timestamp if last time work was done there was an error';

COMMENT ON COLUMN grouper_sync_membership_v.G_LAST_TIME_WORK_WAS_DONE IS 'G_LAST_TIME_WORK_WAS_DONE: timestamp of last time work was done on group';

update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V35, ' || history) from 1 for 3500), db_version = 35 where object_name = 'Grouper';
commit;

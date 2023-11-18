ALTER TABLE grouper_sync_log ADD description_clob clob;

ALTER TABLE grouper_sync_log ADD description_bytes integer;
	
COMMENT ON COLUMN grouper_sync_log.description_clob IS 'description for large data';

COMMENT ON COLUMN grouper_sync_log.description_bytes IS 'size of description in bytes';

UPDATE grouper_sync_log set description_bytes = length(description);
commit;

DROP INDEX grouper_sync_mship_gr_idx;
CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_id, grouper_sync_group_id, grouper_sync_member_id);

ALTER TABLE GROUPER_SYNC
    ADD last_full_sync_start DATE;

ALTER TABLE GROUPER_SYNC
    ADD last_full_metadata_sync_start DATE;

ALTER TABLE GROUPER_SYNC_JOB
    ADD last_sync_start DATE;

ALTER TABLE GROUPER_SYNC_LOG
    ADD sync_timestamp_start DATE;

ALTER TABLE GROUPER_SYNC_GROUP
    ADD last_group_sync_start DATE;

ALTER TABLE GROUPER_SYNC_GROUP
    ADD last_group_metadata_sync_start DATE;

ALTER TABLE GROUPER_SYNC_MEMBER
    ADD last_user_sync_start DATE;

ALTER TABLE GROUPER_SYNC_MEMBER
    ADD last_user_metadata_sync_start DATE;

COMMENT ON COLUMN grouper_sync.last_full_sync_start IS 'start time of last successful full sync';

COMMENT ON COLUMN grouper_sync.last_full_metadata_sync_start IS 'start time of last successful full metadata sync';

COMMENT ON COLUMN grouper_sync_job.last_sync_start IS 'start time of this job';

COMMENT ON COLUMN grouper_sync_log.sync_timestamp_start IS 'start of sync operation for log';

COMMENT ON COLUMN grouper_sync_group.last_group_sync_start IS 'start of last successful group sync';

COMMENT ON COLUMN grouper_sync_group.last_group_metadata_sync_start IS 'start of last successful group metadata sync';

COMMENT ON COLUMN grouper_sync_member.last_user_sync_start IS 'start of last successful user sync';

COMMENT ON COLUMN grouper_sync_member.last_user_metadata_sync_start IS 'start of last successful user metadata sync';

CREATE VIEW grouper_sync_membership_v (g_group_name, g_group_id_index, u_source_id, u_subject_id, u_subject_identifier, m_in_target, m_id, m_in_target_insert_or_exists, m_in_target_start, m_in_target_end, m_last_updated, m_membership_id, m_membership_id2, m_metadata_updated, m_error_message, m_error_timestamp, s_id, s_sync_engine, s_provisioner_name, u_id, u_member_id, u_in_target, u_in_target_insert_or_exists, u_in_target_start, u_in_target_end, u_provisionable, u_provisionable_start, u_provisionable_end, u_last_updated, u_last_user_sync_start, u_last_user_sync, u_last_user_meta_sync_start, u_last_user_metadata_sync, u_member_from_id2, u_member_from_id3, u_member_to_id2, u_member_to_id3, u_metadata_updated, u_last_time_work_was_done, u_error_message, u_error_timestamp, g_id, g_group_id, g_provisionable, g_in_target, g_in_target_insert_or_exists, g_in_target_start, g_in_target_end, g_provisionable_start, g_provisionable_end, g_last_updated, g_last_group_sync_start, g_last_group_sync, g_last_group_meta_sync_start, g_last_group_metadata_sync, g_group_from_id2, g_group_from_id3, g_group_to_id2, g_group_to_id3, g_metadata_updated, g_error_message, g_error_timestamp, g_last_time_work_was_done) AS select g.group_name as g_group_name, g.group_id_index as g_group_id_index, u.source_id as u_source_id, u.subject_id as u_subject_id, u.subject_identifier as u_subject_identifier, m.in_target as m_in_target, m.id as m_id, m.in_target_insert_or_exists as m_in_target_insert_or_exists, m.in_target_start as m_in_target_start, m.in_target_end as m_in_target_end, m.last_updated as m_last_updated, m.membership_id as m_membership_id, m.membership_id2 as m_membership_id2, m.metadata_updated as m_metadata_updated, m.error_message as m_error_message, m.error_timestamp as m_error_timestamp, s.id as s_id, s.sync_engine as s_sync_engine, s.provisioner_name as s_provisioner_name, u.id as u_id, u.member_id as u_member_id, u.in_target as u_in_target, u.in_target_insert_or_exists as u_in_target_insert_or_exists, u.in_target_start as u_in_target_start, u.in_target_end as u_in_target_end, u.provisionable as u_provisionable, u.provisionable_start as u_provisionable_start, u.provisionable_end as u_provisionable_end, u.last_updated as u_last_updated, u.last_user_sync_start as u_last_user_sync_start, u.last_user_sync as u_last_user_sync, u.last_user_metadata_sync_start as u_last_user_meta_sync_start, u.last_user_metadata_sync as u_last_user_metadata_sync, u.member_from_id2 as u_member_from_id2, u.member_from_id3 as u_member_from_id3, u.member_to_id2 as u_member_to_id2, u.member_to_id3 as u_member_to_id3, u.metadata_updated as u_metadata_updated, u.last_time_work_was_done as u_last_time_work_was_done, u.error_message as u_error_message, u.error_timestamp as u_error_timestamp, g.id as g_id, g.group_id as g_group_id, g.provisionable as g_provisionable, g.in_target as g_in_target, g.in_target_insert_or_exists as g_in_target_insert_or_exists, g.in_target_start as g_in_target_start, g.in_target_end as g_in_target_end, g.provisionable_start as g_provisionable_start, g.provisionable_end as g_provisionable_end, g.last_updated as g_last_updated, g.last_group_sync_start as g_last_group_sync_start, g.last_group_sync as g_last_group_sync, g.last_group_metadata_sync_start as g_last_group_meta_sync_start, g.last_group_metadata_sync as g_last_group_metadata_sync, g.group_from_id2 as g_group_from_id2, g.group_from_id3 as g_group_from_id3, g.group_to_id2 as g_group_to_id2, g.group_to_id3 as g_group_to_id3, g.metadata_updated as g_metadata_updated, g.error_message as g_error_message, g.error_timestamp as g_error_timestamp, g.last_time_work_was_done as g_last_time_work_was_done  from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id;

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

update grouper_ddl set last_updated = to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substr((to_char(systimestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V35, ' || history), 1, 3500), db_version = 35 where object_name = 'Grouper';
commit;

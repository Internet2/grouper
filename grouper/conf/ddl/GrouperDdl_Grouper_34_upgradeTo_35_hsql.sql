ALTER TABLE grouper_sync_log ADD COLUMN description_clob CLOB;

ALTER TABLE grouper_sync_log ADD COLUMN description_bytes BIGINT;
	
update grouper_sync_log set description_bytes = length(description);
commit;

DROP INDEX grouper_sync_mship_gr_idx;
CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_id, grouper_sync_group_id, grouper_sync_member_id);

ALTER TABLE GROUPER_SYNC
    ADD COLUMN last_full_metadata_sync_start TIMESTAMP;

ALTER TABLE GROUPER_SYNC
    ADD COLUMN last_full_sync_start TIMESTAMP;
    
ALTER TABLE GROUPER_SYNC_GROUP
    ADD COLUMN last_group_metadata_sync_start TIMESTAMP;

ALTER TABLE GROUPER_SYNC_GROUP
    ADD COLUMN last_group_sync_start TIMESTAMP;

ALTER TABLE GROUPER_SYNC_JOB
    ADD COLUMN last_sync_start TIMESTAMP;

ALTER TABLE GROUPER_SYNC_LOG
    ADD COLUMN sync_timestamp_start TIMESTAMP;

ALTER TABLE GROUPER_SYNC_MEMBER
    ADD COLUMN last_user_metadata_sync_start TIMESTAMP;

ALTER TABLE GROUPER_SYNC_MEMBER
    ADD COLUMN last_user_sync_start TIMESTAMP;

CREATE VIEW grouper_sync_membership_v (g_group_name, g_group_id_index, u_source_id, u_subject_id, u_subject_identifier, m_in_target, m_id, m_in_target_insert_or_exists, m_in_target_start, m_in_target_end, m_last_updated, m_membership_id, m_membership_id2, m_metadata_updated, m_error_message, m_error_timestamp, s_id, s_sync_engine, s_provisioner_name, u_id, u_member_id, u_in_target, u_in_target_insert_or_exists, u_in_target_start, u_in_target_end, u_provisionable, u_provisionable_start, u_provisionable_end, u_last_updated, u_last_user_sync_start, u_last_user_sync, u_last_user_meta_sync_start, u_last_user_metadata_sync, u_member_from_id2, u_member_from_id3, u_member_to_id2, u_member_to_id3, u_metadata_updated, u_last_time_work_was_done, u_error_message, u_error_timestamp, g_id, g_group_id, g_provisionable, g_in_target, g_in_target_insert_or_exists, g_in_target_start, g_in_target_end, g_provisionable_start, g_provisionable_end, g_last_updated, g_last_group_sync_start, g_last_group_sync, g_last_group_meta_sync_start, g_last_group_metadata_sync, g_group_from_id2, g_group_from_id3, g_group_to_id2, g_group_to_id3, g_metadata_updated, g_error_message, g_error_timestamp, g_last_time_work_was_done) AS select g.group_name as g_group_name, g.group_id_index as g_group_id_index, u.source_id as u_source_id, u.subject_id as u_subject_id, u.subject_identifier as u_subject_identifier, m.in_target as m_in_target, m.id as m_id, m.in_target_insert_or_exists as m_in_target_insert_or_exists, m.in_target_start as m_in_target_start, m.in_target_end as m_in_target_end, m.last_updated as m_last_updated, m.membership_id as m_membership_id, m.membership_id2 as m_membership_id2, m.metadata_updated as m_metadata_updated, m.error_message as m_error_message, m.error_timestamp as m_error_timestamp, s.id as s_id, s.sync_engine as s_sync_engine, s.provisioner_name as s_provisioner_name, u.id as u_id, u.member_id as u_member_id, u.in_target as u_in_target, u.in_target_insert_or_exists as u_in_target_insert_or_exists, u.in_target_start as u_in_target_start, u.in_target_end as u_in_target_end, u.provisionable as u_provisionable, u.provisionable_start as u_provisionable_start, u.provisionable_end as u_provisionable_end, u.last_updated as u_last_updated, u.last_user_sync_start as u_last_user_sync_start, u.last_user_sync as u_last_user_sync, u.last_user_metadata_sync_start as u_last_user_meta_sync_start, u.last_user_metadata_sync as u_last_user_metadata_sync, u.member_from_id2 as u_member_from_id2, u.member_from_id3 as u_member_from_id3, u.member_to_id2 as u_member_to_id2, u.member_to_id3 as u_member_to_id3, u.metadata_updated as u_metadata_updated, u.last_time_work_was_done as u_last_time_work_was_done, u.error_message as u_error_message, u.error_timestamp as u_error_timestamp, g.id as g_id, g.group_id as g_group_id, g.provisionable as g_provisionable, g.in_target as g_in_target, g.in_target_insert_or_exists as g_in_target_insert_or_exists, g.in_target_start as g_in_target_start, g.in_target_end as g_in_target_end, g.provisionable_start as g_provisionable_start, g.provisionable_end as g_provisionable_end, g.last_updated as g_last_updated, g.last_group_sync_start as g_last_group_sync_start, g.last_group_sync as g_last_group_sync, g.last_group_metadata_sync_start as g_last_group_meta_sync_start, g.last_group_metadata_sync as g_last_group_metadata_sync, g.group_from_id2 as g_group_from_id2, g.group_from_id3 as g_group_from_id3, g.group_to_id2 as g_group_to_id2, g.group_to_id3 as g_group_to_id3, g.metadata_updated as g_metadata_updated, g.error_message as g_error_message, g.error_timestamp as g_error_timestamp, g.last_time_work_was_done as g_last_time_work_was_done  from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id;

update grouper_ddl set last_updated = to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD'), history = substring((to_char(CURRENT_TIMESTAMP, 'YYYY/MM/DD HH24:mi:DD') || ': upgrade Grouper from V' || db_version || ' to V35, ' || history) from 1 for 3500), db_version = 35 where object_name = 'Grouper';
commit;

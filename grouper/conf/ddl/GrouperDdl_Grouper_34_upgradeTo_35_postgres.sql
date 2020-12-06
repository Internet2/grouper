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
    ADD COLUMN in_grouper VARCHAR(1);

ALTER TABLE grouper_sync_group
    ADD COLUMN in_grouper_insert_or_exists VARCHAR(1);

ALTER TABLE grouper_sync_group
    ADD COLUMN in_grouper_start TIMESTAMP;

ALTER TABLE grouper_sync_group
    ADD COLUMN in_grouper_end TIMESTAMP;

ALTER TABLE grouper_sync_group
    ADD COLUMN last_group_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_group
    ADD COLUMN last_group_metadata_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_job
    ADD COLUMN last_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_log
    ADD COLUMN sync_timestamp_start TIMESTAMP;

ALTER TABLE grouper_sync_member
    ADD COLUMN in_grouper VARCHAR(1);

ALTER TABLE grouper_sync_member
    ADD COLUMN in_grouper_insert_or_exists VARCHAR(1);

ALTER TABLE grouper_sync_member
    ADD COLUMN in_grouper_start TIMESTAMP;

ALTER TABLE grouper_sync_member
    ADD COLUMN in_grouper_end TIMESTAMP;
    
ALTER TABLE grouper_sync_member
    ADD COLUMN last_user_sync_start TIMESTAMP;

ALTER TABLE grouper_sync_member
    ADD COLUMN last_user_metadata_sync_start TIMESTAMP;    
    
ALTER TABLE grouper_sync_membership
    ADD COLUMN in_grouper VARCHAR(1);

ALTER TABLE grouper_sync_membership
    ADD COLUMN in_grouper_insert_or_exists VARCHAR(1);

ALTER TABLE grouper_sync_membership
    ADD COLUMN in_grouper_start TIMESTAMP;

ALTER TABLE grouper_sync_membership
    ADD COLUMN in_grouper_end TIMESTAMP;

COMMENT ON COLUMN grouper_sync.last_full_sync_start IS 'start time of last successful full sync';

COMMENT ON COLUMN grouper_sync.last_full_metadata_sync_start IS 'start time of last successful full metadata sync';

COMMENT ON COLUMN grouper_sync_job.last_sync_start IS 'start time of this job';

COMMENT ON COLUMN grouper_sync_log.sync_timestamp_start IS 'start of sync operation for log';

COMMENT ON COLUMN grouper_sync_group.in_grouper IS 'T if exists in grouper and F is not.  blank if not sure';

COMMENT ON COLUMN grouper_sync_group.in_grouper_insert_or_exists IS 'T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted';

COMMENT ON COLUMN grouper_sync_group.in_grouper_start IS 'when this was put in grouper';

COMMENT ON COLUMN grouper_sync_group.in_grouper_end IS 'when this was taken out of grouper';

COMMENT ON COLUMN grouper_sync_group.last_group_sync_start IS 'start of last successful group sync';

COMMENT ON COLUMN grouper_sync_group.last_group_metadata_sync_start IS 'start of last successful group metadata sync';

COMMENT ON COLUMN grouper_sync_member.in_grouper IS 'T if exists in grouper and F is not.  blank if not sure';

COMMENT ON COLUMN grouper_sync_member.in_grouper_insert_or_exists IS 'T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted';

COMMENT ON COLUMN grouper_sync_member.in_grouper_start IS 'when this was put in grouper';

COMMENT ON COLUMN grouper_sync_member.in_grouper_end IS 'when this was taken out of grouper';

COMMENT ON COLUMN grouper_sync_member.last_user_sync_start IS 'start of last successful user sync';

COMMENT ON COLUMN grouper_sync_member.last_user_metadata_sync_start IS 'start of last successful user metadata sync';

COMMENT ON COLUMN grouper_sync_membership.in_grouper IS 'T if exists in grouper and F is not.  blank if not sure';

COMMENT ON COLUMN grouper_sync_membership.in_grouper_insert_or_exists IS 'T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted';

COMMENT ON COLUMN grouper_sync_membership.in_grouper_start IS 'when this was put in grouper';

COMMENT ON COLUMN grouper_sync_membership.in_grouper_end IS 'when this was taken out of grouper';

CREATE OR REPLACE VIEW grouper_sync_membership_v (G_GROUP_NAME, G_GROUP_ID_INDEX, U_SOURCE_ID, U_SUBJECT_ID, U_SUBJECT_IDENTIFIER, M_IN_TARGET, M_ID, M_IN_TARGET_INSERT_OR_EXISTS, M_IN_TARGET_START, M_IN_TARGET_END, M_LAST_UPDATED, M_MEMBERSHIP_ID, M_MEMBERSHIP_ID2, M_METADATA_UPDATED, M_ERROR_MESSAGE, M_ERROR_TIMESTAMP, S_ID, S_SYNC_ENGINE, S_PROVISIONER_NAME, U_ID, U_MEMBER_ID, U_IN_TARGET, U_IN_TARGET_INSERT_OR_EXISTS, U_IN_TARGET_START, U_IN_TARGET_END, U_PROVISIONABLE, U_PROVISIONABLE_START, U_PROVISIONABLE_END, U_LAST_UPDATED, U_LAST_USER_SYNC_START, U_LAST_USER_SYNC, U_LAST_USER_META_SYNC_START, U_LAST_USER_METADATA_SYNC, U_MEMBER_FROM_ID2, U_MEMBER_FROM_ID3, U_MEMBER_TO_ID2, U_MEMBER_TO_ID3, U_METADATA_UPDATED, U_LAST_TIME_WORK_WAS_DONE, U_ERROR_MESSAGE, U_ERROR_TIMESTAMP, G_ID, G_GROUP_ID, G_PROVISIONABLE, G_IN_TARGET, G_IN_TARGET_INSERT_OR_EXISTS, G_IN_TARGET_START, G_IN_TARGET_END, G_PROVISIONABLE_START, G_PROVISIONABLE_END, G_LAST_UPDATED, G_LAST_GROUP_SYNC_START, G_LAST_GROUP_SYNC, G_LAST_GROUP_META_SYNC_START, G_LAST_GROUP_METADATA_SYNC, G_GROUP_FROM_ID2, G_GROUP_FROM_ID3, G_GROUP_TO_ID2, G_GROUP_TO_ID3, G_METADATA_UPDATED, G_ERROR_MESSAGE, G_ERROR_TIMESTAMP, G_LAST_TIME_WORK_WAS_DONE) AS select G.GROUP_NAME as G_GROUP_NAME, G.GROUP_ID_INDEX as G_GROUP_ID_INDEX, U.SOURCE_ID as U_SOURCE_ID, U.SUBJECT_ID as U_SUBJECT_ID, U.SUBJECT_IDENTIFIER as U_SUBJECT_IDENTIFIER, M.IN_TARGET as M_IN_TARGET, M.ID as M_ID, M.IN_TARGET_INSERT_OR_EXISTS as M_IN_TARGET_INSERT_OR_EXISTS, M.IN_TARGET_START as M_IN_TARGET_START, M.IN_TARGET_END as M_IN_TARGET_END, M.LAST_UPDATED as M_LAST_UPDATED, M.MEMBERSHIP_ID as M_MEMBERSHIP_ID, M.MEMBERSHIP_ID2 as M_MEMBERSHIP_ID2, M.METADATA_UPDATED as M_METADATA_UPDATED, M.ERROR_MESSAGE as M_ERROR_MESSAGE, M.ERROR_TIMESTAMP as M_ERROR_TIMESTAMP, S.ID as S_ID, S.SYNC_ENGINE as S_SYNC_ENGINE, S.PROVISIONER_NAME as S_PROVISIONER_NAME, U.ID as U_ID, U.MEMBER_ID as U_MEMBER_ID, U.IN_TARGET as U_IN_TARGET, U.IN_TARGET_INSERT_OR_EXISTS as U_IN_TARGET_INSERT_OR_EXISTS, U.IN_TARGET_START as U_IN_TARGET_START, U.IN_TARGET_END as U_IN_TARGET_END, U.PROVISIONABLE as U_PROVISIONABLE, U.PROVISIONABLE_START as U_PROVISIONABLE_START, U.PROVISIONABLE_END as U_PROVISIONABLE_END, U.LAST_UPDATED as U_LAST_UPDATED, U.LAST_USER_SYNC_START as U_LAST_USER_SYNC_START, U.LAST_USER_SYNC as U_LAST_USER_SYNC, U.LAST_USER_METADATA_SYNC_START as U_LAST_USER_META_SYNC_START, U.LAST_USER_METADATA_SYNC as U_LAST_USER_METADATA_SYNC, U.MEMBER_FROM_ID2 as U_MEMBER_FROM_ID2, U.MEMBER_FROM_ID3 as U_MEMBER_FROM_ID3, U.MEMBER_TO_ID2 as U_MEMBER_TO_ID2, U.MEMBER_TO_ID3 as U_MEMBER_TO_ID3, U.METADATA_UPDATED as U_METADATA_UPDATED, U.LAST_TIME_WORK_WAS_DONE as U_LAST_TIME_WORK_WAS_DONE, U.ERROR_MESSAGE as U_ERROR_MESSAGE, U.ERROR_TIMESTAMP as U_ERROR_TIMESTAMP, G.ID as G_ID, G.GROUP_ID as G_GROUP_ID, G.PROVISIONABLE as G_PROVISIONABLE, G.IN_TARGET as G_IN_TARGET, G.IN_TARGET_INSERT_OR_EXISTS as G_IN_TARGET_INSERT_OR_EXISTS, G.IN_TARGET_START as G_IN_TARGET_START, G.IN_TARGET_END as G_IN_TARGET_END, G.PROVISIONABLE_START as G_PROVISIONABLE_START, G.PROVISIONABLE_END as G_PROVISIONABLE_END, G.LAST_UPDATED as G_LAST_UPDATED, G.LAST_GROUP_SYNC_START as G_LAST_GROUP_SYNC_START, G.LAST_GROUP_SYNC as G_LAST_GROUP_SYNC, G.LAST_GROUP_METADATA_SYNC_START as G_LAST_GROUP_META_SYNC_START, G.LAST_GROUP_METADATA_SYNC as G_LAST_GROUP_METADATA_SYNC, G.GROUP_FROM_ID2 as G_GROUP_FROM_ID2, G.GROUP_FROM_ID3 as G_GROUP_FROM_ID3, G.GROUP_TO_ID2 as G_GROUP_TO_ID2, G.GROUP_TO_ID3 as G_GROUP_TO_ID3, G.METADATA_UPDATED as G_METADATA_UPDATED, G.ERROR_MESSAGE as G_ERROR_MESSAGE, G.ERROR_TIMESTAMP as G_ERROR_TIMESTAMP, G.LAST_TIME_WORK_WAS_DONE as G_LAST_TIME_WORK_WAS_DONE  from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id;

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

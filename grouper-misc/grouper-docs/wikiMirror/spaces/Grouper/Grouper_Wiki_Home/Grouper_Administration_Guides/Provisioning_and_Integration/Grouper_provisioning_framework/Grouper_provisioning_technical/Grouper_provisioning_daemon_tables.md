---
title: "Grouper provisioning / daemon tables"
space: Grouper
pageId: 28554422
version: 14
lastUpdated: 2026-07-01T05:40:29.055Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554422/Grouper+provisioning+daemon+tables
---

## Create tables in 2.4

```
Run in GSH (pass true if not prod and you want to run the script):

edu.internet2.middleware.grouper.app.tableSync.TableSyncCreateTables.main(new String[]{"false"});

Run the script that is outputted.
```

## grouper_loader_log

Grouper has a daemon log table: grouper_loader_log. This table will remain, holding logs for recent runs of daemon jobs

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR2 (128 Char) | uuid |
| JOB_NAME | VARCHAR2 (512 Char) | quartz job name |
| STATUS | VARCHAR2 (20 Char) | SUCCESS \| ERROR \| WARNING \| etc |
| STARTED_TIME | DATE | when job started |
| ENDED_TIME | DATE | when job ended |
| MILLIS | INTEGER | how long job took in millis |
| MILLIS_GET_DATA | INTEGER | if there is a get data task, how long that took in millis |
| MILLIS_LOAD_DATA | INTEGER | if there is a load data task, how long that took in millis |
| JOB_TYPE | VARCHAR2 (128 Char) | e.g. SQL_SIMPLE |
| JOB_SCHEDULE_TYPE | VARCHAR2 (128 Char) | generally this is CRON |
| JOB_DESCRIPTION | VARCHAR2 (4000 Char) | describe the job (not the task) |
| JOB_MESSAGE | VARCHAR2 (4000 Char) | text about the output of this task |
| HOST | VARCHAR2 (128 Char) | that ran the job |
| GROUP_UUID | VARCHAR2 (128 Char) | if this is related to a specific group, this is the uuid |
| JOB_SCHEDULE_QUARTZ_CRON | VARCHAR2 (128 Char) | schedule |
| JOB_SCHEDULE_INTERVAL_SECONDS | INTEGER | if interval, this is the interval |
| LAST_UPDATED | DATE | when this row was last updated |
| UNRESOLVABLE_SUBJECT_COUNT | INTEGER | how many unresolvables in the job |
| INSERT_COUNT | INTEGER | how many inserts |
| UPDATE_COUNT | INTEGER | how many updates |
| DELETE_COUNT | INTEGER | how many deletes |
| TOTAL_COUNT | INTEGER | total records in job |
| PARENT_JOB_NAME | VARCHAR2 (512 Char) | if this is a subjob, this is the parent job name |
| PARENT_JOB_ID | VARCHAR2 (128 Char) | uuid of the parent job log line |
| AND_GROUP_NAMES | VARCHAR2 (512 Char) | if AND'ing with other groups, these are the group names |
| JOB_SCHEDULE_PRIORITY | INTEGER | quartz priority |
| CONTEXT_ID | VARCHAR2 (40 Char) | links to audit line |

## grouper_sync

Overall table for a grouper scheduled daemon (generally for syncing things, but could be for daemon)

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR | uuid of row |
| SYNC_ENGINE | VARCHAR | type of job. e.g. sql_sync, pspng, etc |
| PROVISIONER_NAME | VARCHAR | name of job in the sync_engine. this must be unique |
| GROUP_COUNT | INTEGER | if there are groups affected, this is the count of groups in job |
| USER_COUNT | INTEGER | if there are users affected, this is the count of users in job |
| RECORDS_COUNT | INTEGER | total number of records affected |
| INCREMENTAL_INDEX | INTEGER | if incremental syncing based on some sequence, this is last one processed |
| INCREMENTAL_TIMESTAMP | TIMESTAMP | if incremental syncing based on timestamp, this is last timestamp processed |
| LAST_INCREMENTAL_SYNC_RUN | TIMESTAMP | last time the incremental sync ran (when started) |
| LAST_FULL_SYNC_RUN | TIMESTAMP | last time a full sync was kicked off |
| LAST_FULL_METADATA_SYNC_RUN | TIMESTAMP | when last full metadata sync ran. this needs to run when groups get renamed |
| LAST_UPDATED | TIMESTAMP | when this record is last updated |

## grouper_sync_job

Each daemon has an overall record, and one for the job that runs. This differentiates the full sync or the incremental run, or a one off that syncs an individual group or user or membership. This also assures that multiple jobs do not run at once. This record should be updated in a thread so that progress can be tracked and a "heartbeat" is established so we know the job is running. Every daemon has a record in grouper_sync, and grouper_sync_job.

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR | uuid |
| GROUPER_SYNC_ID | VARCHAR | foreign key back to grouper_sync |
| SYNC_TYPE | VARCHAR | up to the sync_engine what is available. e.g. full_sync, incremental, group_sync, user_sync, daemon, etc |
| JOB_STATE | VARCHAR | running, pending (if waiting for another job to finish), notRunning |
| LAST_SYNC_INDEX | INTEGER | for incremental jobs, this is the last index processed |
| LAST_SYNC_TIMESTAMP | TIMESTAMP | for incremental jobs, this is the last timestamp processed |
| LAST_TIME_WORK_WAS_DONE | TIMESTAMP | if work was actually accomplished, this is the last time. for instance if 2 hours of incrementals have passed and no work needed to be done, then do not update this field |
| HEARTBEAT | TIMESTAMP | Heartbeat column that is updated as a job is running in a thread (e.g. every 60 seconds) |
| QUARTZ_JOB_NAME | VARCHAR | Quartz job name |
| LAST_UPDATED | TIMESTAMP | when this record was last updated |

## grouper_sync_group

If this daemon affects groups and wants to use this table, there would be a record here for each group affected for each daemon (not each job). For instance, for pspng, there is full and incremental, but only one record per group in this table. If a group is deleted, this record might still exist for some more time (a week?) . If the group is deprovisioned, this record could remain or be removed. Note this could be tracking a group going to a target, or coming from a target. There are no plans to integrate this with the loader.

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR | uuid of record |
| GROUPER_SYNC_ID | VARCHAR | foreign key to daemon |
| GROUP_ID | VARCHAR | uuid of group (not foreign key) |
| GROUP_NAME | VARCHAR | name of group |
| PROVISIONABLE | BOOLEAN | if provisionable (based on provisioning attributes). Job can use this data to know if   this group is provisionable, or it could use attributes. Changes in attributes will change   this boolean in the change log or full sync |
| IN_TARGET | BOOLEAN | if this group is currently in the target |
| IN_TARGET_INSERT_OR_EXISTS | VARCHAR | if the IN_TARGET_START was an insert into the target (was provisioned then), then T, or if it already existed at that time (inserted at some time before that time) |
| IN_TARGET_START | TIMESTAMP | earliest date we know the group was in the target |
| IN_TARGET_END | TIMESTAMP | when this was removed from the target |
| PROVSIONABLE_START | TIMESTAMP | when this group started to be provisionable to this target |
| PROVISIONABLE_END | TIMESTAMP | when this group ended being provisionable from this target |
| GROUP_FROM_ID2 | VARCHAR | metadata on group |
| GROUP_FROM_ID3 | VARCHAR | metadata on group |
| GROUP_TO_ID2 | VARCHAR | metadata on group |
| GROUP_TO_ID3 | VARCHAR | metadata on group |
| LAST_TIME_WORK_WAS_DONE | TIMESTAMP | last time changes were made on this group for this daemon |
| LAST_GROUP_SYNC | TIMESTAMP | last time group sync was started (memberships and metadata) |
| LAST_GROUP_METADATA_SYNC | TIMESTAMP | last time metadata (not memberships) were synced |
| LAST_UPDATED | TIMESTAMP | when this record was last updated |

## grouper_sync_member

If this daemon affects subjects and wants to use this table, there would be a record here for each entity affected for each daemon (not each job). For instance, for pspng, there is full and incremental, but only one record for each subject in this table. If a member is deleted, this record might still exist for some more time (a week?) . If the member is deprovisioned, this record could remain or be removed. Note this could be tracking a member going to a target, or coming from a target. There are no plans to integrate this with the loader.

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR | uuid of record |
| GROUPER_SYNC_ID | VARCHAR | foreign key to daemon |
| MEMBER_ID | VARCHAR | uuid of member (not foreign key) |
| SUBJECT_ID | VARCHAR | subject id of the entity |
| SOURCE_ID | VARCHAR | source id of the entity |
| PROVISIONABLE | BOOLEAN | if provisionable (based on provisioning attributes). Job can use this data to know if   this member is provisionable, or it could use group membership. Changes in membership will change   this boolean in the change log or full sync |
| IN_TARGET | BOOLEAN | if this entity is currently in the target |
| IN_TARGET_INSERT_OR_EXISTS | VARCHAR | if the IN_TARGET_START was an insert into the target (was provisioned then), or if it already existed   at that time (inserted at some time before that time) |
| IN_TARGET_START | TIMESTAMP | earliest date we know the entity was in the target |
| IN_TARGET_END | TIMESTAMP | when this was removed from the target |
| PROVSIONABLE_START | TIMESTAMP | when this entity started to be provisionable to this target |
| PROVISIONABLE_END | TIMESTAMP | when this entity ended being provisionable from this target |
| MEMBER_FROM_ID2 | VARCHAR | metadata on member |
| MEMBER_FROM_ID3 | VARCHAR | metadata on member |
| MEMBER_TO_ID2 | VARCHAR | metadata on member |
| MEMBER_TO_ID3 | VARCHAR | metadata on member |
| LAST_TIME_WORK_WAS_DONE | TIMESTAMP | last time changes were made on this member for this daemon |
| LAST_USER_SYNC | TIMESTAMP | last time user sync was started (memberships and metadata) |
| LAST_USER_METADATA_SYNC | TIMESTAMP | last time metadata (not memberships) were synced |
| LAST_UPDATED | TIMESTAMP | when this record was last updated |

## grouper_sync_membership

If this daemon affects memberships and wants to use this table, there would be a record here for each effective membership affected for each daemon (not each job). For instance, for pspng, there is full and incremental, but only one record for each membership in this table. If a membership is deleted, this record might still exist for some more time (a week?) . If the member is deprovisioned, this record could remain or be removed. Note this could be tracking a membership going to a target, or coming from a target. There are no plans to integrate this with the loader. Generally for large jobs, memberships are not tracked. Perhaps it is configured to be on for troubleshooting only.

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR | uuid of record |
| GROUPER_SYNC_ID | VARCHAR | foreign key to daemon |
| GROUPER_SYNC_GROUP_ID | VARCHAR | foreign key to grouper_sync_group table |
| GROUPER_SYNC_MEMBER_ID | VARCHAR | foreign key to grouper_sync_member table |
| IN_TARGET | BOOLEAN | if this membership is currently in the target |
| IN_TARGET_INSERT_OR_EXISTS | VARCHAR | if the IN_TARGET_START was an insert into the target (was provisioned then), or if it already existed   at that time (inserted at some time before that time) |
| IN_TARGET_START | TIMESTAMP | earliest date we know the membership was in the target |
| IN_TARGET_END | TIMESTAMP | when this was removed from the target |
| MEMBERSHIP_ID | VARCHAR | metadata on member |
| MEMBERSHIP_ID2 | VARCHAR | metadata on member |
| LAST_UPDATED | TIMESTAMP | when this record was last updated |

## grouper_sync_log

This table has the last log that work was done for the job (not daemon), or for each group, member, or membership. These logs can be used on the UI so users can know the last work done. If no work is done (inserts/updates), then there is no log entry change.

| Column | type | description |
| --- | --- | --- |
| ID | VARCHAR | uuid of record |
| GROUPER_SYNC_JOB_ID | VARCHAR | foreign key to daemon job (full or incremental) |
| GROUPER_SYNC_GROUP_ID | VARCHAR | foreign key to grouper_sync_group table if this log is on a group |
| GROUPER_SYNC_MEMBER_ID | VARCHAR | foreign key to grouper_sync_member table if this log is on a subject |
| GROUPER_SYNC_MEMBERSHIP_ID | VARCHAR | foreign key to grouper_sync_membership table if this log is on a membership |
| GROUPER_SYNC_OWNER_ID | VARCHAR | depending on the type of log, this is the ID of the "owner". e.g. the    GROUPER_SYNC_JOB_ID (if log on the job overall)   GROUPER_SYNC_GROUP_ID (if log is on the group for the job)   GROUPER_SYNC_MEMBER_ID (if the log is on the entity for the job)   GROUPER_SYNC_MEMBERSHIP_ID (if the log is on the membership for the job) |
| STATUS | VARCHAR | SUCCESS, ERROR, WARNING |
| DESCRIPTION | VARCHAR | text description of what happened for this entry |
| RECORDS_PROCESSED | INTEGER | how many records processed (looked at, maybe some skipped) |
| RECORDS_CHANGED | INTEGER | how many records actually changed for this log |
| JOB_TOOK_MILLIS | INTEGER | how long this took |
| SERVER | VARCHAR | which server this ran on |
| LAST_UPDATED | TIMESTAMP | when this record was last updated |

## Sample queries

Get all memberships of provisioned groups based on sync metadata (new method)

```
select distinct gg.id as group_id, gg.name, gm.subject_id, gm.id as member_id
  FROM grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_fields gfl, grouper_members gm,
       grouper_sync gsync, grouper_sync_group gsg
  WHERE ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id and ms.enabled = 'T' and ms.member_id = gm.id
       and gs.OWNER_GROUP_ID = gg.id AND gs.FIELD_ID = gfl.ID and GM.SUBJECT_SOURCE = 'jdbc'
       and gg.name = 'test:testGroup' and gfl.name = 'members'
       and gsync.sync_engine = 'theSyncEngine' and gsync.provisioner_name = 'someLdap'
       and gsync.id = gsg.grouper_sync_id and gsg.group_id = gg.id 
       and (gsg.provisionable = 'T' or gsg.in_target = 'T')
```

Get all memberships of provisioned groups based on provisioning attributes (legacy method)

```
   SELECT distinct gg.id as group_id, gg.name,
          gm.SUBJECT_ID, GM.ID as member_id
     FROM grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_fields gfl, grouper_members gm,
          grouper_attribute_assign gaa_marker, grouper_attribute_def_name gadn_marker,
          grouper_attribute_assign gaa_do_provision, grouper_attribute_def_name gadn_do_provision, grouper_attribute_assign_value gaav_do_provision,
          grouper_attribute_assign gaa_target, grouper_attribute_def_name gadn_target, grouper_attribute_assign_value gaav_target          
    WHERE ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id and ms.enabled = 'T' and ms.member_id = gm.id 
          and gs.OWNER_GROUP_ID = gg.id AND gs.FIELD_ID = gfl.ID and GM.SUBJECT_SOURCE = 'jdbc'
          and gfl.name = 'members'
          and gg.id = gaa_marker.owner_group_id and gaa_marker.id = gaa_do_provision.owner_attribute_assign_id and gaa_marker.id = gaa_target.owner_attribute_assign_id
          AND gaa_marker.attribute_def_name_id = gadn_marker.id and gadn_marker.name = 'etc:provisioning:provisioningMarker'
             AND gaa_marker.enabled = 'T'
          AND gaa_do_provision.attribute_def_name_id = gadn_do_provision.id and gadn_do_provision.name = 'etc:provisioning:provisioningDoProvision' 
              AND gaa_do_provision.enabled = 'T' and gaav_do_provision.attribute_assign_id = gaa_do_provision.id and gaav_do_provision.value_string = 'true'
          AND gaa_target.attribute_def_name_id = gadn_target.id and gadn_target.name = 'etc:provisioning:provisioningTarget'
              AND gaa_target.enabled = 'T' and gaav_target.attribute_assign_id = gaa_target.id and GAAV_TARGET.VALUE_STRING = 'personSourceTest'
```

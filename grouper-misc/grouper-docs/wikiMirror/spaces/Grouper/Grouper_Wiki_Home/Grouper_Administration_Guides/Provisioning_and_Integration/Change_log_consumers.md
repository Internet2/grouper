---
title: "Change log consumers"
space: Grouper
pageId: 28545225
version: 46
lastUpdated: 2026-07-01T05:47:36.096Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545225/Change+log+consumers
---

> [This topic is discussed in the Advanced Topics training video](http://www.youtube.com/watch?v=NPpzX2w1YL4).

 

 

## Overview

 The Grouper change log records every change to the registry so that other processes can react to it. It consists of three tables:

 

- The change log temp table (`grouper_change_log_entry_temp`) is where every Grouper process writes events as they happen.
- A daemon moves events from the temp table to the change log table (`grouper_change_log_entry`), giving each a sequential numeric id and calculating point-in-time data.
- Change log consumers read from the change log table and keep a pointer to their progress in the change log consumer table (`grouper_change_log_consumer`).

 The set of event types is defined in the `grouper_change_log_type` table (see "Change log events" below). A friendlier `grouper_change_log_entry_v` SQL view is also available for querying (see "SQL view" below).

 

## Integrating with the change log

 Grouper can integrate with, or notify, external systems in near real time based on the change log. Common approaches:

 

- **Messaging / ESB integration** — Grouper's ESB event listener (the `EsbConsumer` change log consumer) publishes change log events to a message queue or external endpoint. This is the recommended way to send notifications, and it does not require custom code.
- **The Grouper provisioning framework** — provisions memberships and related data to targets such as LDAP, Active Directory, and SQL, driven incrementally by the change log.
- **A custom change log consumer in Java** — gives you direct access to all the information for each change for maximum flexibility, but requires custom code. This is most useful when provisioning a target that the built-in options cannot handle.

## Change log events

 The change log events below are current as of all currently supported Grouper releases. The authoritative list for any release is the `ChangeLogTypeBuiltin` enum (or the `grouper_change_log_type` table) for that version, so check the source for your version to confirm.

 Note that permission notifications are not on flattened permissions, for performance reasons. Instead, whenever anything related to a permission changes (including memberships and all the hierarchies that could form a permission), change log events are added for all the roles involved, using the action name `permissionChangeOnRole` (and `permissionChangeOnSubject`).

 This diagram shows the change log and notifications in the Grouper architecture.

 

| Change log category | Action name |
| --- | --- |
| attributeAssign | addAttributeAssign |
| attributeAssign | deleteAttributeAssign |
| attributeAssignAction | addAttributeAssignAction |
| attributeAssignAction | deleteAttributeAssignAction |
| attributeAssignAction | updateAttributeAssignAction |
| attributeAssignActionSet | addAttributeAssignActionSet |
| attributeAssignActionSet | deleteAttributeAssignActionSet |
| attributeAssignValue | addAttributeAssignValue |
| attributeAssignValue | deleteAttributeAssignValue |
| attributeDef | addAttributeDef |
| attributeDef | deleteAttributeDef |
| attributeDef | updateAttributeDef |
| attributeDefName | addAttributeDefName |
| attributeDefName | deleteAttributeDefName |
| attributeDefName | updateAttributeDefName |
| attributeDefNameSet | addAttributeDefNameSet |
| attributeDefNameSet | deleteAttributeDefNameSet |
| entity | addEntity |
| entity | deleteEntity |
| entity | disableEntity |
| entity | enableEntity |
| entity | updateEntity |
| group | addGroup |
| group | deleteGroup |
| group | disableGroup |
| group | enableGroup |
| group | updateGroup |
| groupComposite | addGroupComposite |
| groupComposite | deleteGroupComposite |
| groupComposite | updateGroupComposite |
| groupField | addGroupField |
| groupField | deleteGroupField |
| groupField | updateGroupField |
| groupSet | addGroupSet |
| groupSet | deleteGroupSet |
| groupTypeAssignment | assignGroupType |
| groupTypeAssignment | unassignGroupType |
| member | addMember |
| member | changeSubject |
| member | deleteMember |
| member | updateMember |
| membership | addMembership |
| membership | deleteMembership |
| membership | updateMembership |
| permission | permissionChangeOnRole |
| permission | permissionChangeOnSubject |
| privilege | addPrivilege |
| privilege | deletePrivilege |
| privilege | updatePrivilege |
| roleSet | addRoleSet |
| roleSet | deleteRoleSet |
| stem | addStem |
| stem | deleteStem |
| stem | updateStem |
| dataFieldAssign | addDataFieldAssign (v6+) |
| dataFieldAssign | deleteDataFieldAssign (v6+) |
| dataRowAssign | addDataRowAssign (v6+) |
| dataRowAssign | deleteDataRowAssign (v6+) |
| dataRowFieldAssign | addDataRowFieldAssign (v6+) |
| dataRowFieldAssign | deleteDataRowFieldAssign (v6+) |

 

 

## Implementing a consumer

 For most integrations you should configure the ESB event listener (`EsbConsumer`) rather than writing a consumer, since it already publishes events to common targets. If you do need custom logic, write a Java class that extends `ChangeLogConsumerBase`.

 Consumers run as Grouper Loader jobs and are configured in `grouper-loader.properties`:

 
```
# name the consumer after the "changeLog.consumer." prefix (here, "myConsumerName")
changeLog.consumer.myConsumerName.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.myConsumerName.quartzCron =
# optional per-consumer batch size; defaults to changeLog.changeLogConsumerBatchSize (1000)
changeLog.consumer.myConsumerName.changeLogConsumerBatchSize =
```

 Grouper tracks each consumer's progress (its last processed sequence number) in `grouper_change_log_consumer` and runs the consumers in the loader. See [Change log consumer in GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547983/Grouper+GSH+change+log+consumer) for a worked example.

 

## SQL view

 There is a friendly SQL view, `grouper_change_log_entry_v`, which is easier to query when debugging than the underlying `grouper_change_log_entry` table.

 

## Troubleshooting

 > The SQL below reads and updates Grouper tables directly. It requires database access and is a Grouper administrator (sysadmin) operation; advancing a consumer skips events, so use it carefully.

 Occasionally a change log consumer may get stuck processing an event. This can happen, for example, when a group has been deleted and a Grouper rule cannot find the group to process. In the UI, these show up as errors on the daemon page:

 

 Here you can see that every minute, the `CHANGE_LOG_consumer_grouperRules` consumer has had an error.

 To advance the consumer, view the error message, which indicates which change log entry it was stuck on (scroll all the way to the right in the Grouper UI):

 
```
Error: Error processing record 256133454, sequenceNumber: 256133454, java.lang.RuntimeException: ...
```

 You can see which change log entry your consumer is stuck at in the `grouper_change_log_consumer` table (substitute the name of your consumer, or select all rows):

 
```sql
select LAST_SEQUENCE_PROCESSED from GROUPER_CHANGE_LOG_CONSUMER WHERE NAME = 'grouperRules';
```

 Look in the change log for entries at or after the sequence number the consumer got stuck at:

 
```sql
SELECT * FROM GROUPER_CHANGE_LOG_ENTRY WHERE SEQUENCE_NUMBER >= 256133454;
```

 Once you have found the group of entries you want to skip over, stop the consumer and wait for it to stop, then update the consumer table to advance the specific consumer past them:

 
```sql
UPDATE GROUPER_CHANGE_LOG_CONSUMER SET LAST_SEQUENCE_PROCESSED = 256133489 WHERE NAME = 'grouperRules';
```

 Then return to the Grouper miscellaneous status page to monitor the consumer for progress.

 

## Design and FAQ

 

- The change log is transactional. If a rollback or failure occurs in Grouper, the change log stays in sync.
- If the destination is unreachable, the consumer retries the same record next time.
- You can query the change log directly with the API through the DAO.
- Change log entries are ordered so that they notify in a workable order (for example, a group is created before members are added to it).
- Each row in the change log has a unique, sequential id.
- The timestamp is in microseconds and will never be the same as another record on the same JVM.
- No database triggers are used; it is an all-Java solution.
- There are 12 columns (`string01`..`string12`) to stash data in each change log entry.
- Grouper keeps track of the progress of consumers run in the loader.

 **See also**

 [Change log consumer in GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547983/Grouper+GSH+change+log+consumer)

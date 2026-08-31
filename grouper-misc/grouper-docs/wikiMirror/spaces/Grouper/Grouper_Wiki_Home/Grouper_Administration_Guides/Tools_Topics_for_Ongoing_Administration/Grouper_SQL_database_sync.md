---
title: "Grouper SQL database sync"
space: Grouper
pageId: 28545506
version: 36
lastUpdated: 2026-07-12T15:26:43.497Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545506/Grouper+SQL+database+sync
---

| [Wiki Home](https://grouper.atlassian.net/wiki/spaces/Grouper/overview) | Download Grouper | Grouper Guides | [Community Contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions) | Developer Resources | [Grouper Website](http://internet2.edu/products-services/trust-identity-middleware/grouper/) |
| --- | --- | --- | --- | --- | --- |

In grouper 2.4 patch #47, Grouper can sync database tables. This is intended for these use cases:

1. Sync memberships to their own table for performance reasons (when you are doing reporting and have a lot of joins and subselects, a table without lots of joins can help a lot)
2. Sync memberships to another database. Other applications might not want to depend on Grouper at runtime, or have other performance reasons
3. Sync a subject table from another database to Grouper. This might be for performance or availability reasons
4. Sync some table to another table. This function is generic so use it for whatever you want (groups, attributes, etc)

The tables in the "from" and "to" need to exist and the right columns and column types need to be there. In addition you cannot name columns which are keywords.

Note, you can sync across database vendors as well (e.g. bring subjects from Oracle to MySQL).

**Some things to know**:

1. when configuring grouper.client.properties as described below, references to databases are those configured in grouper-loader.properties.
2. The "*grouper"* database is the same db as defined in grouper.hibernate.properties. No need to define the grouper db in grouper-loader.properties.
3. When specifying tableFrom or tableTo, depending on your username/schema being used and assumptions your DB driver makes, you may or may not need to specify *schema.tablename*.

## Overall flow of syncs

1. See if needs to run or exit
  
  
  
  | Sync type | Check when | How check |
  | --- | --- | --- |
  | full | at startup | If a full has run in the last X (configurable, e.g. 12 hours),   then dont run |
  | full | at startup | 1. register that this job wants to run 2. see if another job is running 3. wait until it isnt running if so 4. register as running if nothing else running 5. wait a couple more seconds 6. if nothing else running then run |
  | incremental | at startup | if another job is running or pending then wait |
  | incremental | throughout job | if another job is running or pending then exit |
2. Select all of something. Note, if there is a source and destination query, do one in a thread, and the other in the current thread. Handle exceptions appropriately. Wait for both to finish before proceeding.
  
  
  
  | Sync type | Sync subtype | Select what | From where | Example | More info |
  | --- | --- | --- | --- | --- | --- |
  | full | fullSyncFull | select all records and all columns | 1. source 2. destination | select * from table | get all records from both sides |
  | full | fullSyncGroupings | select all distinct groupings | 1. source 2. destination | select distinct grouping_col from table | either:     1. select all one col primary keys 2. select one col that groups sets of records together (e.g. group_name of memberships) |
  | full | fullSyncChangeFlag | select all primary keys and a column   that is a change flag | 1. source 2. destination | select uuid, last_updated from table | This can be a last updated date (to the milli) or a checksum string or something |
  | full | fullSyncMetadata | select all distinct groupings | 1. source 2. destination | select distinct grouping_col from table | 1. if grouping not in dest then add it 2. if grouping is in dest and not source then delete it  Useful if groups get renamed or tags get added/removed  Does not sync all memberships and does not count as a full sync |
  | incremental | incrementalAllColumns | get all incrementals that have happened since the last check  including all columns | 1. source or change log table | select * from table where last_updated > last_checked | If the source table as a last_updated or numeric increasing col. Note, this will not process deletes if off of source table since deleted rows wont be there |
  | incremental | incrementalPrimaryKey | get all incrementals and each row has the primary  key to sync | 1. change log table | select primary_key_col0, primary_key_col1 from change_log_table where last_updated > last_checked | If the change_log_table doesnt have all columns, but might also have deletes |
3. Initial compare
  
  
  
  | Sync type | Sync subtype | Initial compare |
  | --- | --- | --- |
  | full | fullSyncFull | If primary key exists in destination and not source, then delete in primary key in the destination  If all columns row in source matches all columns of row in destination then remove from both lists  Compare all records and batch up the inserts/updates/deletes, done |
  | full | fullSyncGroupings | If a grouping exists in destination and not source, then delete that grouping in destination |
  | full | fullSyncChangeFlag | If primary key exists in destination and not source, then delete in primary key in the destination  If change flag of row in source matches change flag of row in destination then remove from both lists |
  | incremental | * | NA |
4. Switch job type?  
    
  Note: if an incremental switches to a grouping or full sync, then it wont yield to a real full sync...
  
  
  
  
  
  | Sync type | Sync subtype | If this occurs | Switch to |
  | --- | --- | --- | --- |
  | incremental | * | Number of records is greater than X (configurable, e.g. 10k),  and if grouping, there are fewer than Y groupings (configurable, e.g. 5) | 1. Capture current timestamp or max record in change_log 2. Full sync everything 3. Updated last processed to "a", skip records until "a" |
  | incremental | * | If grouping, and if theres a fullSyncGroupings job,  and if a grouping has more than X (configurable, e.g. 5k) | 1. Capture current timestamp or max record in change_log 2. Do a grouping sync on those groupings (e.g. all records for a group) 3. Skip records in that grouping until "a" |
5. Batch up requests (e.g. process a certain number of records at once. Note; this can be done in several threads
  
  
  
  
  
  | Sync type | Sync subtype | Select what | From where | Number of records | Example |
  | --- | --- | --- | --- | --- | --- |
  | full | fullSyncFull | NA, This job is already done |  |  |  |
  | full | fullSyncGroupings | select all columns from source and destination that are between two grouping indexes | 1. source 2. destination | Approx 10k or 100k, might be unknown if grouping col is not primary key. Based on grouping size configuration  e.g. for groups, might be 5k groups at once, for people, might be 50k people at once | select * from table where grouping_col > ? and grouping col < ? |
  | full | fullSyncChangeFlag | select all primary keys and a column   that is a change flag | 1. source 2. destination | 50-900  constrained by bind var max of 1000 | select * from table where primary_key_col0 = ? and primary_key_col1 = ? |
  | incremental | incrementalFullAllColumns | get all incrementals that have happened since the last check  including all columns | 1. destination | 50-900  constrained by bind var max of 1000 | select * from table where primary_key_col0 = ? and primary_key_col1 = ? |
  | incremental | incrementalFullPrimaryKey | get all incrementals and each row has the primary  key to sync | 1. source 2. destination | 50-900  constrained by bind var max of 1000 | select * from table where primary_key_col0 = ? and primary_key_col1 = ? |
6. Process records  
    
  Note these use prepared statement batching, so all the deletes happen in batches of 100 (configurable), updates happen in batches of 100 (configurable), and inserts happen in batches of 100 (configurable)  
    
  
  
  1. If record exists in source and not in destination by primary key, then insert the destination record
  2. If record exists in destination and not source by primary key, then delete the destination record
  3. If a primary key exists in source and destination, but all cols do not match, then update the destination record
  4. If a primary key exists in source and destination, and all cols match, then ignore the record
7. Move pointer forward to max number/date processed

## Performance

The performance will be limited by how many changes there are and how quickly it can get the data on both sides.

From a local mysql to a local mysql, if there are 25000 rows and 25000 inserts, it takes 2 minutes. If there are 25000 rows and no updates, it takes 0.3 seconds.

This will select data in batches, insert/update/delete in batches.

## Configuration

All settings defaults (grouper.client.properties)

```
################################
## Sync database table settings
################################

# the grouping column is what is uniquely selected, and then batched through to get data, default across all sql jobs
# {valueType: "integer"}
# grouperClient.syncTableDefault.groupingSize = 10000

# size of jdbc batches, default across all sql jobs
# {valueType: "integer"}
# grouperClient.syncTableDefault.batchSize = 800

# number of bind vars in select, default across all sql jobs
# {valueType: "integer"}
# grouperClient.syncTableDefault.maxBindVarsInSelect = 900

# default database for all sql jobs for status tabls (e.g. grouper_sync*)
# {valueType: "string"}
# grouperClient.syncTableDefault.statusDatabase = grouper

# default switch from incremental to full if the number of incrementals is over this threshold
# {valueType: "integer"}
# grouperClient.syncTableDefault.switchFromIncrementalToFullIfOverRecords = 300000

# switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
# fullSyncChangeFlag, fullSyncFull, fullSyncGroups
# {valueType: "string"}
# grouperClient.syncTableDefault.switchFromIncrementalToFullSubtype = fullSyncFull

# switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
# {valueType: "integer"}
# grouperClient.syncTableDefault.switchFromIncrementalToGroupIfOverRecordsInGroup = 50000

# switch from incremental to full if the number of groups (and records over threshold) is over this threshold
# i.e. needs to be over 100 groups and over 300000 records
# {valueType: "integer"}
# grouperClient.syncTableDefault.switchFromIncrementalToFullIfOverGroupCount = 100

```

All settings specific to provisioner (in this case "personSource" is a variable for the provisioner (grouper.client.properties)

```
# grouper client or loader database key where copying data from
# the 3rd part is the sync_id.  in this case "personSource".  Defaults to "grouper"
# {valueType: "string"}
#grouperClient.syncTable.personSource.databaseFrom = 

# table or view where copying data from, include the schema if needed
# {valueType: "string"}
#grouperClient.syncTable.personSource.tableFrom = 

# grouper client or loader database key where copying data to
# {valueType: "string"}
#grouperClient.syncTable.personSource.databaseTo = 

# grouper client or loader database key (readonly) if large queries should be performed against a different database
# {valueType: "string"}
#grouperClient.syncTable.personSource.databaseToReadonly = 

# table or view where copying data to, include the schema if needed
# {valueType: "string"}
#grouperClient.syncTable.personSource.tableTo = PERSON_SOURCE_TEMP

# columns must match in from and to tables, you can specify columns or do all with an asterisk
# {valueType: "string"}
#grouperClient.syncTable.personSource.columns = *

# if there is a primary key, list it, else list the composite keys.  note, this doesnt
# have to literally be the database prmiary key, just need to be a unique col(s) in table
# {valueType: "string"}
# grouperClient.syncTable.personSource.primaryKeyColumns = penn_id

# if doing fullSyncChangeFlag (look for a col that says if the rows are equal, e.g. a timestamp or a checksum)
# {valueType: "string"}
# grouperClient.syncTable.personSource.changeFlagColumn = check_sum

# the grouping column is what is uniquely selected, and then batched through to get data.  Optional.
# for groups this should be the group uuid
# {valueType: "string"}
# grouperClient.syncTable.personSource.groupingColumn = penn_id

# the grouping column is what is uniquely selected, and then batched through to get data, defaults to global setting
# {valueType: "integer"}
# grouperClient.syncTable.personSource.groupingSize = 10000

# size of jdbc batches
# {valueType: "integer"}
# grouperClient.syncTable.personSource.batchSize = 800

# number of bind vars in select
# {valueType: "integer"}
# grouperClient.syncTable.personSource.maxBindVarsInSelect = 900

# switch from incremental to full if the number of incrementals is over this threshold
# if this is less than 0, then it will not switch from incremental to full
# {valueType: "integer"}
# grouperClient.syncTable.personSource.switchFromIncrementalToFullIfOverRecords = 300000

# switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
# fullSyncChangeFlag, fullSyncFull, fullSyncGroups
# {valueType: "string"}
# grouperClient.syncTable.personSource.switchFromIncrementalToFullSubtype = fullSyncFull

# switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
# if this is less than 0, then it will not switch from incremental to group
# {valueType: "integer"}
# grouperClient.syncTable.personSource.switchFromIncrementalToGroupIfOverRecordsInGroup = 50000

# switch from incremental to full if the number of groups (and records over threshold) is over this threshold
# i.e. needs to be over 100 groups and over 300000 records
# {valueType: "integer"}
# grouperClient.syncTable.personSource.switchFromIncrementalToFullIfOverGroupCount = 100

# if querying a real time table, this is the table, needs to have primary key columns.
# each record will check the source and destination and see what to do
# {valueType: "string"}
# grouperClient.syncTable.personSource.incrementalPrimaryKeyTable = real_time_table

# name of a column that has a sequence or last updated date.  
# must be in the incrementalPrimaryKeyTable if incremental primary key sync
# {valueType: "string"}
# grouperClient.syncTable.personSource.incrementalProgressColumn = last_updated

# name of a column that has a sequence or last updated date.  
# must be in the main data table if incremental all columns
# {valueType: "string"}
# grouperClient.syncTable.personSource.incrementalAllColumnsColumn = last_updated

# database where status table is.  defaults to "grouper"
# {valueType: "string"}
# grouperClient.syncTable.personSource.statusDatabase = grouper

```

grouper-loader.properties to schedule jobs

```
################################
## Table sync jobs
## tableSync jobs should use class: edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
## and include a setting to point to the grouperClient config, if not same: otherJob.<otherJobName>.grouperClientTableSyncConfigKey = key
## this is the subtype of job to run: otherJob.<otherJobName>.syncType = fullSyncFull    
## (can be: fullSyncFull, fullSyncGroups, fullSyncChangeFlag, incrementalAllColumns, incrementalPrimaryKey)
################################

# Object Type Job class
# {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
# otherJob.membershipSync.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob

# Object Type Job cron
# {valueType: "string"}
# otherJob.membershipSync.quartzCron = 0 0/30 * * * ?

# this is the key in the grouper.client.properties that represents this job
# {valueType: "string"}
# otherJob.membershipSync.grouperClientTableSyncConfigKey = memberships

# fullSyncFull, fullSyncGroups, fullSyncChangeFlag, incrementalAllColumns, incrementalPrimaryKey
# {valueType: "string"}
# otherJob.membershipSync.syncType = fullSyncFull

```

## Logging

Make sure log4j jar is in the classpath, and configure log4j.properties

```
log4j.logger.edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncLog =  DEBUG, grouper_stdout
log4j.additivity.edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncLog = false
```

You will see entries like this. Note, there is an entry every minute for in progress jobs (finalLog: false)

```
2019-05-06 09:13:57,246: [Thread-20] DEBUG GrouperClientLog.debug(92) -  - fullSync: true, key: personSourceTest, finalLog: false, state: inserts, databaseFrom: grouper, tableFrom: testgrouper_sync_subject_from, databaseTo: grouper, tableTo: testgrouper_sync_subject_to, totalCountFrom: 25000, fromGroupingUniqueValues: 25000, groupingsToDelete: 0, numberOfBatches: 3, currentBatch: 2, rowsSelectedFrom: 25000, rowsSelectedTo: 0, sqlBatchExecute: 13 of 25
2019-05-06 09:14:45,341: [main] DEBUG GrouperClientLog.debug(92) -  - fullSync: true, key: personSourceTest, finalLog: true, state: deletes, databaseFrom: grouper, tableFrom: testgrouper_sync_subject_from, databaseTo: grouper, tableTo: testgrouper_sync_subject_to, totalCountFrom: 25000, fromGroupingUniqueValues: 25000, groupingsToDelete: 0, numberOfBatches: 3, currentBatch: 2, rowsSelectedFrom: 25000, rowsSelectedTo: 0, sqlBatchExecute: 24 of 25, rowsNeedInsert: 25000, took: 0:01:48.181
2019-05-06 09:14:45,692: [main] DEBUG GrouperClientLog.debug(92) -  - fullSync: true, key: personSourceTest, finalLog: true, state: deletes, databaseFrom: grouper, tableFrom: testgrouper_sync_subject_from, databaseTo: grouper, tableTo: testgrouper_sync_subject_to, totalCountFrom: 25000, fromGroupingUniqueValues: 25000, toGroupingUniqueValues: 25000, groupingsToDelete: 0, numberOfBatches: 3, currentBatch: 2, rowsSelectedFrom: 25000, rowsSelectedTo: 25000, rowsWithEqualData: 25000, took: 0:00:00.330
2019-05-06 09:14:46,032: [main] DEBUG GrouperClientLog.debug(92) -  - fullSync: true, key: personSourceTest, finalLog: true, state: deletes, databaseFrom: grouper, tableFrom: testgrouper_sync_subject_from, databaseTo: grouper, tableTo: testgrouper_sync_subject_to, totalCountFrom: 25000, fromGroupingUniqueValues: 25000, toGroupingUniqueValues: 25000, groupingsToDelete: 1, sqlBatchExecute: 0 of 1, numberOfBatches: 3, currentBatch: 2, rowsSelectedFrom: 25000, rowsSelectedTo: 24999, rowsWithEqualData: 24998, rowsNeedInsert: 1, rowsNeedUpdate: 1, took: 0:00:00.318

```

1.

---
title: "Grouper SQL database sync from sql server"
space: Grouper
pageId: 28549544
version: 4
lastUpdated: 2026-07-01T05:41:48.301Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549544/Grouper+SQL+database+sync+from+sql+server
---

You can sync a view to a table or table to a table from/to disparate databases. Add the external systems. If the database driver jar is not in the container you need to add it to WEB-INF lib.

Example: Add two tables and a view in SQL server. First table is source of data. View selects from that. Second table is destination of data. This is just a test to see it work, it is nonsensical.

```
CREATE TABLE grouper_v2_5.dbo.UMBC_STAFF_IN_DUO (
	subject_id varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	some_date date NULL
) GO;

INSERT INTO grouper_v2_5.dbo.UMBC_STAFF_IN_DUO (subject_id,some_date)
	VALUES ('some_vale',CAST('12/01/2019' as date) );
commit;

create view UMBC_STAFF_IN_DUO_v as select * from grouper_v2_5.dbo.UMBC_STAFF_IN_DUO;

CREATE TABLE grouper_v2_5.dbo.UMBC_STAFF_IN_DUO2 (
	subject_id varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	some_date date NULL
) GO;
```

Configure an external system in the UI, here is the exported resulting config. Note, you either need to configure the database in the connect string or you need to fully qualify the object names, e.g. grouper_v2_5.dbo.UMBC_STAFF_IN_DUO2. In this case we will configure the database name in the jdbc connect string

(External system screen), or grouper-loader.properties

```
db.sqlServer.testQuery = select count(*) from UMBC_STAFF_IN_DUO
db.sqlServer.url = jdbc:sqlserver://localhost:14433;databaseName=grouper_v2_5
db.sqlServer.pass = *******
db.sqlServer.user = grouper_v2_5
db.sqlServer.driver = com.microsoft.sqlserver.jdbc.SQLServerDriver
```

Configure a table sync (grouper.client.properties)

```
# table or view where copying data from, include the schema if needed
grouperClient.syncTable.test_sql_sync.tableFrom = UMBC_STAFF_IN_DUO_v
grouperClient.syncTable.test_sql_sync.databaseFrom = sqlServer
 
# table or view where copying data to, include the schema if needed
grouperClient.syncTable.test_sql_sync.tableTo = UMBC_STAFF_IN_DUO2
grouperClient.syncTable.test_sql_sync.databaseTo = sqlServer
 
# columns must match in from and to tables, you can specify columns or do all with an asterisk
grouperClient.syncTable.test_sql_sync.columns = *
 
# if there is a primary key, list it, else list the composite keys.  note, this doesnt
# have to literally be the database prmiary key, just need to be a unique col(s) in table
grouperClient.syncTable.test_sql_sync.primaryKeyColumns = subject_id
 
# the grouping column is what is uniquely selected, and then batched through to get data.  Optional.
# for groups this should be the group uuid
grouperClient.syncTable.test_sql_sync.groupingColumn = 
```

Configure a job to run that (grouper-loader.properties)

```
# Object Type Job class
otherJob.test_sql_sync.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
 
# Object Type Job cron
otherJob.test_sql_sync.quartzCron = 0 0 8 * * ?
 
# this is the key in the grouper.client.properties that represents this job
otherJob.test_sql_sync.grouperClientTableSyncConfigKey = test_sql_sync
 
# fullSyncFull, fullSyncGroups, fullSyncChangeFlag, incrementalAllColumns, incrementalPrimaryKey
otherJob.test_sql_sync.syncType = fullSyncFull
```

Works fine

```
select * from UMBC_STAFF_IN_DUO2;

subject_id|some_date |
----------|----------|
some_vale |2019-12-01|
```

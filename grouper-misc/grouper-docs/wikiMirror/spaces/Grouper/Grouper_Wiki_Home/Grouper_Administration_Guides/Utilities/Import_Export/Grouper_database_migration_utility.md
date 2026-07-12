---
title: "Grouper database migration utility"
space: Grouper
pageId: 28549706
version: 12
lastUpdated: 2026-07-01T05:41:20.571Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549706/Grouper+database+migration+utility
---

## Overview

 The Grouper database migration utility copies a Grouper registry from one database to another. It runs as a GrouperShell (gsh) command that reads from a source database connection and writes to a target connection, creating the tables, then the data, then the indexes, foreign keys, and views. The source and target may be different database vendors (for example Oracle to MySQL), but both sides must be the **same Grouper version**.

 > Introduced in Grouper **v2.5.24+** (2020). The utility (`edu.internet2.middleware.grouper.ddl.GrouperDdlDataMigration`) is present and essentially unchanged in the currently supported releases, confirmed in **v4 and v6** (also present in v7).

 This utility is **not a core feature of Grouper**. It is experimental and does not always work — your mileage may vary. Its performance is acceptable but not as good as software built for data migration (for example Talend or another ETL tool). For large migrations, or high-pressure production migrations, use a dedicated ETL tool instead. The manual ETL approach is: create the tables in the target with the Grouper scripts (without foreign keys, indexes, or views); set the source read-only (at the database level, or via `grouper.properties` for web services); migrate the data; set the source back to read/write; then add the indexes, foreign keys, and views from Grouper.

 > **Required privileges:** this is a server-side administrative operation, not a Grouper UI or web-service action. Running it requires shell access to a Grouper install, the ability to edit `grouper.hibernate.properties` and `grouper-loader.properties`, and a root gsh session. It reads and writes whole registries, so the operator is effectively a full registry administrator.

 > Make the source database read-only before you migrate. If the source changes mid-migration, foreign keys in the target can be left inconsistent.

 

## Performance measurements

 These are sample observations; actual times depend on data size, network bandwidth and latency in both directions (reading is generally a no-op since it happens concurrently with writing and is faster), and the target database's speed at applying indexes.

 

- 126K groups, 500K memberships: about 25 minutes, Oracle to Oracle.
- 10K groups, 200K memberships: about 1.5 hours, Oracle to AWS Aurora.
- The same dataset Oracle to MySQL took 10 hours, but most of that was applying indexes (so a different tool would not have done better).

 

## Using this utility

 You need two database connections. One can be the standard Grouper connection (named `grouper`); configure the other (the target, or both) in `grouper-loader.properties` using the `db.<name>.*` external-connection keys.

 You may want to set this temporarily in `grouper.hibernate.properties` to batch inserts:

 
```text
hibernate.jdbc.batch_size = 1000
```

 Configure the additional database connection (here named `mysqlDb`) in `grouper-loader.properties`:

 
```text
db.mysqlDb.user         = grouper_v2_5
db.mysqlDb.pass         = *********
db.mysqlDb.url = jdbc:mysql://localhost:3306/grouper_v2_5?useSSL=false
```

 The target database should have no Grouper tables. If Grouper tables already exist, that is fine — the utility assumes there was a failed migration, that all tables are present, and that there are no indexes, constraints, or views in the target; it picks back up on tables that have no rows in the target. If the migration failed on a particular table, delete all partial rows from that table in the target and restart.

 Run this gsh command:

 
```java
// note: make sure the source is readonly or your foreign keys will be hosed
import edu.internet2.middleware.grouper.ddl.GrouperDdlDataMigration;
new GrouperDdlDataMigration().assignDatabaseFrom("grouper").assignDatabaseTo("mysqlDb").migrateDatabase();
```

 The migration runs in five steps, logged as it goes: STEP1 checks whether Grouper tables already exist in the target; STEP2 creates the tables; STEP3 analyzes the tables and columns; STEP4 syncs the row data table by table; STEP5 creates the indexes, foreign keys, and views. Indexes are therefore added after the data is migrated.

 Sample output is shown below (trimmed). Note these are not realistic performance numbers — a real run should be faster. The excerpt is from a 2020 development run on v2.5.0; paths and version strings will differ on your install.

  
```text
log4j:WARN No such property [maxBackupIndex] in org.apache.log4j.DailyRollingFileAppender.
2020-04-23 04:10:23,864: [main] WARN  GrouperVersion.grouperVersion(123) -  - Can't find version of grouper jar, using 2.5.0
Grouper starting up: version: 2.5.0, build date: null, env: <no label configured>
grouper.properties read from: D:\mchyzer\git\grouper_v2_5\grouper\target\classes\grouper.properties
grouper.hibernate.properties: grouper_v2_5@jdbc:mysql://localhost:3302/grouper_v2_5?useSSL=false

elapsed: 00:00:15.013
elapsed: 00:00:30.023
...
STEP1: are grouper tables are in destination? false
STEP2: creating tables and indexes in destination...
STEP2: complete
STEP3: analyzing tables and columns...
STEP3: complete
STEP4: syncing tables...
elapsed: 00:03:00.098, state: STEP4: syncing tables..., insertingBatch: 0, table: grouper_attribute_assign, rowsTo: 0, rowsFrom: 4194, selectingBatch: 0
elapsed: 00:05:30.173, state: STEP4: syncing tables..., insertingBatch: 0, table: grouper_pit_group_set, rowsTo: 0, rowsFrom: 13598, selectingIdsFrom: true
...
STEP4: complete
Success: table: grouper_attr_assign_action migrated 139 rows
Success: table: grouper_attribute_assign migrated 4194 rows
Success: table: grouper_attribute_def migrated 115 rows
Success: table: grouper_memberships migrated 7394 rows
...
Success: table: grouper_stems migrated 951 rows
Success: table: grouper_table_index migrated 4 rows
Success: table: subject migrated 168 rows
Success: table: subjectattribute migrated 505 rows
STEP5: creating foreign keys, and views in destination...
STEP5: complete
Took: 00:21:52.578
```

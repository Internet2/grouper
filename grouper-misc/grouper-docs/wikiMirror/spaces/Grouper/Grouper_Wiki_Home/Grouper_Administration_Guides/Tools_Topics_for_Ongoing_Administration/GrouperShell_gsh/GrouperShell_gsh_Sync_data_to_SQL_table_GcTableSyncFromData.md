---
title: "GrouperShell (gsh) Sync data to SQL table (GcTableSyncFromData)"
space: Grouper
pageId: 28548163
version: 3
lastUpdated: 2026-07-01T05:45:10.449Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548163/GrouperShell+gsh+Sync+data+to+SQL+table+GcTableSyncFromData
---

v2.6.8+

Sync to a table from a dataset, columns, etc

Sample call

> import edu.internet2.middleware.grouperClient.jdbc.tableSync.*; new GcTableSyncFromData().assignDebugMap(debugMap).assignConnectionName(connectionName).assignTableName(tableName). assignColumnNames(columnNames).assignColumnNamesPrimaryKey

## Options

Java docs:  [https://software.internet2.edu/grouper/doc/2.5.x/grouper-misc/grouperClient/apidocs/edu/internet2/middleware/grouperClient/jdbc/tableSync/GcTableSyncFromData.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper-misc/grouperClient/apidocs/edu/internet2/middleware/grouperClient/jdbc/tableSync/GcTableSyncFromData.html)

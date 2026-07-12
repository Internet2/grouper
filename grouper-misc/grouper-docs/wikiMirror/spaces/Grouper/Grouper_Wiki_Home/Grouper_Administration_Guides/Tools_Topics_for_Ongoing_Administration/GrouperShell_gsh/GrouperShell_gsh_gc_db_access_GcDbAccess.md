---
title: "GrouperShell (gsh) gc db access (GcDbAccess)"
space: Grouper
pageId: 28548175
version: 5
lastUpdated: 2026-07-01T05:45:08.372Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548175/GrouperShell+gsh+gc+db+access+GcDbAccess
---

Use this class to get access to the global database connections, create a new connection, and execute sql against them.

Sample call

> Timestamp lastSuccess = new GcDbAccess().sql("select max(ended_time) from grouper_loader_log where job_name = ?") .addBindVar("CHANGE_LOG_consumer_recentMemberships").select(Timestamp.class);

From a database external system

> Integer theOne = new GcDbAccess().connectionName("externalSystemConfigId").sql("select 1 from dual") .select(Integer.class);

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper-misc/grouperClient/apidocs/edu/internet2/middleware/grouperClient/jdbc/GcDbAccess.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper-misc/grouperClient/apidocs/edu/internet2/middleware/grouperClient/jdbc/GcDbAccess.html)

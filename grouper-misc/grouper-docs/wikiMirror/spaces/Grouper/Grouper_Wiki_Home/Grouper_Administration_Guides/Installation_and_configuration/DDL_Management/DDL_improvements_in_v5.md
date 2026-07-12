---
title: "DDL improvements in v5"
space: Grouper
pageId: 28548400
version: 3
lastUpdated: 2026-07-01T05:44:36.796Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548400/DDL+improvements+in+v5
---

In v5 there will be improvements in how DDL is managed in order to make upgrades easier and more reliable.

The problems are:

1. DDL versions and upgrade tasks as sequential and do not accommodate versioning well
2. It is difficult to run upgrades during substantial DDL upgrades since only one container should run and shouldnt do anything until the DDL update and upgrade task is done

Here is the current design:

- Add a new table that holds a row for each DDL update (e.g. 36, 37, 38)
  
  - Column will hold if the update is substantial (i.e. if backwards and forwards compatible)
  - Column if the update is done or not
  - Column can hold the associated upgrade task if applicable
  - The DDL index can be a decimal instead of an integer so past Grouper versions can squeeze in a DDL update
- Add a new table that holds a row for each upgrade task
  
  - Column will hold if the upgrade task has been completed
  - The index can be a decimal instead of an integer so past Grouper versions can squeeze in an upgrade task
- On startup or periodically in thread, see if the DB version is greater than the java version
- If there is a substantial DDL version in the DB (ahead of Java... i.e. if old Grouper container is running), then kill the container with error message, on startup it should hang. Have a config to disable this
- If the container is newer than the DB version, and there is a substantial DDL update in between, and autoDDL is not true (or periodically in a thread), then kill the container with error message. On startup it should wait. Have a config to disable this.
- On startup, when the autoDDL is run, in one JVM, if there is an upgrade task, run that inline before marking the DDL as done

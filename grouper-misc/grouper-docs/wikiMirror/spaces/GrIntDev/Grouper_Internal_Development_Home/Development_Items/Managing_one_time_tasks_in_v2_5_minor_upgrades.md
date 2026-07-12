---
title: "Managing one-time tasks in v2.5+ minor upgrades"
space: GrIntDev
pageId: 48792621
version: 3
lastUpdated: 2026-07-12T07:01:09.726Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792621/Managing+one-time+tasks+in+v2.5+minor+upgrades
---

When you upgrade to Grouper 2.5 from Grouper 2.4, you will need to do some changes and instructions.

This wiki is about upgrading from Grouper 2.5.X to 2.5.Y.

If there is a long running task that needs to be run (e.g. indexing audit records into an audit table), those can be handled by the "other job upgrade tasks daemon"

If there are short running tasks (e.g. DDL changes), these can be run at startup.

This assumes:

1. The Grouper user will be able to adjust DDL
  
  1. If the Grouper user cannot run DDL, an admin will need to run the DDL before upgrading the container
2. There is a locking mechanism so that only one process runs the update
3. The changes are quick
4. The changes are backwards compatible

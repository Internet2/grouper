---
title: "Grouper loader logs"
space: Grouper
pageId: 28560114
version: 8
lastUpdated: 2026-07-01T05:36:19.794Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560114/Grouper+loader+logs
---

This screen allows you to see loader logs for a loader group. You need READ on the group and the ability to see the loader tab (see [Grouper loader on UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554452/Grouper+loader+on+UI) for details). This is in v2.3.0 ui patch #17. Note, this shows logs across all nodes running the grouper daemon since these logs are stored in the database

# Access the loader logs screen

To see this screen, select the Grouper loader tab under "More" on a group screen.

Then select "View loader logs" in the "Loader actions" button.

# Filter

With the filter you can refine your search. You can search for records that were started in a certain range, ended, last updated, etc. Note, you can put various formats in the timestamp fields. e.g.

```
yyyy-mm-d
yy-mm-dd h:mm
yyyy-m-d hh:m:s
yyyy-m-d hh:m:s.xxx
```

If the job loads multiple groups, you can see the subjob loader records too.

You can look for certain statuses.

By default it will display 400 rows but you can change that number (but there is a configured max which is 5k)

# Screen help

You can hover over the column headers or the filter labels to get more information on the meanings.

# Grouper loader logs for simple jobs

For simple jobs (jobs that load one group), for LDAP or SQL, you will see the logs (one per run), for that loader job

# Grouper loader logs for jobs that manage multiple groups

For jobs that manage multiple groups, for LDAP or SQL, you will see the logs (one per run), for that loader job, or you can see logs for subjobs as well

Default screen:

Show subjobs:

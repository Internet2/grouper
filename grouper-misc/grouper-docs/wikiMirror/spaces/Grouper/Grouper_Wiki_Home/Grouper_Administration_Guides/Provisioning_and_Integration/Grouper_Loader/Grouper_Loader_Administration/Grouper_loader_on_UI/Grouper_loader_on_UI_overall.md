---
title: "Grouper loader on UI overall"
space: Grouper
pageId: 28560370
version: 5
lastUpdated: 2026-07-01T05:35:41.707Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560370/Grouper+loader+on+UI+overall
---

This screen shows all loader jobs in the Grouper registry and high level status and stats.

### Access this screen

As a Grouper admin, click on Miscellaneous, then Loader jobs

### Overall loader screen

A list of groups is shown in alphabetical order by ID path. Clicking on the group will go to the main group screen.

The status will show SUCCESS if there is a success in the allowed timeframe. The tooltip on this field will show when the last success was and what the allowed timeframe is.

Actions is similar to actions on the group screen.

Count is the total number of memberships for most recent SUCCESS run.

Recent changes is the sum of inserts+updates+deletes for the last SUCCESS run.

Type shows if SQL or LDAP, and if simple or list of groups.

Source is the subject source if applicable, with tooltip to show the connect string

Query is the query or filter for the job

### Overall loader screen with tooltip on status value

### Overall loader screen has tooltips on all column headings

### Overall status with tooltip on source

### Overall loader screen showing actions menu

### Overall screen running a job from this screen

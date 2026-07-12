---
title: "Grouper loader failsafe - notifications"
space: Grouper
pageId: 28560073
version: 4
lastUpdated: 2026-07-01T05:36:25.641Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560073/Grouper+loader+failsafe+-+notifications
---

In v2.6.6+, there are notifications for failsafes

## Set global defaults configuration

grouper-loader.properties

## Configure job-specific configuration

## Email sample

```
Subject: Grouper failsafe caused job to not run: SQL_SIMPLE__loader:owner1__d932caceb099461f8a110b0c4d77ddc5

Hello,

This is a notification that Grouper job SQL_SIMPLE__loader:owner1__d932caceb099461f8a110b0c4d77ddc5 did not run due to a failsafe condition.  Approve the failsafe in the UI if this is expected.

http://localhost:8402/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.daemonJobs&daemonJobsFilter=SQL_SIMPLE__loader%3Aowner1__d932caceb099461f8a110b0c4d77ddc5

Timestamp: Fri Jan 07 09:53:41 EST 2022

Regards.
```

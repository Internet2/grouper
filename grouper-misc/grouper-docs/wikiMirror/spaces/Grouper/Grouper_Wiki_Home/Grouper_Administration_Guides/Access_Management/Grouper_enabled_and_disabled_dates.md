---
title: "Grouper enabled and disabled dates"
space: Grouper
pageId: 28544574
version: 30
lastUpdated: 2026-07-12T15:26:23.822Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544574/Grouper+enabled+and+disabled+dates
---

## Membership Enabled and Disabled (Start/End) Dates

- Membership assignments and attributes/permissions can have enabled/disabled dates where the assignment might be enabled in the future, or disabled after a certain period of time
- Enabled/disabled dates are supported through the UI, web services, and gsh.
- The [Grouper loader daemon](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545241/Grouper+Daemon) is required for processing the registry periodically to set the enabled/disabled flag based on dates
- Here is the [Jira issue](https://bugs.internet2.edu/jira/browse/GRP-284)
- Example of assigning these via GSH:
  
  
  ```
  membership = group.getImmediateMembership(Group.getDefaultList(), subject, true, true);
  
  membership.setDisabledTime(GrouperUtil.toTimestamp("2009/11/02"));
  
  membership.update();
  
  ```
  
  * Example configuration in grouper-loader.properties
  
  
  ```
  #quartz cron-like schedule for enabled/disabled daemon.  Note, this has nothing to do with the changelog
  #leave blank to disable this, the default is 12:01am, 11:01am, 3:01pm every day: 0 1 0,11,15 * * ?
  
  changeLog.enabledDisabled.quartz.cron = 0 1 0,11,15 * * ?
  
  #number of seconds between checks for changes in enabled/disabled status
  #suggested values are between 900 and 3600
  
  changeLog.enabledDisabled.queryIntervalInSeconds = 3600
  ```
  
  * Example gsh call for testing
  
  
  ```
  loaderRunOneJob("MAINTENANCE__enabledDisabled");
  
  ```
- Enabled/disabled dates are called Start and End Dates in the UI.

## Group Enabled and Disabled (Start/End) Dates as of Grouper v2.5+

- Grouper supports having enabled/disabled dates set on groups as of version 2.5.
- This is also supported through the UI, [web services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services), and [gsh](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh).
- When a group is expired or unexpired, the result is propagated to memberships, privileges (except admin privileges), and attributes. In other words, those objects are also expired or unexpired.
- Here is the [Jira issue](https://todos.internet2.edu/browse/GRP-849) which contains more details on the changes that were made.
- You can edit enabled/disabled dates in the UI when editing a group:

## See Also

See the [Overview of Access Management Features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544689/Access+Management+Features+Overview) page for guidelines of when to use rules, roles, permission limits, and enabled / disabled dates.

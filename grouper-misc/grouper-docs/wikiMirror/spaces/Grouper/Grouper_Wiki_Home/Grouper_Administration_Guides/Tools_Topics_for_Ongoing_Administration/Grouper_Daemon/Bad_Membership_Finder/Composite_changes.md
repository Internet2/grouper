---
title: "Composite changes"
space: Grouper
pageId: 28555757
version: 7
lastUpdated: 2024-02-28T07:31:27.155Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555757/Composite+changes
---

Composite groups have always been a performance problem because their memberships are flattened out in the grouper_memberships table and are calculated in the same transaction as the source of the change. So for example, adding a subject to a group is usually a quick operation. But if that group is directly and/or indirectly part of many composite groups, then that same transaction checks and possibly adjusts all of those groups. This can result in a very large and time consuming transaction. Or if adding a large group to a factor in a composite (or as a member of a group which is a factor of a composite), that results in many many changes. In one institution, changing MFA group policy caused millions of database changes in one transaction which took a couple hours, locked up the DB, and sometimes crashed the app server.

As of Grouper v4.11.0+, we now separate out the composite calculation so that it's not part of the same transaction. It is instead handled by a change log consumer. This involved the following changes:

1. The following operations no longer check for composite membership adjustments.
  
  1. Creating a composite
  2. Deleting a composite
  3. Adding a member to a group
  4. Deleting a member from a group
  5. Other things that result in these operations. e.g. deleting a group / folder
2. For adding and removing direct members, there would be an exception (based on regex of group names) to allow for the old behavior (having the composite calculations done in the same transaction).
  
  1. composites.synchronousCalculationGroupNameRegex
3. The job CHANGE_LOG_consumer_findBadMemberships already handled some of the work needed. It was renamed to CHANGE_LOG_consumer_compositeMemberships
4. The job would make sure composite memberships are correct when composites are created and deleted. And when memberships are updated.
5. The existing code was adjusted to better handle performance with batch lookups and multiple threads.
6. In order to reduce delays in processing the composites, adjustments were made to the schedule of jobs.
  
  1. CHANGE_LOG_changeLogTempToChangeLog - still run once a minute per quartz
  2. CHANGE_LOG_consumer_compositeMemberships - run once a minute per quartz
  3. Both of these jobs are now long running. They will run for about an hour. After each time it checks and makes changes, it will sleep for 500 ms and check again. It will add a new loader log entry if it's been more than a minute since the last one.
7. The added and deleted composite membership counts would be added to the counts in the grouper_loader_log for the CHANGE_LOG_consumer_compositeMemberships job.
8. In the UI, when adding or removing a member, it can quickly check if the group is involved with any composite factors (directly or indirectly). If it is, then the success message returned to the user indicates that the changes are pending.
9. When deleting a group, if the group has composite memberships (because the change log job hasn't gotten to it yet), it's fine for the group delete to directly delete the memberships.

Note use the [subjob checkbox on the daemon screen](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544508/Grouper+Changelog)

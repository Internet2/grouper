---
title: "New employees group"
space: Grouper
pageId: 28543989
version: 3
lastUpdated: 2026-07-01T05:48:58.902Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543989/New+employees+group
---

Despite all of the information we have in our source of record, we don't have a good way to determine if someone is a brand new employee or a rehire. We had a business need to create a group for "new employees" so they could complete required training. We came up with a simple way to do this and we wanted to share in the event it will help someone else.

This is easy as Grouper stores a timestamp for when someone was added to any group within Grouper. To make this work, we need a source group(s) within Grouper that subjects are added to via some loader job. This source group will then be used in the “new employees” group via another loader job. When someone is added to the source group, an audit record is created for when that add occurred. We will query that audit record in our “new employees” group and load subjects into the “new employees” group based on when they were added to the source group.

1. Create a new group called “New Employees” or whatever you want to call it
2. Create a loader job for the “New Employees” group and add in the following SQL:

```sql
select distinct(subject_id) from grouper_memberships_v where subject_source = '<yourSubjectSource>' and list_type = 'list' and list_name = 'members' and group_name in ('Some:sourceGroup:GoesHere') and to_timestamp(create_time/1000) > (now() - interval '30 days');
```

Note: the source group in this case is “Some:sourceGroup:GoesHere”  
 Note: the days to go back on the source group is 30

3. Save the loader and run it

Subjects will be added to the “New Employees” group if they have been added to the source group within the last 30 days. If they were added more than 30 days ago, the will fall out of the “New Employees” group.

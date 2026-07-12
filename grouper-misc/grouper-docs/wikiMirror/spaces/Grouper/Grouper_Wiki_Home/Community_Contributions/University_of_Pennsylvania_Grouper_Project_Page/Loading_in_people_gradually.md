---
title: "Loading in people gradually"
space: Grouper
pageId: 28544094
version: 5
lastUpdated: 2019-02-11T22:36:52.617Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544094/Loading+in+people+gradually
---

At Penn we want to enforce two step authentication but we do not want to overwhelm our support staff.

Construct a loader to gradually load up a group, 200 people every hour. Allow blackout periods.

This table sets up the timeperiods

```
CREATE TABLE AUTHZADM.two_step_timeperiod_enabled
(
  from_date  DATE                               NOT NULL,
  to_date    DATE                               NOT NULL
)
```

Identify the group to get the members from. Note, these should not be people who are already enrolled in two step.

```
penn:isc:ait:apps:twoFactor:groups:requiredUsersStudent:twoStepStudentsNotEnrolled
```

Subtract people in the target group, or it will reload the same members each time

```
penn:isc:ait:apps:twoFactor:groups:requiredUsersStudent:twoStepStudentsNotEnrolledOrRequired
```

Get a query to get 200 members from the group

```
CREATE OR REPLACE VIEW AUTHZADM.two_step_Require_students_v
BEQUEATH DEFINER
AS 
select subject_id, 
case
when exists (select 1 from two_Step_timeperiod_enabled where sysdate between from_date and to_date )
then 'T'
else 'F'
end as load_now from grouper_memberships_lw_v gmlv 
where group_name = 'penn:isc:ait:apps:twoFactor:groups:requiredUsersStudent:twoStepStudentsNotEnrolledOrRequired'
and list_name = 'members' and rownum <= 200;
```

Make a group to hold the eventual members

```
penn:isc:ait:apps:twoFactor:groups:requiredUsersStudent:twoStepRequiredStudentsGradual
```

Loader job will add any members already in the group, and add 200 members every hour, if the time period is correct

```
select subject_id, 'pennperson' as subject_source_id
from grouper_memberships_lw_v gmlv where group_name = 'penn:isc:ait:apps:twoFactor:groups:requiredUsersStudent:twoStepRequiredStudentsGradual'
and list_name = 'members' and subject_source = 'pennperson'
union
select subject_id, 'pennperson' as subject_source_id
from two_step_Require_students_v where load_now = 'T'
```

Test by filling up the table

Then look at the loader logs

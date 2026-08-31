---
title: "Find where any group in a folder is used in other places"
space: Grouper
pageId: 28544053
version: 1
lastUpdated: 2019-06-07T22:13:31.708Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544053/Find+where+any+group+in+a+folder+is+used+in+other+places
---

We are replacing our payroll system, and "job grade" is going way. We have basis groups on job grade. To find out where they are used in memberships or privileges:

```
select distinct group_name group_contains_target_group, gg_member.name target_group_member, list_name  from grouper_memberships_lw_v gmlv, grouper_groups gg_member
where GMLV.SUBJECT_ID = GG_MEMBER.ID and GMLV.SUBJECT_SOURCE = 'g:gsa'
and gg_member.name like 'penn:community:employee:jobGrade:%'
and gmlv.group_name not like 'penn:community:employee:jobGrade:%'
order by 1, 2, 3
```

Will show the group that contains a group, the member group, and if member or privilege assignment

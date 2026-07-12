---
title: "Recovering a deleted group / memberships"
space: Grouper
pageId: 28547363
version: 4
lastUpdated: 2026-07-01T05:47:00.063Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547363/Recovering+a+deleted+group+memberships
---

## Recovering deleted memberships

If the memberships of a group are deleted, you can run this query to see the direct memberships in the past (point in time)

```
select gpm.subject_source, gpm.subject_id, gpf."name", TO_TIMESTAMP(gpmem.start_time/1000000) , 
TO_TIMESTAMP(gpmem.end_time/1000000)  from grouper_pit_memberships gpmem, grouper_pit_groups gpg, grouper_pit_members gpm,
grouper_pit_fields gpf 
where gpg.id = gpmem.owner_group_id and gpm.id = gpmem.member_id and gpmem.field_id = gpf.id
and gpg."name" = 'a:b:c'

```

Look at the end dates of the results (and the list name to see if it is a membership or privilege, and import the users back to the group (as a replace)

In this case if the event occurred at 9:45am on 11/1/2024, then these are the subject IDs to restore

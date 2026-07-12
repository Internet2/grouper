---
title: "Grouper reporting use cases"
space: Grouper
pageId: 28560096
version: 12
lastUpdated: 2026-07-01T05:36:22.667Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560096/Grouper+reporting+use+cases
---

This wiki describes use cases for [reporting in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554409/Grouper+reporting).

Optionally there is a description of how to write the query to solve the problem.

Note: some details can be left off feature is in scope

## Membership report with attributes and other memberships (as booleans)

From Chris and Shilen

For example, we have a group for the arts and sciences school, we want each membership in a row, and some other attributes, and memberships about MFA

```
SELECT gmlv.subject_id PERSON_ID,
       gm.name NAME,
       gm.description DESCRIPTION,
       mea.net_id NET_ID,
       mea.active_code ACTIVE_CODE,
       -- see if enrolled in two step
       CASE WHEN EXISTS (SELECT 1 FROM grouper_memberships_lw_v gmlv2 WHERE gmlv2.member_id = gmlv.member_id 
                          and gmlv2.group_name = 'app:twoFactor:enrolledUser' and gmlv2.list_name = 'members')
             THEN 'T' ELSE 'F' END as ENROLLED_IN_TWO_STEP,
FROM grouper_memberships_lw_v gmlv, grouper_members gm, my_extra_attributes mea
WHERE gmlv.subject_id = mea.my_id(+)                         --outer join so if they are in the group and not in attribute table they still show up
  AND gmlv.list_name = 'members'
  AND gmlv.subject_source = 'mypeople'
  AND gm.id = gmlv.member_id
  -- report has rows for each person in the arts and sciences org
  AND gmlv.group_name = 'ref:org:artsAndSciences'
```

## Check all groups in a folder with a certain name, return all members, and if they are active employees

From Matt Black

For example, all admin groups for apps

```
SELECT gmlv.group_name GROUP_NAME,
       gmlv.subject_id PERSON_ID,
       gm.name NAME,
       gm.description DESCRIPTION
       -- see if active employees
       CASE WHEN EXISTS (SELECT 1 FROM grouper_memberships_lw_v gmlv2 WHERE gmlv2.member_id = gmlv.member_id
                          and gmlv2.group_name = 'ref:payroll:activeEmployee' and gmlv2.list_name = 'members')
             THEN 'T' ELSE 'F' END as ACTIVE_EMPLOYEE,
FROM grouper_memberships_lw_v gmlv, grouper_members gm
WHERE gmlv.list_name = 'members'
  AND gmlv.subject_source = 'mypeople'
  AND gm.id = gmlv.member_id
  -- all groups with name 'admins' in the 'apps' folder
  AND gmlv.group_name like 'app:%:admins'
```

## Make sure group structure is ok, and memberships

From Matt Black

Check for the presence or absence of a group (based on a specific name) inside a folder (and it's sub folders) and return a report of folders with (or without) the group and membership of the group if it was found.

```
select GS.NAME folder_name, 'T' as admin_exists, gg.name as group_name, GMLV.SUBJECT_ID 
  from grouper_stems gs, grouper_groups gg, grouper_memberships_lw_v gmlv
  where GS.NAME like 'some:folder%' 
  and GMLV.GROUP_ID(+) = GG.ID and GMLV.SUBJECT_SOURCE(+) = 'somesourceid' and GMLV.LIST_NAME(+) = 'members'
  and GG.PARENT_STEM = GS.ID and gg.extension = 'admin'
union
select GS.NAME folder_name, 'F' as admin_exists, null as group_name, null as subject_id from grouper_stems gs 
  where GS.NAME like 'some:folder%' 
  and not exists ( select 1 from grouper_groups gg where GG.PARENT_STEM = GS.ID and gg.extension = 'admin' );

```

## Membership additions to a group that happened yesterday

Note: adjust the subject source and group name

```
select * from grouper_pit_mship_group_lw_v gpmgv where gpmgv.group_name = 'test:someGroup2' and gpmgv.subject_source = 'jdbc' and gpmgv.the_end_time is null and gpmgv.the_start_time /1000000 > extract (epoch from date_trunc('day', current_date - Integer '1')) and gpmgv.the_start_time /1000000 < extract (epoch from date_trunc('day', current_date))
```

## "Full sync" / "Full check" reporting

From Matt Black

The report can ( based on config values at run time) either report all memberships ( on both sides) or just output deltas. ( When doing a "check" no changes are made, and when doing a "sync" mode.)

```
In another pass we could create a report based on loading/provisioning.  Note, in the first pass there is no "sync" at the end of reporting, that is scheduled in the provisioning
```

## "Full sync" reporting

From Matt Black

The report can ( based on config values at run time) either report all memberships ( on both sides) or just output deltas and make corrections during the process.

```
In another pass we could create a report based on loading/provisioning.  Is the other side of the provisioning in a database?  Note, in the first pass there is no "sync" at the end of reporting, that is scheduled in the provisioning
```

## "Full check" reporting

From Matt Black

The report can ( based on config values at run time) either report all memberships ( on both sides) or just output deltas. ( When doing a "check" no changes are made on either side.)

```
In another pass we could create a report based on loading/provisioning.  Is the other side of the provisioning in a database?  Note, in the first pass there is no "sync" at the end of reporting, that is scheduled in the provisioning
```

## Group math changes

From Matt Black

It is important to be able to show an audit history (over time) how the group math for an Access Policy Group was formed and maintained.

It also appears the the effective memberships for a composite do not show up in the "View membership audit log" reports in the UI for a composite group at this time.

## Grouper User login tracking

From Matt Black

While it should also be possible to collect user activity audits from outside of grouper. It would be nice to also have "user login" audited in grouper too.

## Grouper daily report details

From Matt Black

"WITHIN LAST DAY:" reports would be nice too. From multiple points in the folder structure. ( Think "per application", "SOR", etc...)

## Enhanced reports for Loader jobs

From Matt Black

Grouper daily report only shows "global level summary". It would be nice to be able to see heuristics about each single job to track and identify jobs that "struggle some times", or "have anomalies from time to time".

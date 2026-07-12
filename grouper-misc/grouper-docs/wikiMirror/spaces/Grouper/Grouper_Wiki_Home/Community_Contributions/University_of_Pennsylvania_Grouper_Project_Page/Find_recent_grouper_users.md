---
title: "Find recent grouper users"
space: Grouper
pageId: 28544173
version: 8
lastUpdated: 2026-07-01T05:48:46.262Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544173/Find+recent+grouper+users
---

## Current method with postgres

Users with at least 10 audits in last 60 days

```
create view penn_recent_users_v as
SELECT GV.LOGGED_IN_subject_ID,
            COUNT (*) AS the_count
       FROM grouper_audit_entry_v gv
      WHERE     created_on >
                   (  cast (1000 * (extract(EPOCH from clock_timestamp()) - (60*60*24*(cast(60 as bigint)))) as bigint))
            AND gv.logged_in_subject_id IS NOT NULL
            AND logged_in_source_id = 'pennperson'
   GROUP BY LOGGED_IN_subject_ID
     HAVING COUNT (*) > 10
```

Loader job to have a list of users, can get their subject_ids and convert to emails, bcc people

## Old method with Oracle

This query ([change the millis](https://currentmillis.com/) to be a specific date) finds users since 4/22/2019 with more than two audit records

this is penn specific, you can tweak it

```
select GV.LOGGED_IN_subject_ID , count(*) as the_count 
from grouper_audit_entry_v gv where created_on > 1555905600000 
and gv.logged_in_subject_id is not null and logged_in_source_id = 'pennperson'
group by LOGGED_IN_subject_ID having count(*) > 2;
```

Note, this is a loader query to get users in the last 60 days who have done two or more actions

```
CREATE OR REPLACE FORCE VIEW PENN_RECENT_USERS_V
(
   LOGGED_IN_SUBJECT_ID,
   THE_COUNT
)
AS
  SELECT GV.LOGGED_IN_subject_ID, COUNT (*) AS the_count
    FROM grouper_audit_entry_v gv
   WHERE     created_on >
                (  EXTRACT (
                      DAY FROM (  SYS_EXTRACT_UTC (
                                     SYSTIMESTAMP - INTERVAL '60' DAY)
                                - TO_TIMESTAMP ('1970-01-01', 'YYYY-MM-DD')))
                 * 86400000)
         AND gv.logged_in_subject_id IS NOT NULL
         AND logged_in_source_id = 'pennperson'
GROUP BY LOGGED_IN_subject_ID
  HAVING COUNT (*) > 2
```

## Find users with privilege

Users with group privileges

```
select gmlv.subject_id, gmlv.group_name from grouper_memberships_lw_v gmlv where gmlv.subject_source = 'whateverSubjectSourceId' and gmlv.list_type = 'access';
```

Users with folder privilege

```
select gmslv.subject_id, gmslv.stem_name from grouper_mship_stem_lw_v gmslv where gmslv.subject_source = 'whateverSubjectSourceId' and gmslv.list_type = 'naming';
```

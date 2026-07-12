---
title: "Grouper membership report on multiple groups with end date"
space: Grouper
pageId: 28564215
version: 2
lastUpdated: 2026-07-01T05:35:30.637Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564215/Grouper+membership+report+on+multiple+groups+with+end+date
---

This is a report over a handful of groups to show membership, if they are active at the institution (based on reference group), some personal info, and the end date of their membership in each group. The report readers get an email when the weekly report is ready

## Report sample

## View 1: list of users

List of users in the groups of interest. We do not want the end date to be null so set it to 2099

```
CREATE OR REPLACE VIEW authz_wday_learning_rep_help_v
AS SELECT gg.name AS group_name,
    -- if its null just set to 2099 in micros
    COALESCE(gms.immediate_mship_disabled_time, 4070926800000000)/1000 AS end_millis,
    gms.member_id
   FROM grouper_memberships_all_v gms,
    grouper_groups gg,
    grouper_fields gfl
  WHERE gms.owner_group_id = gg.id 
  AND gms.field_id = gfl.id 
  and gfl.name = 'members'
  AND gms.immediate_mship_enabled = 'T' 
  AND gg.name in ('penn:med:admin:ocr:apps:workdayLearning:WorkdayLearningEEUsersUPHS_OCR', 
  'penn:provost:vpr:apps:workdayLearning:WorkdayLearningEEUsersUPHS_ORS', 
  'penn:evp:finance:ftd:apps:workdayLearning:WorkdayLearningEEUsersUPHS_FTD', 
  'penn:provost:vpr:apps:workdayLearning:WorkdayLearningEEUsersUPHS_EHRS', 
  'penn:provost:vpr:apps:workdayLearning:WorkdayLearningEEUsersUPHS_ULAR', 
  'penn:isc:tss:apps:workdayLearning:WorkdayLearningEEUsersUPHS_ISC');

```

## View 2: max end date, order by desc

```
CREATE OR REPLACE VIEW authz_wday_learning_rep_hlp2_v
AS SELECT awlrhv.group_name,
    -- the end date is the max end date (and null means no end date)
    -- order by date descending
    max(awlrhv.end_millis) AS max_end_millis,
    awlrhv.member_id
   FROM authz_wday_learning_rep_help_v awlrhv group by group_name, member_id order by max(awlrhv.end_millis) desc;
```

## View 3: convert dates back to null, lookup if member, get other attributes

```
CREATE OR REPLACE VIEW authz_wday_learning_report_v as
SELECT gm.subject_id AS penn_id,
    gm.subject_identifier0 AS pennkey,
    gm.description,
    awlrhv.group_name,
    -- if it is 2099 then it is null, change it back
    case when max_end_millis = 4070926800000 then null else
    to_char(to_timestamp(max_end_millis::double precision), 'yyyy-mm-dd') end AS end_date,
        -- see if the user is a member of the institution
        CASE
            WHEN (EXISTS ( SELECT 1
               FROM grouper_memberships_lw_v gmlv
              WHERE gmlv.list_name = 'members' AND gmlv.member_id = gm.id AND gmlv.group_name = 'penn:community:activeNonAlumniWithPennname')) THEN 'T'
            ELSE 'F'
        END AS member_of_penn
   FROM authz_wday_learning_rep_hlp2_v awlrhv,
    grouper_members gm
  WHERE gm.subject_source = 'pennperson' 
  AND awlrhv.member_id = gm.id ;
```

## Setup report

Note the query just selects everything from the view, use column names

```
select penn_id, pennkey, description, group_name, end_date, member_of_penn from authz_wday_learning_report_v
```

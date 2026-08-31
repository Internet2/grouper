---
title: "Grouper database independent current time"
space: Grouper
pageId: 28547708
version: 7
lastUpdated: 2026-07-01T05:46:20.502Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547708/Grouper+database+independent+current+time
---

This wiki is generally for Grouper developers or someone troubleshooting recent memberships. In v2.5.30+

There is a Grouper table to hold the current UTC time to be used for joining to other tables. This is so we don't need to use database dependent functions to get the current time, and we can run one unit test on any database to ensure "recent memberships" work correctly.

## grouper_time table

There is one row for the current time

## timeDaemon

grouper-loader.properties

```
# Keep the current time in a database independent way
# {valueType: "class", readOnly: true, mustImplementInterface: "org.quartz.Job"}
otherJob.timeDaemon.class = edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperTimeDaemon

# Run the time daemon every minute
# {valueType: "cron"}
otherJob.timeDaemon.quartzCron = 45 * * * * ?

```

## Why we need this daemon

Here is sample code of how we did this previously for each database

```
      if (GrouperDdlUtils.isHsql()) {
        minEndTimePart = "(1000*(unix_millis(current_timestamp) - (1000*60*60*24*cast(gaaagv_recentMemberships.value_string as int))))";
      } else if (GrouperDdlUtils.isOracle()) {
        minEndTimePart = "(1000000 * (((cast(current_timestamp at time zone 'UTC' as date) - date '1970-01-01')*24*60*60)-(24*60*60*CAST( gaaagv_recentMemberships.value_string AS number ))))";
      } else if (GrouperDdlUtils.isMysql()) {  
        minEndTimePart = "(1000000 * (UNIX_TIMESTAMP() - (60*60*24*CONVERT(gaaagv_recentMemberships.value_string,UNSIGNED INTEGER))))";
      } else if (GrouperDdlUtils.isPostgres()) {
        minEndTimePart = "cast((1000000 * (extract(EPOCH from clock_timestamp()) - (60*60*24*(cast(gaaagv_recentMemberships.value_string as bigint))))) as bigint)";
      }

```

## Example view

See that grouper_time is joined to, row is time_label = 'now', and utc_micros_since_1970 is the current time in micros UTC

```
CREATE VIEW grouper_recent_mships_load_v
(
   group_name,
   subject_source_id,
   subject_id
)
AS
select grmc.group_name_to as group_name,
       gpmglv.subject_source as subject_source_id,
       gpmglv.subject_id as subject_id
from   grouper_recent_mships_conf grmc,
       grouper_pit_mship_group_lw_v gpmglv,
       grouper_time gt,
       grouper_members gm
where  gm.id = gpmglv.member_id
   and gm.subject_resolution_deleted = 'F'
   and gt.time_label = 'now'
   and
       (
          gpmglv.group_id = grmc.group_uuid_from or gpmglv.group_name = grmc.group_name_from
       )
   and gpmglv.subject_source != 'g:gsa'
   and gpmglv.field_name = 'members'
   and
       (
         (
           grmc.include_eligible = 'T'
           and gpmglv.the_active = 'T'
         )
         or (gpmglv.the_end_time >= gt.utc_micros_since_1970 - grmc.recent_micros)
       );
```

## Notes

This table does not need to be exported/imported...

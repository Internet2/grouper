---
title: "Grouper subjects in one group only example"
space: Grouper
pageId: 28548086
version: 9
lastUpdated: 2026-07-01T05:45:24.028Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548086/Grouper+subjects+in+one+group+only+example
---

If you have an application where users can only be in one group at a time (either the first group, or the most recent group), there is a model for that. This example is postgres but you could adapt this to any database.

Here is a folder with some groups and some users in the groups, and some in multiple groups.

Based on those times, if we are doing a flattened structure where each subject stays in their first group, then it should look like this

| Subject | Group |
| --- | --- |
| test.subject.0 | testGroup3 |
| test.subject.1 | testGroup1 |
| test.subject.5 | testGroup3 |
| test.subject.6 | testGroup3 |
| test.subject.8 | testGroup2 |
| test.subject.9 | testGroup1 |

We need another folder, "testFlattened", and each of those groups in there. In each of those groups needs to be each subject based on the first group they are in in the "test" folder.

In order to calculate that, we need to use point-in-time data on the memberships. To make this performant, we need to have an intermediate step, to put these memberships into a table with the subject, group, and start time.

Lets make that table

```
CREATE TABLE grouper_v2_5.test_flattened_groups (
	group_extension varchar(255) NOT NULL,
	subject_id varchar(255) NOT NULL,
	the_start_time_millis int8 NOT NULL,
	PRIMARY KEY (group_extension, subject_id, the_start_time_millis)
);
CREATE INDEX test_flattened_groups_idx ON grouper_v2_5.test_flattened_groups (subject_id, the_start_time_millis);
```

Now we need a view that we can sync to it

```
create view grouper_v2_5.test_flattened_groups_v as
select
    gg.extension as group_extension,
    gpmgl.subject_id,
    gpmgl.the_start_time / 1000 as the_start_time_millis
from
    grouper_pit_mship_group_lw_v gpmgl,
    grouper_groups gg,
    grouper_fields gf
where
    gpmgl.group_id = gg.id
    and gpmgl.subject_source = 'jdbc'
    and gf.name = 'members'
    and gf.id = gpmgl.field_id
    and gpmgl.the_active = 'T'
    and gg.name like 'test:%'
```

```
select * from grouper_v2_5.test_flattened_groups_v tfgv order by subject_id , the_start_time_millis 
```

Now we need to sync that to a table, every 15 minutes. If you want real time ask the team for an example

grouper.client.properties:

```
grouperClient.syncTable.testFlattened.databaseFrom=grouper
grouperClient.syncTable.testFlattened.databaseTo=grouper
grouperClient.syncTable.testFlattened.tableFrom=test_flattened_groups_v
grouperClient.syncTable.testFlattened.tableTo=test_flattened_groups
grouperClient.syncTable.testFlattened.columns=group_extension, subject_id, the_start_time_millis
grouperClient.syncTable.testFlattened.primaryKeyColumns=group_extension, subject_id, the_start_time_millis
```

grouper-loader.properties

```
otherJob.testFlattened.quartzCron=0 0/15 * 1/1 * ? *
otherJob.testFlattened.class=edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.testFlattened.syncType=fullSyncFull
otherJob.testFlattened.grouperClientTableSyncConfigKey=testFlattened
```

Go to grouper daemon ui page and schedule jobs (or bounce daemon).

Run job

Write a view to get the flattened groups out of the table, and into another grouper folder

```
create or replace
view grouper_v2_5.test_flattened_calculated_v as
select
    gm.subject_id,
    (select tfg.group_extension from test_flattened_groups tfg
     where tfg.subject_id = gm.subject_id
       and tfg.the_start_time_millis = (
            select
                min(tfg2.the_start_time_millis) as min
            from
                test_flattened_groups tfg2
            where
                gm.subject_id = tfg2.subject_id)
        limit 1)
  as group_extension
from
    grouper_members gm
where
    gm.subject_id in (select tfg.subject_id from test_flattened_groups tfg)

```

```
select * from grouper_v2_5.test_flattened_calculated_v
```

Make a loader job

```
SQL: select 'testFlattened:' || tfcv.group_extension as group_name, subject_id, 'jdbc' as subject_source_id from grouper_v2_5.test_flattened_calculated_v tfcv
Group SQL (all groups whether have members or not):  select 'testFlattened:' || gg.extension as group_name from grouper_groups gg where gg.enabled ='T' and gg.name like 'test:%'
Groups like: testFlattened:%
```

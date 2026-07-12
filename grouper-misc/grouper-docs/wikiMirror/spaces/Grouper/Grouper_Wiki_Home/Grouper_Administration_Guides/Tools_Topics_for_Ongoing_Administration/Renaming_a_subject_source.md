---
title: "Renaming a subject source"
space: Grouper
pageId: 28544350
version: 6
lastUpdated: 2026-07-01T05:48:35.425Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544350/Renaming+a+subject+source
---

Changing the name of a subject source is a challenging task. One approach to this would be to create the new subject source under the desired name, updating all loader jobs and provisioners to use the new source, and making manual changes in all ad hoc groups. This method would perform mass deletes and insertions, and introduces a large amount of changes: audits, changelog entries, and point in time data. It also obscures the timeline of membership changes, because it will show all memberships being added on the same day.

An alternate approach is to make changes directly in the database. While there is a danger of something going wrong and corrupting data, when done correctly it is a cleaner and quicker solution.

If the subject identifiers, subject name, or email are changing in the new subject source, it would be good to make those changes through data manipulation. These are cached in the GROUPER_MEMBERS table, and will only receive updated data for individual subjects when that subject is viewed in the UI, or for every subject when the USDU process runs once a day. Using staging tables and SQL merges, the cached data can be updated directly in the tables, so it is already mostly correct at the time of the cutover.

## Identifying data to change

In the first step, we obtain counts for the number of records referring to the existing subject source. After the cutover, we will want to check the counts for the new source, to verify a clean transition.

To identify the tables where the name of the subject source is used, this SQL (Postgres) will list them.

```sql
select table_catalog, table_schema, table_name, column_name, data_type, character_maximum_length
from information_schema.columns
 where column_name like '%source%'
   and is_updatable = 'YES'
   and table_schema != 'pg_catalog'
;

```

| table_catalog | table_schema | table_name | column_name | data_type | character_maximum_length |
| --- | --- | --- | --- | --- | --- |
| postgres | public | grouper_members | subject_source | character varying | 255 |
| postgres | public | grouper_pit_members | source_id | character varying | 40 |
| postgres | public | grouper_pit_members | subject_source | character varying | 255 |
| postgres | public | grouper_pit_fields | source_id | character varying | 40 |
| postgres | public | grouper_pit_groups | source_id | character varying | 40 |
| postgres | public | grouper_pit_stems | source_id | character varying | 40 |
| postgres | public | grouper_pit_attribute_def | source_id | character varying | 40 |
| postgres | public | grouper_pit_memberships | source_id | character varying | 40 |
| postgres | public | grouper_pit_group_set | source_id | character varying | 40 |
| postgres | public | grouper_pit_attribute_assign | source_id | character varying | 40 |
| postgres | public | grouper_pit_attr_assn_value | source_id | character varying | 40 |
| postgres | public | grouper_pit_attr_assn_actn | source_id | character varying | 40 |
| postgres | public | grouper_pit_attr_def_name | source_id | character varying | 40 |
| postgres | public | grouper_pit_attr_def_name_set | source_id | character varying | 40 |
| postgres | public | grouper_pit_attr_assn_actn_set | source_id | character varying | 40 |
| postgres | public | grouper_pit_role_set | source_id | character varying | 40 |
| postgres | public | grouper_sync_member | source_id | character varying | 255 |
| postgres | public | grouper_pit_config | source_id | character varying | 40 |
| postgres | public | grouper_rpt_members_v | subject_source | character varying | 255 |

In this example, ignoring grouper_rpt_members_v which is a view, and ignoring source_id in the grouper_pit_* tables which does not refer to the subject source, the tables in question are grouper_members, grouper_pit_members, and grouper_sync_member.

To get the counts of records for the subject source in question:

```sql
select count(*), subject_source from grouper_members where subject_source not in ('g:gsa', 'g:isa') group by subject_source;

```

| count | subject_source |
| --- | --- |
| 29 | enterprise-users |
| 2 | enterprise-services |

```sql
select count(*), subject_source from grouper_pit_members where subject_source not in ('g:gsa', 'g:isa') group by subject_source;

```

| count | subject_source |
| --- | --- |
| 2 | enterprise-services |
| 29 | enterprise-users |

```sql
select count(*), source_id from grouper_sync_member group by source_id;

```

| count | source_id |
| --- | --- |
| 2 | enterprise-users |

## Identifying configuration properties to change

```sql
select config_file_name, config_key, config_value from grouper_config where config_value like '%enterprise-users%';

```

| config_file_name | config_key | config_value |
| --- | --- | --- |
| grouper-loader.properties | provisioner.myProvisioner.subjectSourcesToProvision | enterprise-users |
| subject.properties | subjectApi.source.enterprise-users.id | enterprise-users |
| subject.properties | subjectApi.source.enterprise-users.name | enterprise-users |

Note that the subjectApi.source.<key>.id value is the string used everywhere as the "subject source", in db tables, loader jobs, provisioners, and elsewhere in the UI. The "key" is an internal value and does not appear anywhere else. It just needs to be unique and consistent for a single source. The "name" value in the sample above is a friendly name for the subject source, which appears in a few places in the UI.

During the migration, we can batch replace all of the names with the new names with one SQL statement. Make a note of the config keys related to the existing subject source. We may not want to change these, if the result would be to update its id to the id of our new source.

## Identifying attributes to change

Attributes referring to the subject source are commonly found in loader groups. We can do a batch replace of all the attributes containing the existing source name.

This query combines both direct attribute values and values on an attribute assignment.

```sql
select gaav.value_string, coalesce(g.name, g2.name) as group_name, gadn.name as attribute_def_name
from grouper_attribute_assign_value gaav
join grouper_attribute_assign gaa on gaav.attribute_assign_id = gaa.id
join grouper_attribute_def_name gadn on gaa.attribute_def_name_id = gadn.id
left join grouper_groups g on g.id = gaa.owner_group_id
left join grouper_attribute_assign gaa2 on gaa.owner_attribute_assign_id = gaa2.id
left join grouper_groups g2 on g2.id = gaa2.owner_group_id
where gaav.value_string like '%enterprise-users%';

```

| value_string | group_name | attribute_def_name |
| --- | --- | --- |
| enterprise-users | test:demo:testGroup | etc:attribute:pspng:provision_to |
| selectidassubject_id,'enterprise-users'assubject_identifierfrommy_memberships | test:demo:demoLoader | etc:legacy:attribute:legacyAttribute_grouperLoaderQuery |
| enterprise-users | test:demo:demoLdapLoader | etc:attribute:loaderLdap:grouperLoaderLdapSourceId |

## (optional) Pause loader jobs and provisioner daemons

While the system is still running, you may want to pause loader jobs, provisioner jobs, and custom batch jobs that may use the subject source by name.

If you have a lot of jobs to pause, see below for a SQL command to do this while the containers are down.

## Rename or duplicate the subject source configuration

The new subject source must exist by id in the subject.properties file or equivalent database config, before the containers are brought up. If you are storing subject properties in the database, then you will want to create the new config in the database BEFORE the containers are down and the UI isn't available. If there is any problem with a subject source, containers will fail to start, and there will be no UI to help with fixing issues. Changes can still be made using SQL.

To rename a subject source, simply change the value of subjectApi.source.<key>.id. Ideally this should be done while the containers are down. If this isn't convenient, make the change right before bringing the containers down to minimize impact.

To duplicate a subject source, export the subject.properties from the UI configuration page (if configured in the database), or open the grouper.properties file (if configured from a file). Duplicate the properties starting with subjectApi.source.<old-key>.*, and in the second copy change old-key to new-key. Also make sure to change the id for the new source, since this is what will used as the official "subject source" id.

## Bring down all Grouper containers

If the containers are running while the SQL changes are happening, the system could get confused, and possibly revert some changes made in the database. All containers should be down during this time, so that the data changes can happen in isolation.

## Make database changes

### Table columns

```sql
drop table if exists bkp_grouper_members;

drop table if exists bkp_grouper_pit_members;

drop table if exists bkp_grouper_sync_member;

create table bkp_grouper_members as
    select * from grouper_members where subject_source = 'enterprise-users';

create table bkp_grouper_pit_members as
    select * from grouper_pit_members where subject_source = 'enterprise-users';

create table bkp_grouper_sync_member as
    select * from grouper_sync_member
    where source_id = 'enterprise-users';

drop index member_subjectsourcetype_idx;

UPDATE grouper_members SET subject_source = 'NEW_SOURCE_ID'
 WHERE subject_source = 'enterprise-users';

CREATE UNIQUE INDEX member_subjectsourcetype_idx ON grouper_members (subject_id, subject_source, subject_type);

UPDATE grouper_pit_members SET subject_source = 'NEW_SOURCE_ID'
 WHERE subject_source = 'enterprise-users';

UPDATE grouper_sync_member SET source_id = 'NEW_SOURCE_ID'
 WHERE source_id = 'enterprise-users';

```

### (optional) Disable loader and provisioning jobs

```sql
update grouper_qz_triggers
   set trigger_state = 'PAUSED'
 where trigger_state = 'WAITING'
   and (
       job_name like 'SQL%'
    or job_name like 'LDAP%'
    or job_name like 'CHANGE_LOG_consumer_provisioner_incremental_%'
    or job_name like 'OTHER_JOB_provisioner_full_%'
   )
   and job_name not like 'SQL_GROUP_LIST__etc:attribute:recentMemberships:grouperRecentMembershipsLoader__%';

```

### Update configs

If you need to update a config in the database while the UI is down, make sure to change it in both grouper_config and grouper_pit_config

```sql
update grouper_config set config_value = 'NEW_SOURCE_ID' where config_key = 'subjectApi.source.enterprise-users.id';
update grouper_pit_config set config_value = 'NEW_SOURCE_ID' where config_key = 'subjectApi.source.enterprise-users.id';

```

To do a batch replacement on other uses of the old source name:

```sql
update grouper_config set config_value = replace(config_value, 'enterprise-users', 'NEW_SOURCE_ID') where config_key like '%enterprise-users%';

```

### Update attribute values

```sql
drop table if exists bkp_grouper_attribute_assign_value;

create table bkp_grouper_attribute_assign_value as select * from grouper_attribute_assign_value;

update grouper_attribute_assign_value gaav set value_string = replace(value_string, 'enterprise-users', 'NEW_SOURCE_ID')
where gaav.id in (
    select gaav.id
    from grouper_attribute_assign_value gaav
    join grouper_attribute_assign gaa on gaav.attribute_assign_id = gaa.id
    join grouper_attribute_def_name gadn on gaa.attribute_def_name_id = gadn.id
    left join grouper_attribute_assign gaa2 on gaa.owner_attribute_assign_id = gaa2.id
    where gaav.value_string like '%enterprise-users%';
);

```

### Bring up the UI, disable old subject source

Bring up a UI container. If there are errors with the subject source, the system won't start. In that case, more database or configuration changes are needed to resolve the issue.

Verify group memberships, that they refer to the new subject source name. View Miscellaneous > Subject sources, to see that the new name shows up. Diagnostics on the new subject source should work.

Bring up the other containers once the UI looks good.

If you made a duplicate subject source instead of renaming the existing one, disable the old one on the Miscellaneous > External systems page.

### (optional) Re-schedule jobs

If you want to enable all paused jobs at once, this command will do it. You will want to verify the jobs still work, and don't make major changes. You can also look in loader jobs that specify the suject source, to make sure it is referring to the new source.

```sql
update grouper_qz_triggers
   set trigger_state = 'WAITING'
 where trigger_state = 'PAUSED'
   and (
       job_name like 'SQL%'
    or job_name like 'LDAP%'
    or job_name like 'CHANGE_LOG_consumer_provisioner_incremental_%'
    or job_name like 'OTHER_JOB_provisioner_full_%'
   )
   and job_name not like 'SQL_GROUP_LIST__etc:attribute:recentMemberships:grouperRecentMembershipsLoader__%';

```

### Remove old subject source

If you made a duplicate subject source instead of renaming the existing one, you can remove the old version. But make sure no new subjects for the old source have snuck back into the members table, especially if there are memberships for them.

```sql
select * from grouper_members where subject_source = 'enterprise-users';

select * from grouper_memberships_lw_v where subject_source = 'enterprise-users';

```

# Complex migrations

If the subject migration is more than a simple rename, it is still possible to do that through database manipulation. Identifiers, names, emails, and other data in the grouper_members table can be modified to other data, as long as the subject configuration conforms to the new field data.

In the example below, data is merged from a temporary table for a subject source, where even the subject ID is changing. First, we need to handle records not in the new source by munging their ids, We will need to investigate these further.

```sql
create table temp_userid_mapping as ... /* join grouper_members with the new subject source */ ...

/* clear out the memberships for accounts not in the new source */

update grouper_members set subject_id = concat(subject_id, '-XXXXXXX'), subject_identifier0 = concat(subject_identifier0, '-XXXXXXX'),
                           subject_identifier1 = concat(subject_identifier1, '-XXXXXXX')
where id in (
    select gm.id from grouper_members gm
    left join mp_midpoint_person mp on gm.subject_identifier1 = mp.uid
    where gm.subject_source = 'enterprise-users' and mp.uid is null
    );

update grouper_pit_members set subject_id = concat(subject_id, '-XXXXXXX'), subject_identifier0 = concat(subject_identifier0, '-XXXXXXX')
where source_id in (
    select gm.id from grouper_members gm
    left join mp_midpoint_person mp on gm.subject_identifier1 = mp.uid
    where gm.subject_source = 'enterprise-users' and mp.uid is null
    );

/* Update member table - this won't work if there is any subjectid collision due to unique constraint */

drop index member_subjectsourcetype_idx;

UPDATE grouper_members AS t1
SET
    subject_id = t2.subject_id_new,
    email0 = t2.email_new
FROM temp_userid_mapping AS t2
WHERE
    t1.id = t2.member_id;

CREATE UNIQUE INDEX member_subjectsourcetype_idx ON grouper_members (subject_id, subject_source, subject_type);

/* Update pit member table */

UPDATE grouper_pit_members AS t1
SET
    subject_id = t2.subject_id_new
FROM temp_userid_mapping AS t2
WHERE
    t1.source_id = t2.member_id;

/* Update provisioner data */

UPDATE grouper_sync_member AS t1
SET
    subject_id = t2.subject_id_new
FROM temp_userid_mapping AS t2
WHERE
    t1.member_id = t2.member_id;

```

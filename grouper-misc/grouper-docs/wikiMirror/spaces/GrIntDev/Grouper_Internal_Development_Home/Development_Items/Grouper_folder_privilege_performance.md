---
title: "Grouper folder privilege performance"
space: GrIntDev
pageId: 48792675
version: 18
lastUpdated: 2026-07-12T07:01:11.989Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792675/Grouper+folder+privilege+performance
---

Grouper has a grouper.properties setting

```

OLD

# if folders should be shown only if there is an object inside that the user can see
security.show.folders.where.user.can.see.subobjects = true

NEW

# if all folders should be shown only if there is an object inside that the user can see (or a privilege on that folder).
# this has been re-coded and is the new setting.  The old setting "security.show.folders.where.user.can.see.subobjects" is not used anymore
# {valueType: "boolean", required: true}
security.folders.are.viewable.by.all = false

# put in a group name to exclude non admins who have a lot of privileges who have bad performance
# {valueType: "group"}
security.show.all.folders.if.in.group = $$grouper.rootStemForBuiltinObjects$$:sysadminStemViewers

# recalc in change log if the user has needed privileges in the last X seconds, default to 604800 which is one week
# so if someone has needed stem view privileges in the last week, then have the change log keep it up to date
# 0 means dont do this for anyone (full recalc each time), -1 means do this for everyone who has ever checked stem view,
# other negative values are not valid.
# {valueType: "integer"}
security.folders.view.privileges.recalcChangeLog.ifNeededInLastSeconds = 604800

# log error if performance is above this number of seconds.  tells you to exclude some users or disable feature
# leave blank or -1 to disable
# {valueType: "integer", required: true}
security.show.all.folders.log.above.seconds = 30 

# log security folder view privilege change log consumer
# {valueType: "boolean", required: true}
security.folder.view.privileges.changeLogConsumer.log = false

# log security folder view privilege full daemon
# {valueType: "boolean", required: true}
security.folder.view.privileges.fullDaemon.log = false

```

Pre-v2.6.6 It is recommended to disable this for large deployments since it is too costly to find which objects users have privileges on objects in.

Post-v2.6.6 It is recommend to enable this for all deployments

The problem is it needs to look at all the object types (groups, folders, attributes), for any privilege and join that to folders, and join that to stemSets to walk up the ancestor path.

The fix in 2.6.* is to make part of this calculation a little easier and have a table which has the information cached for users of the UI. This table would store which users can view which folders. This can be joined to stem_set to see which folders to show for a user

| **Table: grouper_stem_view_privilege** |
| --- |
| Column | Data type | Notes |
| stem_uuid | Varchar(40) | UUID of stems (foreign key cascade delete) |
| member_uuid | Varchar(40) | UUID of members (foreign key cascade delete) |
| object_type | Char(1) | G (has group privilege directly in folder)  S (has folder privilege on this folder)  A (has attribute privilege on an attribute directly in this folder) |

Index: grouper_stem_view_priv_stem_idx, grouper_stem_view_priv_mem_idx

The last login table holds last logins for users. This is only for the part of the UI that shows the menu or navigation, not for Custom UIs that dont show that. The API to adjust this table should look for and remove duplicates.

| **Table: grouper_last_login** |
| --- |
| Column | Data type | Notes |
| member_uuid | Varchar(40) | UUID of members (foreign key cascade delete) |
| last_login_millis | Bigint | When last logged in |
| last_stem_view_compute_millis | Bigint | When stem view privs last computed |

Index: grouper_ui_last_login_mem_idx

## Folder view privilege logic

- A nightly "full" sync would
  
  - Make sure that for each user who has logged in the last 7 days (configurable) that their privileges are up to date including removing duplicates
  - Remove rows of users who have not logged in in the last seven days
- Change log consumer
  
  - If a user who has logged in the last X days has a flattened privilege change, adjust the table
- Stem search, if a user isn't computed, they will be computed

## Change in Grouper

The logic to show the folder tree or browse folders needs to read this table.

Grouper will change the default to only show folders that have objects with privileges inside

## Queries

Query the stem privilege table

```
select gm.subject_id, gm.subject_source, gs.name as stem_name, gsvp.object_type 
from grouper_stem_view_privilege gsvp , grouper_members gm , grouper_stems gs 
where gsvp.stem_uuid = gs.id and gsvp.member_uuid = gm.id 
```

## Pre-compute privileges

In v2.6.7+ add users to etc:privilege:stemViewPrecompute and run the OTHER_JOB_stemViewPrivilegesFull full sync daemon to pre-compute users

## Performance test

```
mysql with privileges off:
Find root stems: 19ms
Find root stems: 8ms
Find root stems: 7ms
Find root stems: 8ms
Find root stems: 5ms
Find root stems: 11ms
Find root stems: 7ms
Find root stems: 6ms
Find root stems: 6ms
Find root stems: 5ms
Find root stems: 6ms
Find root stems: 5ms
Find root stems: 4ms

2.5 mysql with privileges on:
Find root stems: 5614ms
Find root stems: 4924ms
Find root stems: 6631ms
Find root stems: 3086ms
Find root stems: 3083ms
Find root stems: 4396ms
Find root stems: 4269ms
Find root stems: 6542ms
Find root stems: 3188ms
Find root stems: 3050ms
Find root stems: 4089ms
Find root stems: 4025ms
Find root stems: 5565ms

2.6 mysql with privileges on not having calculated previously:
Find root stems: 16574ms
Find root stems: 14083ms
Find root stems: 13051ms
Find root stems: 13556ms
Find root stems: 11596ms
Find root stems: 13735ms
Find root stems: 16724ms
Find root stems: 15507ms
Find root stems: 12904ms
Find root stems: 13624ms
Find root stems: 13906ms

2.6 mysql with privileges on yes having calculated previously:
Find root stems: 96ms
Find root stems: 104ms
Find root stems: 110ms
Find root stems: 1160ms
Find root stems: 126ms
Find root stems: 99ms
Find root stems: 865ms
Find root stems: 441ms
Find root stems: 129ms
Find root stems: 118ms
Find root stems: 1008ms
Find root stems: 145ms
Find root stems: 103ms
```

## Proof of concept (outdated)

Postgres DDL

```
CREATE TABLE penngrouper.grouper_stem_view_privilege (
	stem_uuid varchar(40) NOT NULL,
	member_uuid varchar(40) NOT NULL,
	object_type bpchar(1) NOT NULL
)
CREATE INDEX grouper_stem_view_priv_mem_idx ON penngrouper.grouper_stem_view_privilege USING btree (member_uuid, object_type);
CREATE INDEX grouper_stem_view_priv_stem_idx ON penngrouper.grouper_stem_view_privilege USING btree (stem_uuid, object_type);

```

Populate table for user and GrouperAll

```
delete from grouper_stem_view_privilege where member_uuid in (select gm.id from grouper_members gm where gm.subject_source = 'pennperson' and gm.subject_id in ('10754302', 'GrouperAll'));

insert into grouper_stem_view_privilege (stem_uuid, member_uuid, object_type) (
select distinct gg.parent_stem as stem_id, gm.id as member_id, 'G' as object_type
from grouper_memberships_all_v gmav,
    grouper_members gm,
    grouper_groups gg,
    grouper_fields gfl
where gmav.owner_group_id = gg.id AND gmav.field_id = gfl.id AND gmav.member_id = gm.id AND gmav.immediate_mship_enabled = 'T'
and gfl.name in ('readers', 'viewers', 'admins', 'optins', 'optouts', 'updaters', 'groupAttrReaders', 'groupAttrUpdaters')
and gm.subject_id in ('10754302', 'GrouperAll'));

insert into grouper_stem_view_privilege (stem_uuid, member_uuid, object_type) (
select distinct gs.parent_stem as stem_id, gm.id as member_id, 'S' as object_type
from grouper_memberships_all_v gmav,
    grouper_members gm,
    grouper_stems gs,
    grouper_fields gfl
where gmav.owner_stem_id = gs.id AND gmav.field_id = gfl.id AND gmav.member_id = gm.id AND gmav.immediate_mship_enabled = 'T'
and gfl.name in ('creators', 'stemAdmins', 'stemAttrReaders', 'stemAttrUpdaters')
and gm.subject_id in ('10754302', 'GrouperAll'));

insert into grouper_stem_view_privilege (stem_uuid, member_uuid, object_type) (
select distinct ga.stem_id as stem_id, gm.id as member_id, 'A' as object_type
from grouper_memberships_all_v gmav,
    grouper_members gm,
    grouper_attribute_def ga,
    grouper_fields gfl
where gmav.owner_attr_def_id = ga.id AND gmav.field_id = gfl.id AND gmav.member_id = gm.id AND gmav.immediate_mship_enabled = 'T'
and gfl.name in ('attrOptins', 'attrAdmins', 'attrViewers', 'attrOptouts', 'attrUpdaters', 'attrReaders', 'attrDefAttrRead', 'attrDefAttrUpdate')
and gm.subject_id in ('10754302', 'GrouperAll'));
commit;
```

See what folders are available for user

```
select distinct gs.name from grouper_stems gs, grouper_stem_set_v gssv, grouper_stem_view_privilege gsvp, grouper_members gm 
where gs.id = gssv.then_has_stem_id and gssv.if_has_stem_id = gsvp.stem_uuid and gm.id = gsvp.member_uuid;
and gm.subject_id in ('10754302', 'GrouperAll');
```

View for group privileges

```
create view grouper_stem_view_priv_g_v as
select distinct gg.parent_stem as stem_id, gm.id as member_id, 'G' as object_type
from grouper_memberships ms,
    grouper_group_set gs,
    grouper_members gm,
    grouper_groups gg,
    grouper_fields gfl
where ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id
and gs.owner_group_id = gg.id AND gs.field_id = gfl.id AND ms.member_id = gm.id AND ms.enabled = 'T'
and gfl.type = 'access';
```

View for own stem privileges, they will propagate up

```
create view grouper_stem_view_priv_s_v as
select distinct gst.id as stem_id, gm.id as member_id, 'S' as object_type
from grouper_memberships ms,
    grouper_group_set gs,
    grouper_members gm,
    grouper_stems gst,
    grouper_fields gfl
where ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id
and gs.owner_stem_id = gst.id AND gs.field_id = gfl.id AND ms.member_id = gm.id AND ms.enabled = 'T'
and gfl.type = 'naming';

```

View for attribute privileges

```
create view grouper_stem_view_priv_a_v as
select distinct gad.stem_id as stem_id, gm.id as member_id, 'G' as object_type
from grouper_memberships ms,
    grouper_group_set gs,
    grouper_members gm,
    grouper_attribute_def gad, 
    grouper_fields gfl
where ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id
and gs.owner_attr_def_id = gad.id AND gs.field_id = gfl.id AND ms.member_id = gm.id AND ms.enabled = 'T'
and gfl.type = 'attributeDef';
```

POC of updating privileges for a user

```
delete from grouper_stem_view_privilege where member_uuid = 'c5c8ef55-76be-4b0d-9910-9efbf465cff3';
insert into grouper_stem_view_privilege (stem_uuid, member_uuid, object_type)  (
select gsv.stem_id, gsv.member_id, gsv.object_type from grouper_stem_view_priv_g_v gsv
where gsv.member_id = 'c5c8ef55-76be-4b0d-9910-9efbf465cff3' union all
select gsv.stem_id, gsv.member_id, gsv.object_type from grouper_stem_view_priv_s_v gsv
where gsv.member_id = 'c5c8ef55-76be-4b0d-9910-9efbf465cff3');
commit;
```

```
insert into grouper_stem_view_privilege (stem_uuid, member_uuid, object_type, last_updated)  (
select gsv.stem_id, gsv.member_id, gsv.object_type, 123456789 as last_updated from grouper_stem_view_priv_g_v gsv
where gsv.member_id = 'c5c8ef55-76be-4b0d-9910-9efbf465cff3' union all
select gsv.stem_id, gsv.member_id, gsv.object_type, 123456789 as last_updated from grouper_stem_view_priv_s_v gsv
where gsv.member_id = 'c5c8ef55-76be-4b0d-9910-9efbf465cff3');
commit;
delete from grouper_stem_view_privilege where member_uuid = 'c5c8ef55-76be-4b0d-9910-9efbf465cff3' and last_updated < 123456789;
commit;

```

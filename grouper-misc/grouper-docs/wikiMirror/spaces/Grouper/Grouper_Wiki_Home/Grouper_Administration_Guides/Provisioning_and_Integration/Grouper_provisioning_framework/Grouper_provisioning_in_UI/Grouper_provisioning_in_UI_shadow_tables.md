---
title: "Grouper provisioning in UI shadow tables"
space: Grouper
pageId: 28555085
version: 4
lastUpdated: 2020-02-02T07:33:06.024Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555085/Grouper+provisioning+in+UI+shadow+tables
---

There are [tables to keep track of provisioning state](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554422/Grouper+provisioning+daemon+tables)

The identifier in the provisioning tables is the type of provisioner and the name. e.g. for SQL sync this could be

Provisioner type: sqlTableSync

Provisioner name: someMembershipSync

Create a view that has these memberships (note, change the provisioner name, subject source)

Simple view of memberships from a query like this

```
select distinct gmlv.group_id, gmlv.group_name, gmlv.subject_id, gmlv.member_id
  FROM grouper_memberships_lw_v gmlv, grouper_sync gsync, grouper_sync_group gsg
  WHERE gmlv.SUBJECT_SOURCE = 'jdbc' and gmlv.list_name = 'members'
       and gsync.sync_engine = 'SQL_SYNC_ENGINE' and gsync.provisioner_name = 'someLdap'
       and gsync.id = gsg.grouper_sync_id and gsg.group_id = gmlv.group_id 
       and gsg.provisionable = 'T'
```

This query option is more complex but might perform better especially on mysql or database where views of views are not ideal

```
select distinct gg.id as group_id, gg.name as group_name, gm.subject_id, gm.id as member_id
  FROM grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_fields gfl, grouper_members gm,
       grouper_sync gsync, grouper_sync_group gsg
  WHERE ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id and ms.enabled = 'T'
       and gs.OWNER_GROUP_ID = gg.id AND gs.FIELD_ID = gfl.ID and GM.SUBJECT_SOURCE = 'jdbc'
       and gfl.name = 'members'
       and gsync.sync_engine = 'SQL_SYNC_ENGINE' and gsync.provisioner_name = 'someLdap'
       and gsync.id = gsg.grouper_sync_id and gsg.group_id = gg.id 
       and gsg.provisionable = 'T'
```

Simple view of memberships and privileges (if you need privileges too)

```
select distinct gmlv.group_id, gmlv.group_name, gmlv.subject_id, gmlv.member_id, gmlv.list_name
  FROM grouper_memberships_lw_v gmlv, grouper_sync gsync, grouper_sync_group gsg
  WHERE gmlv.SUBJECT_SOURCE = 'jdbc' 
       and gsync.sync_engine = 'SQL_SYNC_ENGINE' and gsync.provisioner_name = 'someLdap'
       and gsync.id = gsg.grouper_sync_id and gsg.group_id = gmlv.group_id 
       and gsg.provisionable = 'T'
```

Simple view of memberships / privileges

```
select distinct gg.id as group_id, gg.name as group_name, gm.subject_id, gm.id as member_id
  FROM grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_fields gfl, grouper_members gm,
       grouper_sync gsync, grouper_sync_group gsg
  WHERE ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id and ms.enabled = 'T'
       and gs.OWNER_GROUP_ID = gg.id AND gs.FIELD_ID = gfl.ID and GM.SUBJECT_SOURCE = 'jdbc'
       and gfl.name = 'members'
       and gsync.sync_engine = 'SQL_SYNC_ENGINE' and gsync.provisioner_name = 'someLdap'
       and gsync.id = gsg.grouper_sync_id and gsg.group_id = gg.id 
       and gsg.provisionable = 'T'
```

Complex view of memberships / privileges might perform better especially in mysql

```

```

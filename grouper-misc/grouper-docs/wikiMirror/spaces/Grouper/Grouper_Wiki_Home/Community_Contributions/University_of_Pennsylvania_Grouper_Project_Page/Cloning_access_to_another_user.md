---
title: "Cloning access to another user"
space: Grouper
pageId: 28544308
version: 4
lastUpdated: 2026-07-01T05:48:38.990Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544308/Cloning+access+to+another+user
---

There are requests for new employees to get access like another user.

This is a little complicated since some groups are indirect, some are loaded, and some analysis is needed to make this happen.

We need a UI screen to do this work for us at some point.

Here are some queries to help

## Find memberships one user has where the other user doesnt, exclude indirect and loaded groups

User to copy: 18141566

User to get new memberships: 16562702

```
select
  gg.name
from
  grouper_memberships gms,
  grouper_members gm,
  grouper_fields gf,
  grouper_groups gg
where
  gms.field_id = gf.id
  and gf.name = 'members'
  and gms.member_id = gm.id
  and gg.id = gms.owner_group_id
  -- not a loader group
  and not exists (
  select
    1
  from
    grouper_attr_asn_group_v gaagv
  where
    gaagv.group_id = gg.id
    and attribute_def_name_name like '%etc:attribute:loaderMetadata:loaderMetadata')
  -- not composite
  and gms.mship_type = 'immediate'
  and gm.subject_id = '18141566'
  -- isnt already assigned
  and not exists (
  select
    1
  from
    grouper_memberships gms2,
    grouper_members gm2,
    grouper_fields gf2,
    grouper_groups gg2
  where
    gms2.field_id = gf2.id
    and gf2.name = 'members'
    and gms2.member_id = gm2.id
    and gg2.id = gms2.owner_group_id
    and gms2.mship_type = 'immediate'
    and gms2.owner_group_id = gms.owner_group_id
    and gm2.subject_id = '16562702' 
)
order by
  1;
```

Review the groups and make some assignments

## Group privileges that one user has where the other doesnt

```
select
  gg.name, gf.name 
from
  grouper_memberships gms,
  grouper_members gm,
  grouper_fields gf,
  grouper_groups gg
where
  gms.field_id = gf.id
  and gf.type = 'access'
  and gms.member_id = gm.id
  and gg.id = gms.owner_group_id
  and gms.mship_type = 'immediate'
  and gm.subject_id = '18141566'
  and not exists (
  select
    1
  from
    grouper_memberships gms2,
    grouper_members gm2,
    grouper_fields gf2,
    grouper_groups gg2
  where
    gms2.field_id = gf2.id
    and gf2.id = gf.id
    and gms2.member_id = gm2.id
    and gg2.id = gms2.owner_group_id
    and gms2.mship_type = 'immediate'
    and gms2.owner_group_id = gms.owner_group_id
    and gm2.subject_id = '16562702' 
)
order by
  1;
```

## Stem privileges

```
select
  gs.name, gf.name 
from
  grouper_memberships gms,
  grouper_members gm,
  grouper_fields gf,
  grouper_stems gs
where
  gms.field_id = gf.id
  and gf.type = 'naming'
  and gms.member_id = gm.id
  and gs.id = gms.owner_stem_id
  and gms.mship_type = 'immediate'
  and gm.subject_id = '18141566'
  and not exists (
  select
    1
  from
    grouper_memberships gms2,
    grouper_members gm2,
    grouper_fields gf2,
    grouper_stems gs2
  where
    gms2.field_id = gf2.id
    and gf2.id = gf.id
    and gms2.member_id = gm2.id
    and gs2.id = gms2.owner_stem_id
    and gms2.mship_type = 'immediate'
    and gms2.owner_stem_id = gms.owner_stem_id
    and gm2.subject_id = '16562702' 
)
order by
  1;
```

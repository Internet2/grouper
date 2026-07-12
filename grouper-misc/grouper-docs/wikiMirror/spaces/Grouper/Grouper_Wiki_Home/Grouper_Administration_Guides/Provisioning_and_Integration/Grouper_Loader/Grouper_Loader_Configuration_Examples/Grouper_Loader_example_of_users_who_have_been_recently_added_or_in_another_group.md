---
title: "Grouper Loader example of users who have been recently added or in another group"
space: Grouper
pageId: 28555121
version: 4
lastUpdated: 2026-07-12T05:05:55.452Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555121/Grouper+Loader+example+of+users+who+have+been+recently+added+or+in+another+group
---

The loader query on the "Can keep claimed license" group contains

- people in "Have claimed a license"
- who have claimed their license in the last 60 days
- or who has recently logged in to the SP

```
select
  subject_id,
  subject_source
from
  grouper_memberships_lw_v gmlv
where
  gmlv.list_name = 'members'
  /* people in our source */
  and gmlv.subject_source = 'myInstitutionSubjectSourceId'
  /* they are in the claimed group */
  and gmlv.group_name = 'penn:isc:ait:apps:atlassian:helperGroups:jiraHaveSelfClaimedLicense'
  and (
  /* either they have logged in recently */
  exists (
  select
    1
  from
    grouper_memberships_lw_v gmlv2
  where
    gmlv2.member_id = gmlv.member_id
    and gmlv2.list_name = 'members'
    and gmlv2.group_name = 'penn:isc:ait:apps:atlassian:helperGroups:recentAtlassianCloudUsers')
  or 
  /* or they claimed their license in the last 60 days */
  exists (
  select
    1
  from
    grouper_pit_memberships_lw_v gpmlv,
    grouper_pit_groups gpg,
    grouper_pit_fields gpf,
    grouper_pit_members gpm
  where
    gpmlv.owner_group_id = gpg.id
    and gpg.name = 'penn:isc:ait:apps:atlassian:helperGroups:jiraHaveSelfClaimedLicense'
    and gpmlv.field_id = gpf.id
    and gpf.name = 'members'
    and gpmlv.member_id = gpm.id
    and gpm.source_id = gmlv.member_id
    and gpmlv.membership_active = 'T'
    and to_timestamp((1692715884578000 / 1000000) + (60 * 24 * 60 * 60)) > now()))
```

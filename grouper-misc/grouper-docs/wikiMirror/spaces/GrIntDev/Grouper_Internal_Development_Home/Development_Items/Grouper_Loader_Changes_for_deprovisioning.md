---
title: "Grouper Loader - Changes for deprovisioning"
space: GrIntDev
pageId: 48792854
version: 18
lastUpdated: 2026-07-12T06:45:48.033Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792854/Grouper+Loader+-+Changes+for+deprovisioning
---

## 

## Attributes

When grouper loader runs to add/delete members from groups, it will populate the following attributes.

- grouperLoaderMetadata - marker attribute assigned to groups only. This is single assigned, no value. The following attributes are assigned to the assignment of this attribute to a group. The are single valued string attributes.
  
  - grouperLoaderMetadataLoaded (required)- set to true means this group is loaded from the loader
  - grouperLoaderMetadataGroupId (required) - group id of the loader job which is controlling the memberships of this group.
  - grouperLoaderMetadataLastFullMillisSince1970 (required) - millis since 1970 that this group was fully processed (run in thread and dont do PIT and change log)
  - grouperLoaderMetadataLastIncrementalMillisSince1970 (optional) - millis since 1970 that this group was incrementally processed (run in thread and dont do PIT and change log)
  - grouperLoaderMetadataLastSummary (optional) - total: 105, inserted: 6, deleted: 7, updated: 2 (run in thread and dont do PIT and change log)
- If a group is processed by loader, it will set these attributes
- If a group is assigned to be processed by a loader job, but was not included, the grouperLoaderMetadataLoaded will be set to "false" without touching the other attributes

## Show on UI

Show all the groups in the loader tab that logged in user has View privilege on and have grouperLoaderMetadata attributes. Group names will be links. Also show summary info and last loaded time (full/incremental)

A group itself should list that it is a loader job for a particular group that configures it. On the loader main readonly tab, it should say if it is or was loaded from a loader job, and which group controlled it. When the last full and incremental processing occurred. And the summary info.

## Do not load (TODO)

Spec to be provided later

If a membership is in the deprovisioning lockout, and this group and loader job group is marked to deprovision users, then dont load it.

## Delete groups not loaded

Have a grouper-loader.base.properties setting that defaults to true (in future, currently false)

```
#potentially delete groups that are no longer in the source system
loader.deleteGroupsNoLongerInSource = false
```

If there is a "like" string setting then dont do this

Look in the "like" string logic, and do that for groups managed by a list of groups job (SQL_GROUP_LIST, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES)

In Grouper 2.4 will use this metadata to remove groups which are removed from the source system. Currently this is done with SQL like string.

Document how to find groups with no members (SQL). When group members go to zero, log as "warn" that it happened.

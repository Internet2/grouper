---
title: "Circular Memberships"
space: Grouper
pageId: 206635009
version: 2
lastUpdated: 2026-09-28T07:39:09.941Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/206635009/Circular+Memberships
---

## Summary

A group cannot be a member of itself, either directly or through other groups. As of Grouper v7.6.0 ([GRP-7291](https://grouper.atlassian.net/browse/GRP-7291)), Grouper blocks any membership that would create a loop, at any depth.

For example, if group A is a member of group B, you cannot add group B as a member of group A. If you try, Grouper shows an error and the member is not added. The error is: *The group can't be added because it would create a circular membership.*

## What is blocked

Here "A → B" means group B is a member of group A:

- A → A: a group as a member of itself (this was already blocked)
- A → B → A
- A → B → C → A, and any longer loop

The check applies to every way a membership can be added: the UI, web services, GSH, loaders, imports, MCP, and the Java API. It cannot be turned off, and nobody can bypass it, including GrouperSystem.

## Why

There are very few real uses for circular memberships. Grouper can still work out who is in a loop, but deep or wide loops cause serious performance problems, where operations run long or time out. Loops are also hard to find and undo in the UI. Sites that copy nested LDAP or Active Directory groups into Grouper can create loops without realizing it.

## Existing circular memberships

Removing a membership is never blocked. Loops that already exist keep working as before. To break a loop, remove any one membership in it. Once a loop is broken, it cannot be re-created.

### Finding existing circular memberships

Before upgrading, check whether your Grouper already has circular memberships. Run this query against the Grouper database. It is read-only and works on PostgreSQL, Oracle, and MySQL. On a large registry it can take a while, so run it off-peak.

Each row is one membership that is part of a loop. For a loop A → B → C → A it returns three rows. Remove any one membership in each loop to break it. If an automated job creates these memberships, fix the source data (for example the nested LDAP groups) before upgrading, since after the upgrade the job's attempts to add them are rejected.

```sql
select distinct
       og.name as owner_group_name,
       mg.name as member_group_name,
       gs.owner_group_id,
       gs.member_group_id
from grouper_group_set gs
join grouper_fields f
  on f.id = gs.field_id and f.name = 'members' and f.type = 'list'
join grouper_group_set gs_back
  on gs_back.owner_group_id = gs.member_group_id
 and gs_back.member_group_id = gs.owner_group_id
 and gs_back.field_id = gs.field_id
join grouper_groups og on og.id = gs.owner_group_id
join grouper_groups mg on mg.id = gs.member_group_id
where gs.depth = 1
order by og.name, mg.name
```

## Start dates and disabled groups

A membership with a future start date, or one that comes back when a disabled group is re-enabled, is checked when the enabled/disabled daemon activates it. If activating it would create a loop, the membership stays inactive. The daemon (`OTHER_JOB_enabledDisabled`) then ends in a warning status that names the groups involved. It warns on every run until that membership is removed or the loop is broken another way.

See [Grouper enabled and disabled dates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544574/Grouper+enabled+and+disabled+dates) for how start and end dates work.

## Missing group sets for an existing loop

The `OTHER_JOB_syncAllSetTables` daemon adds group set rows that are missing, e.g. after a failed or interrupted change. If a membership in an existing loop is missing its group set rows, adding them would create the loop, so the daemon skips those memberships, finishes everything else, and then ends in error with a message like:

```
2 immediate groupSets were not added since they would create a circular membership, remove one of the memberships in each loop:
Not adding groupSet for ownerGroupId = <id>, memberGroupId = <id>: The group can't be added because it would create a circular membership.
...
```

It fails this way on every run until the loop is broken. Remove one membership in each loop listed, using the query above to turn the group ids into names. Loops whose group set rows are all present do not cause this.

## Scope

This covers groups added as members of other groups. Composites, ABAC/JEXL scripted groups, and rules/loader-driven cycles have their own considerations and will be handled separately.

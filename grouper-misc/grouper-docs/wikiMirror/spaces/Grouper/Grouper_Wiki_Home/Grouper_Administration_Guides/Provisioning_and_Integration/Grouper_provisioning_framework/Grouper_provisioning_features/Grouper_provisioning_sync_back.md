---
title: "Grouper provisioning sync back"
space: Grouper
pageId: 28555407
version: 4
lastUpdated: 2026-07-07T16:40:25.727Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555407/Grouper+provisioning+sync+back
---

*Available in Grouper 7.1.0+.*

This guide explains what the provisioner *sync-back* feature does, how to turn it on per provisioner, and the limitations operators should be aware of.

For running a full sync off this cache instead of reading the target each run, see [Full sync from the sync-back cache](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/40271874/Full+sync+from+the+sync-back+cache).

## What sync-back is

Every Grouper provisioner pushes Grouper state to a target system (Azure, Okta, LDAP, SCIM, Duo, etc.). Without sync-back, Grouper only tracks the bare minimum needed to make the next provisioning pass work (the `grouper_sync_*` tables: which groups, members, and memberships are in the target, plus a few link ids).

With sync-back enabled, Grouper additionally records a **snapshot of each native target object** (group, user, membership) into a set of generic reporting tables. Those snapshots are populated whenever the provisioner reads the target — they are not a separate query and do not trigger any extra API calls.

The intended uses are:

- Operator visibility — "what did the target look like the last time we provisioned it?" without having to call the vendor API.
- Cross-protocol reporting — every provisioner writes the same schema, so reports and audits can be written once and apply to all of them.
- Diagnostics — when something looks wrong, the snapshot rows let you see what attribute values Grouper actually saw on the target.

## The reporting tables

| Table | What it holds |
| --- | --- |
| `grouper_prov_group` | One row per target group seen during the last read pass. Keyed by provisioner + target group id. |
| `grouper_prov_group_attr` | Per-provisioner catalog of group attribute names that have ever been captured. Stable ids. |
| `grouper_prov_group_attr_value` | The per-group attribute values from the latest read. |
| `grouper_prov_user` | One row per target user/entity seen during the last read pass. |
| `grouper_prov_user_attr` | Per-provisioner catalog of user attribute names. |
| `grouper_prov_user_attr_value` | The per-user attribute values. |
| `grouper_prov_mship` | One row per membership (target group id + target user id) seen during the last read pass. |
| `grouper_prov_mship_role` | Catalog of membership role names where the target supports roles (e.g. SCIM "owner"/"member"). |

All rows are scoped to a single provisioner via `grouper_sync_internal_id`. Two provisioners against the same target system will each have their own independent set of rows.

## Reporting views (the easy way to query)

Three pre-joined views ship with the schema and are the recommended way for operators to read the sync-back data. They wrap the raw tables together with the matching `grouper_sync_*` provisioning state and the relevant core Grouper rows (`grouper_members`, `grouper_groups`), so you rarely need to write the joins yourself.

| View | What it gives you |
| --- | --- |
| `grouper_prov_user_attr_v` | Provisioner users fanned out one row per (user, attribute, value), joined with their grouper_member and grouper_sync_member rows. Users that have no captured attributes still appear as one row with null attribute columns. Use this for "what does the target look like for this user?" queries. |
| `grouper_prov_group_attr_v` | Same idea on the group axis: one row per (group, attribute, value), joined with grouper_groups and grouper_sync_group. Groups with no captured attributes show as one row with nulls. |
| `grouper_prov_mship_v` | Memberships joined with both sides — the provisioner user, the provisioner group, and their corresponding grouper_members / grouper_groups / grouper_sync_membership rows. Use this for membership audits and "is this user in this group on the target?" questions. |

All three views include the provisioner name (from `grouper_sync`), the sync engine, the Grouper-side subject id / group name, and the captured target attribute value. They are the right starting point for most reports — prefer them over the raw tables unless you specifically need something the views don't expose.

## How to enable it

The normal path is the provisioning configuration wizard in the UI — edit the provisioner and toggle:

- **Load entities into generic grouper table**
- **Load groups into generic grouper table**
- **Load memberships into generic grouper table**

Each axis (entities, groups, memberships) can be turned on independently. Under the hood these set three config keys:

provisioner.*configId*.loadEntitiesToGenericGrouperTable = true provisioner.*configId*.loadGroupsToGenericGrouperTable = true provisioner.*configId*.loadMembershipsToGenericGrouperTable = trueAll three default to `false`. Turning one on has no effect on the others, and turning any of them on does not change what the provisioner sends to the target — only what gets recorded back into the reporting tables.

### Choosing which attributes to capture

Two more wizard fields control which target-side attributes get recorded into `grouper_prov_*_attr_value`:

- **Native attributes (entities)** — sets `provisioner.*configId*.nativeAttributesEntities`
- **Native attributes (groups)** — sets `provisioner.*configId*.nativeAttributesGroups`

If left blank, the protocol's curated defaults are used (see "Attribute coverage" below). Either field accepts two forms:

*Comma-separated list* — for flat-attribute targets like LDAP, just list the attribute names:

sn, mail, telephoneNumber*JSON array* — for nested payloads (SCIM, Duo, etc.) or when you need to declare the value type explicitly. Each entry has a required `name` (what gets stored in `grouper_prov_*_attr.attribute_name`), an optional `path` (a JSON Pointer for SCIM-style protocols, or the bare attribute name for LDAP-style; defaults to `"/" + name` or just `name`), and an optional `type` (`string` | `integer` | `boolean` | `timestamp`; auto-detected from the runtime value if omitted):

[ { "name": "active" }, { "name": "displayName" }, { "name": "lastModified", "path": "/meta/lastModified", "type": "timestamp" } ]The `path` is what lets you pull a value out of a nested SCIM payload (e.g. `/meta/lastModified`, `/emails/0/value`) without writing protocol-specific code — the framework walks the JSON for you and records whatever it finds at that pointer.  
**Caveat:** the underlying DAO for the protocol must declare it supports sync-back (`canSyncBack = true`). All built-in protocols in this release support it; a custom provisioner you wrote yourself does not unless you've explicitly wired it up.

## How it works at a high level

1. The provisioner runs its normal full or incremental pass. During its read phase it asks the target system for groups, users, and (sometimes) memberships.
2. As each native object comes back from a read, the framework captures a snapshot of it in memory.
3. Each write the provisioner performs (insert, update, delete) is captured too. For endpoints that echo the object, the returned body is snapshotted directly; for endpoints that return no body (SCIM PATCH 204, LDAP modify) the framework issues a follow-up read of that object so the change is still captured.
4. At the end of the run, the framework flushes those snapshots into the `grouper_prov_*` tables — inserting new rows, updating changed ones, and deleting rows for objects no longer present.

As of 7.3.1, the reporting tables converge within the same run: both reads and writes are captured, on full and incremental passes alike, so an insert, update, or delete Grouper performs is reflected in `grouper_prov_*` at the end of that same run. (Before 7.3.1, writes did not update the reporting tables directly and only caught up on the next read pass.)

## What protocols support it

The following provisioner protocols have sync-back wired up:

- Adobe
- Azure
- Box
- Datadog
- Duo
- Freshservice Requester
- Google
- LDAP
- Okta
- Remedy
- SCIM (incl. AWS variant)
- TeamDynamix
- TrueFoundry

SQL provisioners are intentionally excluded — the target is already a database, so a sync-back snapshot would be redundant. Custom or third-party provisioners need explicit DAO opt-in to participate.

## Limitations and behaviors to be aware of

### Convergence within the run (7.3.1+)

As of 7.3.1 the reporting tables converge at the end of each run: reads populate them and writes (insert, update, delete) are captured as well, on both full and incremental passes and including endpoints that return no body. Before 7.3.1 the tables reflected only the last read, so writes lagged to the next read pass. The tables still represent what the provisioner saw during its run, not a continuously live mirror — for sub-second freshness use the target's own audit log.

### Memberships only when the target returns them

Some protocols return memberships embedded in their group or user payloads (SCIM, Duo). Others require an explicit membership read (LDAP). Operators should not assume `grouper_prov_mship` is populated on every read pass — it depends on whether the provisioner actually asked for memberships that pass.

### selectAll vs. selectByIds reads

When the provisioner is configured with `selectAllEntities=true` and `selectAllGroups=true` (the typical case), every native object visible to the read scan gets captured. When those flags are `false`, only the specific ids looked up that pass are captured — as of 7.3.1 this scoped capture runs on both full and incremental passes — so the reporting tables may still be a subset of what's actually in the target.

### Attribute coverage

Each protocol has a curated default list of attributes it captures (typically the schema's core identity fields — id, name, login, status booleans, etc.). Defaults intentionally exclude human-name fields and free-text descriptions; override the per-provisioner attribute list if you need them.

## Verifying it works

After enabling the load flags, run a full sync and check:

select count(*) from grouper_prov_user where grouper_sync_internal_id = (select internal_id from grouper_sync where provisioner_name = '*configId*');As of 7.3.1 the first full sync after enabling the flags both provisions the target and captures the resulting objects, so the reporting tables populate on that same run. (Before 7.3.1 the first sync's read happened before its writes, so rows only appeared on the second sync.)

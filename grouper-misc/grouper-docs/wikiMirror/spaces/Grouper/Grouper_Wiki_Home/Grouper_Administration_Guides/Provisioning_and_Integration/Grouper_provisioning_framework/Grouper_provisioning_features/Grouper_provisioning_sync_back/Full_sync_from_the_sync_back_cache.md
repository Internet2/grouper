---
title: "Full sync from the sync-back cache"
space: Grouper
pageId: 40271874
version: 1
lastUpdated: 2026-07-07T16:39:41.934Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/40271874/Full+sync+from+the+sync-back+cache
---

*Available in Grouper 7.3.1+.*

This page covers the *read* side of provisioner sync-back: running a full sync off the `grouper_prov_*` sync-back cache instead of pulling the target's current state from its API each run. For what sync-back is and how the cache is populated, see the parent page, [Grouper provisioning sync back](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555407/Grouper+provisioning+sync+back).

## What it does

During a full sync, Grouper normally retrieves the target's current users, groups, and memberships from the target API, compares that against Grouper's desired state, and applies the difference. On a large or expensive target that bulk retrieval dominates the run — most acutely on group-centric targets (for example Okta), where reading memberships means iterating every group. The *full sync from sync-back cache* options let Grouper resolve that "current target state" from the `grouper_prov_*` reporting tables instead, skipping the bulk target read.

## How the cache stays current, and what still talks to the target

Reading from the cache does not mean Grouper stops talking to the target. The `grouper_prov_*` tables are still populated the way sync-back always populates them — from real conversations with the target API — and this option only removes the one expensive step of pulling the *entire* current target state at the start of each full sync. Three things keep the cache current: (1) every user, group, or membership change Grouper decides to make is still a real create/update/delete call to the target, and as of 7.3.1 the result is captured straight back into the cache in the same run; (2) objects the cache does not know about (a brand-new user) or that are in an error state are still read individually from the target that run; and (3) the cache must be seeded, and kept honest against out-of-band drift, by ordinary read passes — so you still run conventional full syncs that read from the target to (re)populate it.

So the compare treats the cache as "what the target looked like as of the last real read, plus everything Grouper has written since," applies Grouper's desired state on top — adding memberships that exist in Grouper but not the cache, removing stale ones — and every one of those adds and removes is itself a real target API call that refreshes the cache. The target conversation shifts from "list everything, every run" to "write the deltas and read only the gaps" — not to "nothing at all."

## How to configure it

Three independent toggles control it, one per object axis. Each is a per-provisioner config key (prefix `provisioner.*configId*.`) and defaults to `false`:

- `fullSyncUsersFromSyncBack` — resolve target users from `grouper_prov_user`. Requires **Sync entities back into generic grouper tables** (`loadEntitiesToGenericGrouperTable`).
- `fullSyncGroupsFromSyncBack` — resolve target groups from `grouper_prov_group`. Requires **Sync groups back into generic grouper tables** (`loadGroupsToGenericGrouperTable`).
- `fullSyncMembershipsFromSyncBack` — resolve target memberships from `grouper_prov_mship`. Requires **Sync memberships back into generic grouper tables** (`loadMembershipsToGenericGrouperTable`).

Each toggle is ignored unless its matching sync-back load flag is on (so the cache is actually populated) and the provisioner's DAO declares it supports it. In the configuration wizard they appear as **Full sync users / groups / memberships from sync-back cache**.

**Group-centric coupling.** For targets that read memberships by iterating groups (for example Okta), `fullSyncGroupsFromSyncBack` only takes effect together with `fullSyncMembershipsFromSyncBack` — configuration validation blocks groups-from-cache without memberships-from-cache, so the membership retrieval never loses the group list it iterates.

## Recommended rollout

Turn on the `load*ToGenericGrouperTable` flags first and let a couple of real full syncs populate the cache (confirm rows land in the `grouper_prov_*` tables), then enable the `*FromSyncBack` toggles. This is best suited to targets where Grouper is authoritative: because a from-cache run issues no bulk target read, a change made directly on the target outside Grouper is only detected when a real read pass next captures it.

## Caveats

- **Attribute completeness.** For user attribute compares to be accurate when serving users from the cache, the captured native entity attributes must include every attribute this provisioner compares or writes — otherwise the compare runs against an incomplete object. See "Choosing which attributes to capture" on the parent page.
- **New and errored objects still hit the target.** Users missing from the cache or in an error state are re-read individually, so a from-cache run is not strictly zero target reads.

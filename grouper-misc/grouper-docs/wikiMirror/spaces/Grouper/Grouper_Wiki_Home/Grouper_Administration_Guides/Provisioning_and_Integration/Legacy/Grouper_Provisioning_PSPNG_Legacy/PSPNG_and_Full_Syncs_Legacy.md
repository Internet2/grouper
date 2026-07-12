---
title: "PSPNG and Full Syncs (Legacy)"
space: Grouper
pageId: 28555026
version: 2
lastUpdated: 2024-03-18T21:29:11.117Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555026/PSPNG+and+Full+Syncs+Legacy
---

PSPNG's full-sync processing is different than expected because its functionality is shared across some incremental sync'ing as well as, of course, scheduled full-syncs.

First, it might help to define what a Full Sync is within PSPNG:

1. Where a group and all its members are reconciled between Grouper and the target system
2. Where #1 is done for all the groups in Grouper that are enabled for provisioning to the target system
3. (Optionally, if grouperIsAuthoritative=true) Delete groups in the target system that no longer exist in Grouper

Architecturally, a full-sync handler (aka thread) is created for each provisioner, regardless of whether scheduled full-syncs are enabled. These handlers wait on internal (and Grouper Messaging) queues for full-sync commands which might be #1 or #3 above. In other words, there is a mechanism ready to reconcile Grouper with the target system.

**Full-Sync Invocations**

There are several mechanisms that message to a provisioner's reconciliation handler:

1. (Most obvious) If grouper-loader.properties has a full-sync schedule, then all the grouper groups which are enabled for provisioning to a target will be queued for reconciliation at the start of that schedule. This only takes a short while because the reconciliations occur asynchronously
2. Also, if grouperIsAuthoritative=true for a provisioner, then a special "Delete Extra Groups" message will be queued by the full-sync schedule. When the full-sync handler receives that message, it will enumerate the (provisioning-enabled) Grouper groups and the groups in the target system and remove the extra groups in the target system. Note that incremental provisioning will always delete groups in a provisioned destination when the GroupDelete changelog action is processed. This grouperIsAuthoritative property only defines what happens if incremental provisioning fails or if Grouper and the target system otherwise diverge.
3. Incremental Provisioning can command a full sync of a group when incremental provisioning finds conflicting actions within a changelog batch. For example, if a member was added and then removed from a group, PSPNG schedules a full sync of that group to make sure the member is left in the correct state. This could be done by handling the events in precise order, but PSPNG has been optimized to rearrange events into the highest performance order. As it is expected to be rare for a changelog batch to have conflicting changes, the decision was made to use this on-demand full sync to resolve the ambiguity (instead of targeted membership reads or event reordering)
4. A Grouper Messaging queue is created for each provisioner (pspng_full_sync_<provisioner name>) and group names received on this queue are reconciled by the full-sync handler. This mechanism is mostly used by PSPNG's unit tests, but could also be used in production when a target system is incorrect.
5. It is also possible that full-sync will be used to correct non-membership properties of groups, eg, the group's name. If this functionality is implemented in the full-sync process, then incremental provisioning will schedule a full sync for a group when its attributes change.

**Logging**

Unfortunately, it is confusing that the full-sync handlers log the same "Full-Sync" messages regardless of why the reconciliation is occurring. In other words, a reconciliation that is happening because of a incremental-provisioning nuance can be easily confused in the log with actions that come from a scheduled full sync.

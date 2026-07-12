---
title: "Grouper pspng changes in v2.5 initial release"
space: GrIntDev
pageId: 48793092
version: 6
lastUpdated: 2026-07-12T06:46:04.811Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793092/Grouper+pspng+changes+in+v2.5+initial+release
---

In order to fix suspected performance issues with pspng, and in order to not require a lot of configuration changes in the 2.5 upgrade, lets start using the "[Grouper provisioning in UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548587/Grouper+provisioning+in+UI)" attributes.

Note, in addition to using those attributes we would also start using the ["sync" tables](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554422/Grouper+provisioning+daemon+tables) to keep track of syncs and state in target systems

## Steps

| Step | Type | Description | Who / when |
| --- | --- | --- | --- |
| 1 | upgrade task | Configure provisioner (grouper_sync) in grouper.properties. Link to pspng config  Configure sync jobs (grouper_sync_job) in grouper-loader.properties other jobs (e.g. one for incremental, one for full) |  |
| 2 | grouper team  upgrade task | Script to migrate from provision_to and do_not_provision_to to new attributes (e.g. run from installer).   Delete provision_to and do_not_provision_to attributes |  |
| 3 | grouper team  upgrade task | groupSelectionExpression, attributesUsedInGroupSelection, attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName  These need to be removed if they are the default.    Otherwise we need to see what people use, and make sure we incorporate    that in the new grouper provisioning attributes. We should do a quick study of this and    introduce an expression (new feature) for provisioning attributes which will probably work for all existing needs.   Add in feature that could require "policy" group tags in order to be provisionable (maybe identify which tags are needed, not just policy). |  |
| 4 | grouper team | When a full or incremental starts. Get or create the grouper_sync and grouper_sync_job records via Grouper API, register as running or    wait until other jobs are done. Keep the records up to date in thread (via API or example?). Update the grouper_sync_group    attributes after group syncs are accomplished  Dont repeat full syncs anymore if changes are made  Dont pause after a sync is made anymore |  |
| 5 | grouper team | In full and incrementals, call the framework method to sync up the grouper_sync_group table with provisioning attributes  Use the grouper_sync_group table as the indicator as to if a group is provisionable or not. Framework methods can batch select / create / update these  Update the group, member, and membership "in target" attributes as applicable. Batch save with framework methods |  |
| 6 | optional for first release  grouper team | Start to use the data fields in the grouper sync tables. e.g. record counts, log statements, provisioned IDs for groups and users, etc |  |
| 7 | optional for first release  grouper team | We can batch select stuff from the grouper database based on the sync tables so there are fewer queries during syncs.    e.g. for incrementals, can batch select a lot of stuff in grouper that might be needed.   e.g. for full syncs, can batch select a lot of stuff in grouper that might be needed.   not sure if group syncs could benefit |  |

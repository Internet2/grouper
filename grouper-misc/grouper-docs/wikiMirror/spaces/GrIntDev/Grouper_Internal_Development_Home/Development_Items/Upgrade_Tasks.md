---
title: "Upgrade Tasks"
space: GrIntDev
pageId: 48792818
version: 2
lastUpdated: 2026-07-12T06:45:44.207Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792818/Upgrade+Tasks
---

## Upgrade Tasks

There are times when a patch or upgrade require data to be updated in Grouper. For example, there was an API patch in 2.4 that required new group sets to be added for entities.

UpgradeTasksJob is an "otherJob" that runs once an hour by default. It checks to see what version you are currently on (based on an attribute value assignment on etc:attribute:upgradeTasks:upgradeTasksMetadataGroup) and applies updates in UpgradeTasks that you don't currently have.

If you have a new data upgrade that needs to be applied after a patch, a new enum can be added to UpgradeTasks and released with the patch.

TODOs

- Currently a new Grouper install will default to version 0 and therefore apply all upgrades when the daemon runs. Normally, this is ok if the upgrades are a no-op if there's nothing to do. In the future, maybe set the correct version on a new install to avoid unnecessary upgrades.

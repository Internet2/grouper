---
title: "Grouper provisioning full workflow"
space: Grouper
pageId: 28555100
version: 2
lastUpdated: 2026-07-01T05:38:58.070Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555100/Grouper+provisioning+full+workflow
---

## Workflow

Here is the workflow of a provisioner running a full sync

| Step | Name | Description |
| --- | --- | --- |
| 1 | propagate provisioning attributes | make sure that provisionable settings (attributes under the covers) are propagated to all descendent objects in folders |
| 2 | retrieve all data from grouper and target | retrieve at same time all data from grouper, sync objects, and target |
| 3 | create wrapper objects | to hold all related data together |
| 4 | validate objects and filter invalid | look at objects and validate them and filter invalid objects. Manipulate attribute data types |
| 5 | matching ID of target object | identify or translate matching ID of all target objects |
| 6 | retrieve subject link | resolve subjects for subject link for subjects missing data |
| 7 | translate grouper groups/entities data to target format | take the grouper groups, entities and translate to the target format |
| 8 | manipulate grouper groups/entities attributes | based on configs manipulate the defaults, types, etc for grouper target translated group/entity attributes and fields |
| 9 | matching id of grouper groups/entities | calculate the matching id of grouper translated group/entity data |
| 10 | index matching id of grouper groups/entities | take all the matching ids of grouper groups/entities and index those for quick lookups |
| 11 | create missing groups / entities | create missing groups / entities |
| 12 | retrieve target group and entity link | based on data retrieved, update the group and entity link |
| 13 | validate groups / entities | based on validations on fields / attributes make sure objects are valid |
| 14 | translate grouper memberships to target | translate grouper memberships to target after the link data is resolved |
| 15 | manipulate grouper memberships attributes | based on configs manipulate the defaults, types, etc for grouper target translated membership attributes and fields |
| 16 | matching id of grouper memberships | calculate the matching id of grouper translated membership data |
| 17 | index matching ID of memberships | take the matching IDs of the grouper side and the target side and index objects in the data index |
| 18 | update sync objects | process sync objects so the target state is accurately reflected |
| 19 | validate memberships | make sure any validations for memberships are considered |
| 20 | compare target objects | look at the grouper side and the target side and compare and generate the target actions |
| 21 | send changes to target | send inserts updates and deletes to target |
| 22 | process results | store counts and errors |

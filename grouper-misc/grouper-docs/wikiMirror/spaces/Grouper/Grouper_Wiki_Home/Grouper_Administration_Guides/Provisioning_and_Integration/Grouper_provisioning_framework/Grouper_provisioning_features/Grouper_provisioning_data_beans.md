---
title: "Grouper provisioning data beans"
space: Grouper
pageId: 28554278
version: 3
lastUpdated: 2026-07-01T05:40:47.395Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554278/Grouper+provisioning+data+beans
---

This page is mostly for developers but might be useful in understanding how the provisioning framework works and troubleshooting

## Categories of data

Each of these categories of data has "attributes" associated with them, which probably are not the attribute framework

| Category | Description |
| --- | --- |
| Group | Holds group data |
| Entity | Holds subject/entity/person data |
| Membership | Tuple that connects group and entity and perhaps some metadata |

## Representations of data

Each type of data is represented in different forms

| Representation | Description |
| --- | --- |
| Grouper | Data that comes out of Grouper (if it still exists there, or if it is virtual data for a delete) |
| Grouper sync | Sync data is stored in a shadow table for each type of data to cache things from the target, provisioning state, and data that might get deleted from Grouper |
| Grouper target | Grouper data that is translated to the target format so it can be compared to the target data and provisioned |
| Target | Data from the target |
| Target native | Object in a format that is not related to the provisioning framework |
| Wrapper | Bean that represents one record of a data category that has reference to the other various representations of the data |

## Indexes of data

There are indexes for each category of data of the in memory beans

| Grouping | Index | Points to |
| --- | --- | --- |
| Grouper | Group uuid | Group wrapper |
| Member uuid | Entity wrapper |
| Tuple of group and member uuid | Membership wrapper |
| Sync | grouperSyncGroupId | Group wrapper |
| grouperSyncMemberId | Entity wrapper |
| Tuple of grouperSyncGroupId and grouperSyncMemberId | Membership wrapper |
| Matching | Target matching group id | Group wrapper |
| Target matching entity id | Entity wrapper |
| Target matching membership id | Membership wrapper |

## Data beans

These beans are used to archive data state or to be used for provisioning business. If the bean is not authoritative, then it is only for logging and shouldnt be relied on as a source for the data for future provisioning purposes (other than just adding to the index)

In other words, if you want to look at all the target objects in the provisioner at a certain point in time, do not look at GrouperProvisioningDataTarget, instead look at all wrappers in GrouperProvisioningData and look at the target references inside.

| Bean | Authoritative source | Description |
| --- | --- | --- |
| GrouperProvisioningData | Yes | Holds the full unique sets of group wrappers, entity wrappers, and membership wrappers |
| GrouperProvisioningDataIndex | Yes | Indexes the GrouperProvisioningData for grouper, sync, and target matching id's |
| GrouperProvisioningDataChanges | No | Archives the changes being sent from the provisioner to the target |
| GrouperProvisioningDataGrouper | No | Archives the data retrieved from Grouper about the provisioning job |
| GrouperProvisioningDataGrouperTarget | No | Archives translations of Grouper beans into the target format |
| GrouperProvisioningDataIncrementalInput | No | Vehicle for external processes to identify which records need to be incrementally processed and which actions |
| GrouperProvisioningDataSync | No | Archives the the sync data retrieved from Grouper |
| GrouperProvisioningDataTarget | No | Archives the data retrieved from the target |

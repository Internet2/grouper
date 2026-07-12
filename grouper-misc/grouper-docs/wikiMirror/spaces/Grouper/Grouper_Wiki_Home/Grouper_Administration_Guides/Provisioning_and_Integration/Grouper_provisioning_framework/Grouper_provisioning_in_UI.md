---
title: "Grouper provisioning in UI"
space: Grouper
pageId: 28548587
version: 23
lastUpdated: 2026-07-12T15:26:55.851Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548587/Grouper+provisioning+in+UI
---

For Grouper v2.6+.

Provisioning configuration is set up in the Grouper UI with helpful documentation, wizard-like interfaces, descriptive [validations](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554271/Grouper+provisioning+validation), and diagnostic tests.

## Example assigning provisionable

Assign provisioning actions on a folder:

See that groups in the folder will be provisioned

### UI actions

- Edit LDAP configs (grouper loader properties)
- Enable a provisioning target type (grouper loader properties)
- Enable a provisioning target
- Disable/Enable a provisioning target for folder or group
- See grouper loader logs for change log consumers
- Manage change log bookmark for change log listeners
- Trigger a full sync (send message), look at provisioningLastFullMillisSince1970 until complete
- Reporting can do a report of a full sync

### Privileges

- In provisioning configuration you identify readers, assigners, and (soon) admins. Users also need READ on a group to view/assign/admin provisionable

Screenshots

Use the "More actions" button to access Provisioning

List of assigned targets for a folder

Assigning a target to a folder

**Attribute definitions**

| Definition | Assigned To | Purpose | Value | Cardinality |
| --- | --- | --- | --- | --- |
| provisioningDef | folder, group | identify a group type | marker | Multi assign |
| provisioningValueDef | folder assignment, group assignment | name/value pairs | string | Single assign, single valued |

****

**Attribute names**

| Name | Definition | Value |
| --- | --- | --- |
| provisioningMarker | provisioningDef | <none> |
| provisioningTarget | provisioningValueDef | Related to a config in grouper-loader.properties which links this provisioner to entend the class GrouperProvisionerBase |
| provisioningDirectAssign | provisioningValueDef | if this is directly assigned or inherited |
| provisioningOwnerStemId | provisioningValueDef | if this is not a direct assignment, then this is the stem id where it is inherited from |
| provisioningStemScope | provisioningValueDef | If folder provisioning applies to only this folder or this folder and subfolders. one\|sub |
| provisioningDoProvision | provisioningValueDef | If you should provisioning (default to true) |
| provisioningLastFullMillisSince1970 | provisioningValueDef | Millis since 1970 that this was last full provisioned |
| provisioningLastIncrementalMillisSince1970 | provisioningValueDef | Millis since 1970 that this was last incremental provisioned. Even if the incremental did not change the target |
| provisioningLastFullSummary | provisioningValueDef | Summary of last full run |
| provisioningLastIncrementalSummary | provisioningValueDef | Summary of last incremental run |

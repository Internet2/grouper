---
title: "Grouper Duo Provisioning groups and membership"
space: Grouper
pageId: 28560437
version: 7
lastUpdated: 2026-07-01T05:35:34.637Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560437/Grouper+Duo+Provisioning+groups+and+membership
---

> This page is an example of provisioning Grouper groups and their memberships into Duo. The Duo provisioner looks up Duo users, creates the matching groups in Duo, and keeps each group's memberships in sync. In this example the provisioned group is a Duo **bypass** group, and the right to mark a group as provisionable is delegated in Grouper to the team that manages it.
> 
> Available in **v2.5.51+** (the Duo provisioner was introduced in 2021).

> **Privileges:** creating and editing a provisioner configuration in the UI requires a Grouper system administrator (a member of the wheel group / running as root). Marking individual groups as provisionable is delegated separately: the provisioner's `groupAllowedToAssign` setting names the group whose members may assign the "provisionable" marker, and `allowAssignmentsOnlyOnOneStem` restricts that to a single folder.

## Duo group

The group can be seen in the Duo admin console and has a bypass configured.

## Provisionable groups

Any group marked as a policy group in this folder is synced to Duo. The display extension ("name" in the UI) is used as the group name in Duo.

Because the display extension is used as the group name in Duo, the "provisionable" marker is configured to only be assignable on one folder in Grouper.

## Troubleshoot the provisioner

You can see activity for the provisioner.

See errors.

## Duo credential on the Duo side

## Duo credential on the Grouper side

## Configure provisioner

```text
provisioner.duoLoader.addDisabledFullSyncDaemon = true
provisioner.duoLoader.addDisabledIncrementalSyncDaemon = true
provisioner.duoLoader.allowAssignmentsOnlyOnOneStem = true
provisioner.duoLoader.class = edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner
provisioner.duoLoader.customizeGroupCrud = true
provisioner.duoLoader.customizeMembershipCrud = true
provisioner.duoLoader.deleteGroupsIfGrouperDeleted = true
provisioner.duoLoader.deleteMembershipsIfNotExistInGrouper = true
provisioner.duoLoader.duoExternalSystemConfigId = duoAdminProdReadwrite
provisioner.duoLoader.entity2advanced = true
provisioner.duoLoader.entityAttributeValueCache0entityAttribute = id
provisioner.duoLoader.entityAttributeValueCache0has = true
provisioner.duoLoader.entityAttributeValueCache0source = target
provisioner.duoLoader.entityAttributeValueCache0type = entityAttribute
provisioner.duoLoader.entityAttributeValueCacheHas = true
provisioner.duoLoader.entityMatchingAttribute0name = loginId
provisioner.duoLoader.entityMatchingAttributeCount = 1
provisioner.duoLoader.groupAllowedToAssign = penn:isc:ait:apps:twoFactor:twoFactorSecurity:twoFactorOwners
provisioner.duoLoader.groupAttributeValueCache0groupAttribute = id
provisioner.duoLoader.groupAttributeValueCache0has = true
provisioner.duoLoader.groupAttributeValueCache0source = target
provisioner.duoLoader.groupAttributeValueCache0type = groupAttribute
provisioner.duoLoader.groupAttributeValueCacheHas = true
provisioner.duoLoader.groupMatchingAttribute0name = name
provisioner.duoLoader.groupMatchingAttributeCount = 1
provisioner.duoLoader.hasTargetEntityLink = true
provisioner.duoLoader.hasTargetGroupLink = true
provisioner.duoLoader.loadEntitiesToGrouperTable = true
provisioner.duoLoader.logAllObjectsVerbose = true
provisioner.duoLoader.logAllObjectsVerboseToLogFile = false
provisioner.duoLoader.numberOfEntityAttributes = 2
provisioner.duoLoader.numberOfGroupAttributes = 3
provisioner.duoLoader.onlyProvisionPolicyGroups = true
provisioner.duoLoader.operateOnGrouperEntities = true
provisioner.duoLoader.operateOnGrouperGroups = true
provisioner.duoLoader.operateOnGrouperMemberships = true
provisioner.duoLoader.provisioningType = membershipObjects
provisioner.duoLoader.selectAllEntities = true
provisioner.duoLoader.selectAllGroups = false
provisioner.duoLoader.showAdvanced = true
provisioner.duoLoader.showAssigningProvisioning = true
provisioner.duoLoader.startWith = this is start with read only
provisioner.duoLoader.subjectSourcesToProvision = pennperson
provisioner.duoLoader.targetEntityAttribute.0.name = id
provisioner.duoLoader.targetEntityAttribute.1.name = loginId
provisioner.duoLoader.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.duoLoader.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier0
provisioner.duoLoader.targetGroupAttribute.0.name = id
provisioner.duoLoader.targetGroupAttribute.1.name = name
provisioner.duoLoader.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.duoLoader.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = displayExtension
provisioner.duoLoader.targetGroupAttribute.2.name = description
provisioner.duoLoader.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.duoLoader.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = description

```

## Assign provisionable

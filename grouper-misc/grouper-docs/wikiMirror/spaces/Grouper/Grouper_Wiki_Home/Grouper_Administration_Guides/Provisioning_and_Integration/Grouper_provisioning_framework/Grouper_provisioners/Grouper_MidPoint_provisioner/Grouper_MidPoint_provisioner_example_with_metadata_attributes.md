---
title: "Grouper MidPoint provisioner example with metadata attributes"
space: Grouper
pageId: 28560271
version: 5
lastUpdated: 2026-07-01T05:35:57.195Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560271/Grouper+MidPoint+provisioner+example+with+metadata+attributes
---

## Overview

 This example configures a MidPoint provisioner named `mp` that lets users choose, per group, which targets (Shibboleth or Box) the group provisions to, and specify the Shibboleth SP entity IDs for each group. The target list and the entity IDs are carried as metadata attributes and translated into target group attributes.

 > This example was built with Grouper v2.6.18. The MidPoint provisioner is a current feature, available in all currently supported releases (class `edu.internet2.middleware.grouper.app.midpointProvisioning.MidPointProvisioner`, introduced 2022).

 > **Privileges:** editing a provisioner's configuration requires Grouper administrator (sysadmin) access, with the configuration editor in read-write mode. Marking a group or folder as provisionable requires administrative privilege on that object.

 

## Mark as provisionable

 On the group's "Provisioning" screen, set "Target name" to `mp`, choose to provision the object, enter the "Shibboleth SP entity IDs", and select the targets ("shibboleth" and/or "box").

 

 

## Data in database

 The selections above are stored as rows in the provisioner's group-attributes table (`gr_mp_group_attributes`): a `target` row per selected target and a `shibbolethEntityIds` row per entity ID.

 

 

## Configure

 

 

## Exported config

 
```text
provisioner.mp.addDisabledFullSyncDaemon = true
provisioner.mp.addDisabledIncrementalSyncDaemon = true
provisioner.mp.class = edu.internet2.middleware.grouper.app.midpointProvisioning.MidPointProvisioner
provisioner.mp.configureMetadata = true
provisioner.mp.customizeEntityCrud = true
provisioner.mp.customizeGroupCrud = true
provisioner.mp.customizeMembershipCrud = true
provisioner.mp.dbExternalSystemConfigId = grouper
provisioner.mp.deleteEntitiesIfNotExistInGrouper = true
provisioner.mp.deleteGroupsIfNotExistInGrouper = true
provisioner.mp.deleteMembershipsIfNotExistInGrouper = true
provisioner.mp.logAllObjectsVerbose = true
provisioner.mp.makeChangesToEntities = true
provisioner.mp.metadata.0.canChange = true
provisioner.mp.metadata.0.canUpdate = true
provisioner.mp.metadata.0.name = md_shibbolethEntityIds
provisioner.mp.metadata.0.showForFolder = true
provisioner.mp.metadata.0.showForGroup = true
provisioner.mp.midPointDeletedColumnName = deleted
provisioner.mp.midPointLastModifiedColumnName = last_modified
provisioner.mp.midPointLastModifiedColumnType = long
provisioner.mp.midPointListOfTargets = shibboleth, box
provisioner.mp.numberOfGroupAttributes = 1
provisioner.mp.numberOfMetadata = 1
provisioner.mp.operateOnGrouperEntities = true
provisioner.mp.operateOnGrouperGroups = true
provisioner.mp.operateOnGrouperMemberships = true
provisioner.mp.provisioningType = membershipObjects
provisioner.mp.selectAllEntities = true
provisioner.mp.showAdvanced = true
provisioner.mp.startWith = this is start with read only
provisioner.mp.subjectSourcesToProvision = jdbc
provisioner.mp.targetGroupAttribute.0.multiValued = true
provisioner.mp.targetGroupAttribute.0.name = shibbolethEntityIds
provisioner.mp.targetGroupAttribute.0.showAdvancedAttribute = true
provisioner.mp.targetGroupAttribute.0.showAttributeValueSettings = true
provisioner.mp.targetGroupAttribute.0.storageType = separateAttributesTable
provisioner.mp.targetGroupAttribute.0.translateExpression = ${ grouperUtil.splitTrimToSet(grouperProvisioningGroup.retrieveAttributeValueString('md_shibbolethEntityIds'), ',')}
provisioner.mp.targetGroupAttribute.0.translateExpressionType = translationScript

```

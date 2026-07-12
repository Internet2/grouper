---
title: "Grouper SQL provisioner example with folder metadata"
space: Grouper
pageId: 28560361
version: 2
lastUpdated: 2026-07-01T05:35:42.702Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560361/Grouper+SQL+provisioner+example+with+folder+metadata
---

## Description

This provisioner provisioned SQL groups/subjects/memberships, and the group name is translated with folder metadata.

Note, pre v5.21.4 or v4.20.0, you need to configure the metadata to show on folders and groups. With this version or later you can configure solely on folders.

## Tables

(this DDL is postgres but you can easily adjust it for other databases)

```
CREATE TABLE some_provisioning_table (
  group_name varchar(1024) NOT NULL,
  subject_id varchar(100) NOT NULL
);
CREATE TABLE some_provisioning_entity (
	subject_id varchar(100) NOT NULL
);
CREATE TABLE some_provisioning_group (
	group_name varchar(1024) NULL
);
```

## Assign the metadata on some folders and not others

## You can see the metadata propagated to the group

Even though this group is not directly provisionable, it is inherited from the ancestor folder

## See the translated values in the target

Note: the "doe" and "trin" have a subdomain suffix, "artsc" does not (since no metadata assigned there)

## Configuration properties

Note the group name translation:

```
${grouperProvisioningGroup.name + grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_subdomain'),  '')}
```

```
provisioner.some_sql.addDisabledFullSyncDaemon = true
provisioner.some_sql.addDisabledIncrementalSyncDaemon = true
provisioner.some_sql.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
provisioner.some_sql.configureMetadata = true
provisioner.some_sql.customizeEntityCrud = true
provisioner.some_sql.customizeGroupCrud = true
provisioner.some_sql.customizeMembershipCrud = true
provisioner.some_sql.dbExternalSystemConfigId = grouper
provisioner.some_sql.deleteEntitiesIfNotExistInGrouper = true
provisioner.some_sql.deleteGroupsIfNotExistInGrouper = true
provisioner.some_sql.deleteMembershipsIfNotExistInGrouper = true
provisioner.some_sql.deleteMembershipsOnlyInTrackedGroups = false
provisioner.some_sql.entityMatchingAttribute0name = subject_id
provisioner.some_sql.entityMatchingAttributeCount = 1
provisioner.some_sql.groupMatchingAttribute0name = group_name
provisioner.some_sql.groupMatchingAttributeCount = 1
provisioner.some_sql.groupTableIdColumn = group_name
provisioner.some_sql.groupTableName = some_provisioning_group
provisioner.some_sql.logAllObjectsVerbose = true
provisioner.some_sql.makeChangesToEntities = true
provisioner.some_sql.membershipEntityForeignKeyColumn = subject_id
provisioner.some_sql.membershipGroupForeignKeyColumn = group_name
provisioner.some_sql.membershipTableName = some_provisioning_table
provisioner.some_sql.metadata.0.dropdownValues = .doe, .trinity
provisioner.some_sql.metadata.0.formElementType = dropdown
provisioner.some_sql.metadata.0.name = md_subdomain
provisioner.some_sql.metadata.0.showForFolder = true
provisioner.some_sql.numberOfEntityAttributes = 1
provisioner.some_sql.numberOfGroupAttributes = 1
provisioner.some_sql.numberOfMembershipAttributes = 2
provisioner.some_sql.numberOfMetadata = 1
provisioner.some_sql.operateOnGrouperEntities = true
provisioner.some_sql.operateOnGrouperGroups = true
provisioner.some_sql.operateOnGrouperMemberships = true
provisioner.some_sql.provisioningType = membershipObjects
provisioner.some_sql.selectAllEntities = true
provisioner.some_sql.showAdvanced = true
provisioner.some_sql.startWith = this is start with read only
provisioner.some_sql.subjectSourcesToProvision = jdbc
provisioner.some_sql.targetEntityAttribute.0.name = subject_id
provisioner.some_sql.targetEntityAttribute.0.translateExpressionType = grouperProvisioningEntityField
provisioner.some_sql.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField = subjectId
provisioner.some_sql.targetGroupAttribute.0.name = group_name
provisioner.some_sql.targetGroupAttribute.0.translateExpression = \u0024{grouperProvisioningGroup.name + grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_subdomain'),  '')}
provisioner.some_sql.targetGroupAttribute.0.translateExpressionType = translationScript
provisioner.some_sql.targetMembershipAttribute.0.name = group_name
provisioner.some_sql.targetMembershipAttribute.0.translateExpressionType = grouperTargetGroupField
provisioner.some_sql.targetMembershipAttribute.0.translateFromGrouperTargetGroupField = group_name
provisioner.some_sql.targetMembershipAttribute.1.name = subject_id
provisioner.some_sql.targetMembershipAttribute.1.translateExpressionType = grouperTargetEntityField
provisioner.some_sql.targetMembershipAttribute.1.translateFromGrouperTargetEntityField = subject_id
provisioner.some_sql.userPrimaryKey = subject_id
provisioner.some_sql.userTableName = some_provisioning_entity

```

## Configuration UI

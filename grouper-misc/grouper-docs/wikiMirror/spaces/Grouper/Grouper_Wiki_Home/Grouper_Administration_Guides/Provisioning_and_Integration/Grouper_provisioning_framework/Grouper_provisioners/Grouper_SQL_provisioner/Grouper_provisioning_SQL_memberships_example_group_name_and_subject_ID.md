---
title: "Grouper provisioning SQL memberships example, group name and subject ID"
space: Grouper
pageId: 28560242
version: 3
lastUpdated: 2026-07-01T05:35:59.268Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560242/Grouper+provisioning+SQL+memberships+example+group+name+and+subject+ID
---

In this example, we will publish memberships to a SQL table with two columns: group_name and subject_id

[Youtube screen video](https://youtu.be/Gd7MLMNYY84)

Note: in 2.6.5 you need to set a membership group and entity foreign key column, which hopefully in a subsequent version you will not need to do

Note: in 2.6.5 you need to edit the configuration directly after you are done with your wizard config to set each membership column as select/insert/update

## DDL

(mysql in this case but could go to any database)

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

## Config

```
provisioner.some_sql.addDisabledFullSyncDaemon = true
provisioner.some_sql.addDisabledIncrementalSyncDaemon = true
provisioner.some_sql.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
provisioner.some_sql.customizeEntityCrud = true
provisioner.some_sql.customizeGroupCrud = true
provisioner.some_sql.customizeMembershipCrud = true
provisioner.some_sql.dbExternalSystemConfigId = grouper
provisioner.some_sql.deleteEntitiesIfNotExistInGrouper = true
provisioner.some_sql.deleteGroupsIfNotExistInGrouper = true
provisioner.some_sql.deleteMembershipsIfNotExistInGrouper = true
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
provisioner.some_sql.numberOfEntityAttributes = 1
provisioner.some_sql.numberOfGroupAttributes = 1
provisioner.some_sql.numberOfMembershipAttributes = 2
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
provisioner.some_sql.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.some_sql.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = name
provisioner.some_sql.targetMembershipAttribute.0.name = group_name
provisioner.some_sql.targetMembershipAttribute.0.translateExpressionType = grouperTargetGroupField
provisioner.some_sql.targetMembershipAttribute.0.translateFromGrouperTargetGroupField = group_name
provisioner.some_sql.targetMembershipAttribute.1.name = subject_id
provisioner.some_sql.targetMembershipAttribute.1.translateExpressionType = grouperTargetEntityField
provisioner.some_sql.targetMembershipAttribute.1.translateFromGrouperTargetEntityField = subject_id
provisioner.some_sql.userPrimaryKey = subject_id
provisioner.some_sql.userTableName = some_provisioning_entity
```

Note: you need to set groups/folders as provisionable

## Wizard

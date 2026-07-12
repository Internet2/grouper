---
title: "Grouper SQL provisioner with groups, entities, memberships"
space: Grouper
pageId: 28560122
version: 3
lastUpdated: 2026-07-01T05:36:18.770Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560122/Grouper+SQL+provisioner+with+groups+entities+memberships
---

This example uses 4.7.2. If you are provisioning to SQL and you can provision to three tables, that is ideal. If you want the app to read memberhsips, just make a view of the tables for the app to use.

## Group table

Has an entry per provisionable group. Stored the ID (ID index), and name (ID path). The ID is the cascade delete foreign key to other tables

```
CREATE TABLE from_grouper_group (
  id varchar(50) NOT NULL,
  "name" varchar(1024) NOT NULL,
  CONSTRAINT from_grouper_group_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX from_grouper_group_name_idx ON from_grouper_group ("name");
```

## Group attribute table

Holds arbitrary attributes about groups

```
CREATE TABLE from_grouper_group_attr (
  group_id varchar(50) NOT NULL,
  attribute_name varchar(50) NOT NULL,
  attribute_value varchar(1000) NOT NULL,
  CONSTRAINT from_grouper_group_attr_pk PRIMARY KEY (group_id,attribute_name,attribute_value)
);
ALTER TABLE from_grouper_group_attr ADD CONSTRAINT from_grouper_group_attr_fk FOREIGN KEY (group_id) REFERENCES from_grouper_group(id) ON DELETE CASCADE;
CREATE INDEX from_grouper_group_attr_attribute_name_idx ON public.from_grouper_group_attr (attribute_name,attribute_value);
```

## Entity table

```
CREATE TABLE from_grouper_entity (
  id varchar(50) NOT NULL,
  subject_id varchar(100) NOT NULL,
  subject_source_id varchar(20) NOT NULL,
  CONSTRAINT from_grouper_entity_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX from_grouper_entity_subject_id_idx ON from_grouper_entity (subject_id,subject_source_id);  
```

## Membership table

```
CREATE TABLE from_grouper_membership (
  group_id varchar(50) NOT NULL,
  entity_id varchar(50) NOT NULL,
  CONSTRAINT from_grouper_membership_pk PRIMARY KEY (group_id,entity_id),
  CONSTRAINT from_grouper_membership_fk FOREIGN KEY (group_id) REFERENCES from_grouper_group(id),
  CONSTRAINT from_grouper_membership_fk_1 FOREIGN KEY (entity_id) REFERENCES from_grouper_entity(id)
);
```

## Attribute view for attribute framework

```
create view from_grouper_attributes_v as
select gg.name as group_name,
(select gaagv.value_integer from grouper_aval_asn_group_v gaagv
where gaagv.attribute_def_name_name = 'test:gidNumber' and gaagv.group_id = gg.id  ) as gid_number
from grouper_groups gg;
```

## Membership view

```
create view from_grouper_membership_v as
select fgg."name" , fge.subject_id, fge.subject_source_id,
(select fgga.attribute_value from from_grouper_group_attr fgga
where fgga.group_id = fgg.id and fgga.attribute_name = 'gidNumber') as gid_number
from from_grouper_entity fge, from_grouper_group fgg, from_grouper_membership fgm 
where fgg.id = fgm.group_id and fge.id = fgm.entity_id 
```

## Provisioner configuration

```
provisioner.iam_unifieddb_group_memberships.addDisabledFullSyncDaemon = true
provisioner.iam_unifieddb_group_memberships.addDisabledIncrementalSyncDaemon = true
provisioner.iam_unifieddb_group_memberships.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
provisioner.iam_unifieddb_group_memberships.customizeEntityCrud = true
provisioner.iam_unifieddb_group_memberships.customizeGroupCrud = true
provisioner.iam_unifieddb_group_memberships.customizeMembershipCrud = true
provisioner.iam_unifieddb_group_memberships.dbExternalSystemConfigId = grouper
provisioner.iam_unifieddb_group_memberships.deleteEntitiesIfNotExistInGrouper = true
provisioner.iam_unifieddb_group_memberships.deleteGroupsIfNotExistInGrouper = true
provisioner.iam_unifieddb_group_memberships.deleteMembershipsIfNotExistInGrouper = true
provisioner.iam_unifieddb_group_memberships.deleteMembershipsOnlyInTrackedGroups = false
provisioner.iam_unifieddb_group_memberships.entityMatchingAttribute0name = id
provisioner.iam_unifieddb_group_memberships.entityMatchingAttributeCount = 1
provisioner.iam_unifieddb_group_memberships.groupAttributesAttributeNameColumn = attribute_name
provisioner.iam_unifieddb_group_memberships.groupAttributesAttributeValueColumn = attribute_value
provisioner.iam_unifieddb_group_memberships.groupAttributesGroupForeignKeyColumn = group_id
provisioner.iam_unifieddb_group_memberships.groupAttributesTableName = from_grouper_group_attr
provisioner.iam_unifieddb_group_memberships.groupMatchingAttribute0name = id
provisioner.iam_unifieddb_group_memberships.groupMatchingAttributeCount = 1
provisioner.iam_unifieddb_group_memberships.groupResolver.columnNames = gid_number
provisioner.iam_unifieddb_group_memberships.groupResolver.groupMatchingColumn = group_name
provisioner.iam_unifieddb_group_memberships.groupResolver.groupResolverAttributes = true
provisioner.iam_unifieddb_group_memberships.groupResolver.resolveAttributesWithSQL = true
provisioner.iam_unifieddb_group_memberships.groupResolver.sqlConfigId = grouper
provisioner.iam_unifieddb_group_memberships.groupResolver.tableOrViewName = from_grouper_attributes_v
provisioner.iam_unifieddb_group_memberships.groupResolver.tableStructure = columnsAreAttributes
provisioner.iam_unifieddb_group_memberships.groupTableIdColumn = id
provisioner.iam_unifieddb_group_memberships.groupTableName = from_grouper_group
provisioner.iam_unifieddb_group_memberships.logAllObjectsVerbose = true
provisioner.iam_unifieddb_group_memberships.logAllObjectsVerboseToLogFile = false
provisioner.iam_unifieddb_group_memberships.makeChangesToEntities = true
provisioner.iam_unifieddb_group_memberships.membershipEntityForeignKeyColumn = entity_id
provisioner.iam_unifieddb_group_memberships.membershipGroupForeignKeyColumn = group_id
provisioner.iam_unifieddb_group_memberships.membershipTableName = from_grouper_membership
provisioner.iam_unifieddb_group_memberships.numberOfEntityAttributes = 3
provisioner.iam_unifieddb_group_memberships.numberOfGroupAttributes = 3
provisioner.iam_unifieddb_group_memberships.numberOfMembershipAttributes = 2
provisioner.iam_unifieddb_group_memberships.operateOnGrouperEntities = true
provisioner.iam_unifieddb_group_memberships.operateOnGrouperGroups = true
provisioner.iam_unifieddb_group_memberships.operateOnGrouperMemberships = true
provisioner.iam_unifieddb_group_memberships.provisioningType = membershipObjects
provisioner.iam_unifieddb_group_memberships.recalculateAllOperations = true
provisioner.iam_unifieddb_group_memberships.selectAllEntities = true
provisioner.iam_unifieddb_group_memberships.selectEntities = true
provisioner.iam_unifieddb_group_memberships.showAdvanced = true
provisioner.iam_unifieddb_group_memberships.startWith = this is start with read only
provisioner.iam_unifieddb_group_memberships.subjectSourcesToProvision = jdbc,personLdapSource
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.0.name = id
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.0.translateExpressionType = grouperProvisioningEntityField
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField = id
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.1.name = subject_id
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectId
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.2.name = subject_source_id
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.iam_unifieddb_group_memberships.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = subjectSourceId
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.0.name = id
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.0.storageType = groupTableColumn
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = id
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.1.name = name
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.1.storageType = groupTableColumn
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = name
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.2.name = gidNumber
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.2.storageType = separateAttributesTable
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.2.translateExpression = \u0024{grouperProvisioningGroup.retrieveAttributeValueString('groupAttributeResolverSql__gid_number')}
provisioner.iam_unifieddb_group_memberships.targetGroupAttribute.2.translateExpressionType = translationScript
provisioner.iam_unifieddb_group_memberships.targetMembershipAttribute.0.name = group_id
provisioner.iam_unifieddb_group_memberships.targetMembershipAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.iam_unifieddb_group_memberships.targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField = id
provisioner.iam_unifieddb_group_memberships.targetMembershipAttribute.1.name = entity_id
provisioner.iam_unifieddb_group_memberships.targetMembershipAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.iam_unifieddb_group_memberships.targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField = id
provisioner.iam_unifieddb_group_memberships.unresolvableSubjectsRemove = true
provisioner.iam_unifieddb_group_memberships.useSeparateTableForGroupAttributes = true
provisioner.iam_unifieddb_group_memberships.userPrimaryKey = id
provisioner.iam_unifieddb_group_memberships.userTableName = from_grouper_entity
```

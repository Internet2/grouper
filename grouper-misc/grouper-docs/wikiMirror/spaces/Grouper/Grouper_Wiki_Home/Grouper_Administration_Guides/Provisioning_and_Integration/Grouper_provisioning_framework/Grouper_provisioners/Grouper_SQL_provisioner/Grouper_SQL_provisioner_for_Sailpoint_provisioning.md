---
title: "Grouper SQL provisioner for Sailpoint provisioning"
space: Grouper
pageId: 28560147
version: 2
lastUpdated: 2026-07-01T05:36:11.377Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560147/Grouper+SQL+provisioner+for+Sailpoint+provisioning
---

At Penn we provision group memberships to Sailpoint using shared tables. Note there is a trigger that populates a change log table to tell Sailpoint IIQ to pick up a record in real time.

## Postgres DDL

```
CREATE TABLE iiq_groups_from_penngroups (
	group_name varchar(1024) NOT NULL,
	friendly_name varchar(1024) NOT NULL,
	CONSTRAINT iiq_groups_from_penngroups_pk PRIMARY KEY (group_name),
	CONSTRAINT iiq_groups_from_penngroups_un UNIQUE (friendly_name)
)
GRANT SELECT ON TABLE iiq_groups_from_penngroups TO iiq_penngroups_prod;
```

```
CREATE TABLE iiq_mships_from_penngrps_chlog (
	penn_id varchar(12) NOT NULL,
	"action" varchar(10) NOT NULL DEFAULT 'insert'::character varying
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE iiq_mships_from_penngrps_chlog TO iiq_penngroups_prod;
```

```

CREATE TABLE penngrouper.iiq_mships_from_penngroups (
	group_name varchar(1024) NOT NULL,
	penn_id varchar(12) NOT NULL,
	CONSTRAINT iiq_mships_from_penngroups_pk PRIMARY KEY (group_name, penn_id)
);

CREATE INDEX iiq_mships_from_penngroups_group_name_idx ON iiq_mships_from_penngroups USING btree (group_name, penn_id);
CREATE INDEX iiq_mships_from_penngroups_penn_id_idx ON iiq_mships_from_penngroups USING btree (penn_id, group_name);

create trigger iiq_mships_from_pgr_trig after
insert
    or
delete
    or
update
    on
    iiq_mships_from_penngroups for each row execute function iiq_mships_from_pgr_func();

GRANT SELECT ON TABLE iiq_mships_from_penngroups TO iiq_penngroups_prod;

ALTER TABLE penngrouper.iiq_mships_from_penngroups ADD CONSTRAINT iiq_mships_from_penngroups1_fk FOREIGN KEY (penn_id) REFERENCES penngrouper.iiq_users_from_penngroups(penn_id);
ALTER TABLE penngrouper.iiq_mships_from_penngroups ADD CONSTRAINT iiq_mships_from_penngroups2_fk FOREIGN KEY (group_name) REFERENCES penngrouper.iiq_groups_from_penngroups(group_name);
```

## Provisioner config

```
provisioner.groupsToSailpoint.addDisabledFullSyncDaemon = true
provisioner.groupsToSailpoint.addDisabledIncrementalSyncDaemon = true
provisioner.groupsToSailpoint.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
provisioner.groupsToSailpoint.customizeEntityCrud = true
provisioner.groupsToSailpoint.customizeGroupCrud = true
provisioner.groupsToSailpoint.customizeMembershipCrud = true
provisioner.groupsToSailpoint.dbExternalSystemConfigId = grouper
provisioner.groupsToSailpoint.deleteEntitiesIfNotExistInGrouper = true
provisioner.groupsToSailpoint.deleteGroupsIfNotExistInGrouper = true
provisioner.groupsToSailpoint.deleteMembershipsIfNotExistInGrouper = true
provisioner.groupsToSailpoint.entityMatchingAttribute0name = penn_id
provisioner.groupsToSailpoint.entityMatchingAttributeCount = 1
provisioner.groupsToSailpoint.groupMatchingAttribute0name = group_name
provisioner.groupsToSailpoint.groupMatchingAttributeCount = 1
provisioner.groupsToSailpoint.groupTableIdColumn = group_name
provisioner.groupsToSailpoint.groupTableName = iiq_groups_from_penngroups
provisioner.groupsToSailpoint.hasTargetEntityLink = false
provisioner.groupsToSailpoint.hasTargetGroupLink = false
provisioner.groupsToSailpoint.logAllObjectsVerbose = true
provisioner.groupsToSailpoint.makeChangesToEntities = true
provisioner.groupsToSailpoint.membershipEntityForeignKeyColumn = penn_id
provisioner.groupsToSailpoint.membershipGroupForeignKeyColumn = group_name
provisioner.groupsToSailpoint.membershipTableName = iiq_mships_from_penngroups
provisioner.groupsToSailpoint.numberOfEntityAttributes = 1
provisioner.groupsToSailpoint.numberOfGroupAttributes = 2
provisioner.groupsToSailpoint.numberOfMembershipAttributes = 2
provisioner.groupsToSailpoint.operateOnGrouperEntities = true
provisioner.groupsToSailpoint.operateOnGrouperGroups = true
provisioner.groupsToSailpoint.operateOnGrouperMemberships = true
provisioner.groupsToSailpoint.provisioningType = membershipObjects
provisioner.groupsToSailpoint.selectAllEntities = true
provisioner.groupsToSailpoint.showAdvanced = true
provisioner.groupsToSailpoint.startWith = this is start with read only
provisioner.groupsToSailpoint.subjectSourcesToProvision = pennperson
provisioner.groupsToSailpoint.targetEntityAttribute.0.name = penn_id
provisioner.groupsToSailpoint.targetEntityAttribute.0.translateExpressionType = grouperProvisioningEntityField
provisioner.groupsToSailpoint.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField = subjectId
provisioner.groupsToSailpoint.targetGroupAttribute.0.name = group_name
provisioner.groupsToSailpoint.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.groupsToSailpoint.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = name
provisioner.groupsToSailpoint.targetGroupAttribute.1.name = friendly_name
provisioner.groupsToSailpoint.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.groupsToSailpoint.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = displayName
provisioner.groupsToSailpoint.targetMembershipAttribute.0.name = group_name
provisioner.groupsToSailpoint.targetMembershipAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.groupsToSailpoint.targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField = name
provisioner.groupsToSailpoint.targetMembershipAttribute.1.name = penn_id
provisioner.groupsToSailpoint.targetMembershipAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.groupsToSailpoint.targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField = subjectId
provisioner.groupsToSailpoint.userPrimaryKey = penn_id
provisioner.groupsToSailpoint.userTableName = iiq_users_from_penngroups

```

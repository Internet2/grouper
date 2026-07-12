---
title: "Grouper Duo admin roles"
space: Grouper
pageId: 28560193
version: 8
lastUpdated: 2026-07-01T05:36:05.175Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560193/Grouper+Duo+admin+roles
---

Duo administrators (of all roles) are provisioned from Grouper to Duo. Users who leave are automatically deprovisioned

## Manage users

Someone who is a Duo owner should be administering the roles from Grouper. If roles are assigned directly in Duo, they will be overwritten since Grouper is the system of record. We have SSO mandated in Duo prod so all users are provisioned as their EPPN.

## Deprovisioning users

All of the roles in Duo have membership requirements to auto-deprovision users. Generally a group shouldnt be added to a role group unless that group has auto-deprovisioning, since the membership requirement is only for direct manual memberships.

## New role members

Administrative role members in Duo need to be invited. This is done automatically when they are provisioned by PennGroups. New users should get an email from Duo to setup their account. If there is a problem (e.g. if they do not activate in time), a Duo owner can resend an email to the user from Duo.

## Setup credential in Duo

## Setup external system in PennGroups

## Configure provisioner

```
provisioner.duoAdminRoleProd.addDisabledFullSyncDaemon = true
provisioner.duoAdminRoleProd.addDisabledIncrementalSyncDaemon = true
provisioner.duoAdminRoleProd.allowAssignmentsOnlyOnOneStem = true
provisioner.duoAdminRoleProd.class = edu.internet2.middleware.grouper.app.duo.role.GrouperDuoRoleProvisioner
provisioner.duoAdminRoleProd.customizeEntityCrud = true
provisioner.duoAdminRoleProd.customizeGroupCrud = true
provisioner.duoAdminRoleProd.customizeMembershipCrud = true
provisioner.duoAdminRoleProd.deleteEntitiesIfNotExistInGrouper = true
provisioner.duoAdminRoleProd.deleteMemberships = true
provisioner.duoAdminRoleProd.deleteMembershipsIfNotExistInGrouper = true
provisioner.duoAdminRoleProd.duoExternalSystemConfigId = duoAdminProvisioningProd
provisioner.duoAdminRoleProd.entityAttributeValueCache0entityAttribute = id
provisioner.duoAdminRoleProd.entityAttributeValueCache0has = true
provisioner.duoAdminRoleProd.entityAttributeValueCache0source = target
provisioner.duoAdminRoleProd.entityAttributeValueCache0type = entityAttribute
provisioner.duoAdminRoleProd.entityAttributeValueCache1entityAttribute = email
provisioner.duoAdminRoleProd.entityAttributeValueCache1has = true
provisioner.duoAdminRoleProd.entityAttributeValueCache1source = target
provisioner.duoAdminRoleProd.entityAttributeValueCache1type = entityAttribute
provisioner.duoAdminRoleProd.entityAttributeValueCacheHas = true
provisioner.duoAdminRoleProd.entityMatchingAttribute0name = email
provisioner.duoAdminRoleProd.entityMatchingAttributeCount = 1
provisioner.duoAdminRoleProd.entityMembershipAttributeName = role
provisioner.duoAdminRoleProd.entityMembershipAttributeValue = groupAttributeValueCache0
provisioner.duoAdminRoleProd.groupAllowedToView = penn\u003Aisc\u003Aait\u003Aapps\u003AtwoFactor\u003AtwoFactorSecurity\u003AtwoFactorOwners
provisioner.duoAdminRoleProd.groupAttributeValueCache0groupAttribute = role
provisioner.duoAdminRoleProd.groupAttributeValueCache0has = true
provisioner.duoAdminRoleProd.groupAttributeValueCache0source = grouper
provisioner.duoAdminRoleProd.groupAttributeValueCache0type = groupAttribute
provisioner.duoAdminRoleProd.groupAttributeValueCacheHas = true
provisioner.duoAdminRoleProd.hasTargetEntityLink = true
provisioner.duoAdminRoleProd.logAllObjectsVerbose = true
provisioner.duoAdminRoleProd.logAllObjectsVerboseToLogFile = true
provisioner.duoAdminRoleProd.logCommandsAlways = true
provisioner.duoAdminRoleProd.makeChangesToEntities = true
provisioner.duoAdminRoleProd.numberOfEntityAttributes = 5
provisioner.duoAdminRoleProd.numberOfGroupAttributes = 1
provisioner.duoAdminRoleProd.onlyProvisionPolicyGroups = true
provisioner.duoAdminRoleProd.operateOnGrouperEntities = true
provisioner.duoAdminRoleProd.operateOnGrouperGroups = true
provisioner.duoAdminRoleProd.operateOnGrouperMemberships = true
provisioner.duoAdminRoleProd.provisioningType = entityAttributes
provisioner.duoAdminRoleProd.selectAllEntities = true
provisioner.duoAdminRoleProd.selectGroups = false
provisioner.duoAdminRoleProd.showAdvanced = true
provisioner.duoAdminRoleProd.showAssigningProvisioning = true
provisioner.duoAdminRoleProd.startWith = this is start with read only
provisioner.duoAdminRoleProd.subjectSourcesToProvision = pennperson
provisioner.duoAdminRoleProd.targetEntityAttribute.0.insert = false
provisioner.duoAdminRoleProd.targetEntityAttribute.0.name = id
provisioner.duoAdminRoleProd.targetEntityAttribute.0.showAdvancedAttribute = true
provisioner.duoAdminRoleProd.targetEntityAttribute.0.showAttributeCrud = true
provisioner.duoAdminRoleProd.targetEntityAttribute.0.update = false
provisioner.duoAdminRoleProd.targetEntityAttribute.1.defaultValue = Read-only
provisioner.duoAdminRoleProd.targetEntityAttribute.1.name = role
provisioner.duoAdminRoleProd.targetEntityAttribute.1.required = true
provisioner.duoAdminRoleProd.targetEntityAttribute.1.showAdvancedAttribute = true
provisioner.duoAdminRoleProd.targetEntityAttribute.1.showAttributeValidation = true
provisioner.duoAdminRoleProd.targetEntityAttribute.1.showAttributeValueSettings = true
provisioner.duoAdminRoleProd.targetEntityAttribute.2.name = name
provisioner.duoAdminRoleProd.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.duoAdminRoleProd.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = name
provisioner.duoAdminRoleProd.targetEntityAttribute.3.name = email
provisioner.duoAdminRoleProd.targetEntityAttribute.3.translateExpression = \u0024{grouperProvisioningEntity.getSubjectIdentifier0() + '@upenn.edu'}
provisioner.duoAdminRoleProd.targetEntityAttribute.3.translateExpressionType = translationScript
provisioner.duoAdminRoleProd.targetEntityAttribute.4.name = send_email
provisioner.duoAdminRoleProd.targetEntityAttribute.4.select = false
provisioner.duoAdminRoleProd.targetEntityAttribute.4.showAdvancedAttribute = true
provisioner.duoAdminRoleProd.targetEntityAttribute.4.showAttributeCrud = true
provisioner.duoAdminRoleProd.targetEntityAttribute.4.translateExpressionType = staticValues
provisioner.duoAdminRoleProd.targetEntityAttribute.4.translateFromStaticValues = 1
provisioner.duoAdminRoleProd.targetEntityAttribute.4.update = false
provisioner.duoAdminRoleProd.targetGroupAttribute.0.name = role
provisioner.duoAdminRoleProd.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.duoAdminRoleProd.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = displayExtension

```

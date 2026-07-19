---
title: "Grouper Duo Administrator Role Provisioner"
space: Grouper
pageId: 28555002
version: 7
lastUpdated: 2026-07-01T05:39:10.098Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555002/Grouper+Duo+Administrator+Role+Provisioner
---

This works in Grouper in v4.7.0+

## External System

Note the credential on the Duo side needs "Grant administrators" and "Grant read resource"

[See Duo external system doc](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548604/Grouper+Duo+External+System)

## Description

- Duo admin users use the email address for username
- Each user can have 1 and only 1 role
- In your prod env you should have an email routable EPPN from SSO and setup and require SSO for administrators
- The provisioner can send an email invite for new users (since a new user cannot use Duo as an administrator until they accept an email invite previously sent)
- Note, other environments cannot have admins with same login (email) as other envs. Its a best practice to have a standard email suffix for each env (non-prod vs prod)
- Note, if a user is an admin of another Duo env (e.g. not at your institution) using their eppn, then they need to change that login id
- Example at Penn:
  
  - Prod: users use their EPPN with SSO: [loginid@example.com](mailto:loginid@example.com)
  - Non-prod: users use their IT email address without SSO: [login@example.com](mailto:login@example.com)
- Its possible if you do not have a standard email address to set that with entity metadata (i.e. set it in the provisioning settings for a subject in grouper)
- The role name can be the display extension of the group, or could be metadata. It must match exactly the role in Duo
- If someone is in multiple roles in Grouper, the provisioner will select the most important role and use that (since they can only have one)
- If someone doesnt have a role, and entities are set to be deleted, then their account will be deleted in Duo

Manage administrators in Duo

## Attributes

Group fields and attributes

| Grouper name | Type | Required | Description |
| --- | --- | --- | --- |
| role | String | required | role name |

Entity fields and attributes

| Grouper name | Type | Required | Description |
| --- | --- | --- | --- |
| role | String | required | role name. Check this [doc](https://duo.com/docs/adminapi#create-administrator) for most recent available roles. e.g.   Owner, Administrator, Application Manager, User Manager, Help Desk, Billing, or Read-only |
| id | String | required | admin id |
| name | String | required | name of the admin |
| email | String | required | unique email address of the admin |
| send_email | String | optional | 1 send email for new users, 0 dont (default) |

## Example configuration

This folder is provisioner and has a group for all the admin roles

```
provisioner.duoAdminRoleTest.addDisabledFullSyncDaemon = true
provisioner.duoAdminRoleTest.addDisabledIncrementalSyncDaemon = true
provisioner.duoAdminRoleTest.allowAssignmentsOnlyOnOneStem = true
provisioner.duoAdminRoleTest.class = edu.internet2.middleware.grouper.app.duo.role.GrouperDuoRoleProvisioner
provisioner.duoAdminRoleTest.customizeEntityCrud = true
provisioner.duoAdminRoleTest.customizeGroupCrud = true
provisioner.duoAdminRoleTest.customizeMembershipCrud = true
provisioner.duoAdminRoleTest.deleteEntitiesIfNotExistInGrouper = true
provisioner.duoAdminRoleTest.deleteMemberships = true
provisioner.duoAdminRoleTest.deleteMembershipsIfNotExistInGrouper = true
provisioner.duoAdminRoleTest.duoExternalSystemConfigId = duoAdminProvisioningTest
provisioner.duoAdminRoleTest.entityAttributeValueCache0entityAttribute = id
provisioner.duoAdminRoleTest.entityAttributeValueCache0has = true
provisioner.duoAdminRoleTest.entityAttributeValueCache0source = target
provisioner.duoAdminRoleTest.entityAttributeValueCache0type = entityAttribute
provisioner.duoAdminRoleTest.entityAttributeValueCache1entityAttribute = email
provisioner.duoAdminRoleTest.entityAttributeValueCache1has = true
provisioner.duoAdminRoleTest.entityAttributeValueCache1source = target
provisioner.duoAdminRoleTest.entityAttributeValueCache1type = entityAttribute
provisioner.duoAdminRoleTest.entityAttributeValueCacheHas = true
provisioner.duoAdminRoleTest.entityMatchingAttribute0name = email
provisioner.duoAdminRoleTest.entityMatchingAttributeCount = 1
provisioner.duoAdminRoleTest.entityMembershipAttributeName = role
provisioner.duoAdminRoleTest.entityMembershipAttributeValue = groupAttributeValueCache0
provisioner.duoAdminRoleTest.groupAllowedToView = penn:isc:ait:apps:twoFactor:twoFactorSecurity:twoFactorOwners
provisioner.duoAdminRoleTest.groupAttributeValueCache0groupAttribute = role
provisioner.duoAdminRoleTest.groupAttributeValueCache0has = true
provisioner.duoAdminRoleTest.groupAttributeValueCache0source = grouper
provisioner.duoAdminRoleTest.groupAttributeValueCache0type = groupAttribute
provisioner.duoAdminRoleTest.groupAttributeValueCacheHas = true
provisioner.duoAdminRoleTest.hasTargetEntityLink = true
provisioner.duoAdminRoleTest.logAllObjectsVerbose = true
provisioner.duoAdminRoleTest.logAllObjectsVerboseToLogFile = true
provisioner.duoAdminRoleTest.logCommandsAlways = true
provisioner.duoAdminRoleTest.makeChangesToEntities = true
provisioner.duoAdminRoleTest.numberOfEntityAttributes = 5
provisioner.duoAdminRoleTest.numberOfGroupAttributes = 1
provisioner.duoAdminRoleTest.onlyProvisionPolicyGroups = true
provisioner.duoAdminRoleTest.operateOnGrouperEntities = true
provisioner.duoAdminRoleTest.operateOnGrouperGroups = true
provisioner.duoAdminRoleTest.operateOnGrouperMemberships = true
provisioner.duoAdminRoleTest.provisioningType = entityAttributes
provisioner.duoAdminRoleTest.selectAllEntities = true
provisioner.duoAdminRoleTest.selectGroups = false
provisioner.duoAdminRoleTest.showAdvanced = true
provisioner.duoAdminRoleTest.showAssigningProvisioning = true
provisioner.duoAdminRoleTest.startWith = this is start with read only
provisioner.duoAdminRoleTest.subjectSourcesToProvision = pennperson
provisioner.duoAdminRoleTest.targetEntityAttribute.0.insert = false
provisioner.duoAdminRoleTest.targetEntityAttribute.0.name = id
provisioner.duoAdminRoleTest.targetEntityAttribute.0.showAdvancedAttribute = true
provisioner.duoAdminRoleTest.targetEntityAttribute.0.showAttributeCrud = true
provisioner.duoAdminRoleTest.targetEntityAttribute.0.update = false
provisioner.duoAdminRoleTest.targetEntityAttribute.1.defaultValue = Read-only
provisioner.duoAdminRoleTest.targetEntityAttribute.1.name = role
provisioner.duoAdminRoleTest.targetEntityAttribute.1.required = true
provisioner.duoAdminRoleTest.targetEntityAttribute.1.showAdvancedAttribute = true
provisioner.duoAdminRoleTest.targetEntityAttribute.1.showAttributeValidation = true
provisioner.duoAdminRoleTest.targetEntityAttribute.1.showAttributeValueSettings = true
provisioner.duoAdminRoleTest.targetEntityAttribute.2.name = name
provisioner.duoAdminRoleTest.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.duoAdminRoleTest.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = name
provisioner.duoAdminRoleTest.targetEntityAttribute.3.name = email
provisioner.duoAdminRoleTest.targetEntityAttribute.3.translateExpression = \u0024{grouperProvisioningEntity.getSubjectIdentifier0() + '@isc.upenn.edu'}
provisioner.duoAdminRoleTest.targetEntityAttribute.3.translateExpressionType = translationScript
provisioner.duoAdminRoleTest.targetEntityAttribute.4.name = send_email
provisioner.duoAdminRoleTest.targetEntityAttribute.4.select = false
provisioner.duoAdminRoleTest.targetEntityAttribute.4.showAdvancedAttribute = true
provisioner.duoAdminRoleTest.targetEntityAttribute.4.showAttributeCrud = true
provisioner.duoAdminRoleTest.targetEntityAttribute.4.translateExpressionType = staticValues
provisioner.duoAdminRoleTest.targetEntityAttribute.4.translateFromStaticValues = 1
provisioner.duoAdminRoleTest.targetEntityAttribute.4.update = false
provisioner.duoAdminRoleTest.targetGroupAttribute.0.name = role
provisioner.duoAdminRoleTest.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.duoAdminRoleTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = displayExtension

```

---
title: "Grouper Duo Administrator Role Provisioner"
space: Grouper
pageId: 28555002
version: 9
lastUpdated: 2026-10-18T04:43:21.098Z
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
- The role can be the display extension of the group, or could be metadata. It can be a Duo role name (case insensitive) or role id, including custom admin roles. See roles and custom roles
- If someone is in multiple roles in Grouper, the provisioner will select the highest priority role and use that (since they can only have one)
- If someone doesnt have a role, and entities are set to be deleted, then their account will be deleted in Duo

Manage administrators in Duo

## Roles and custom roles

v7.9.0+, v6.7.0+, v4.28.0+ (GRP-7497): the provisioner sends Duo the role_id, not the legacy role name which Duo is deprecating. This allows custom admin roles. It is backwards compatible: existing role names keep working, no config change is needed.

- The role attribute value can be a role name (case insensitive) or a role id
- Custom role names are looked up with GET /admin/v1/admin_roles, which needs "Grant read resource" on the Duo credential. Without it, built-in role names and any role id (including a custom role id) still work
- Get a custom role id from the role in the Duo admin panel (Users, Administrators, Admin Roles)
- An unknown value is sent as a role id, and Duo rejects that administrator if it is not valid

Built-in roles

| Role name | Role id | Default priority |
| --- | --- | --- |
| Owner | owner | 800 |
| Administrator | service_manager | 700 |
| Application Manager | application_manager | 600 |
| User Manager | user_manager | 500 |
| Security Analyst | security_analyst | 400 |
| Help Desk | help_desk | 300 |
| Billing | billing | 200 |
| Read-only | read_only | 100 |

Priority: if someone is in several role groups, the highest priority role wins. Custom roles default to 150 (above Read-only only). Ties go to the alphabetically first role id. Override any priority with the optional "Role priority" setting, JSON of role name or role id to integer:

```
provisioner.duoAdminRoleTest.rolePriority = {"Tier 2 Support": 350, "RLU6PE8ICYDAWJY30ZJ2": 750}
```

## Attributes

Group fields and attributes

| Grouper name | Type | Required | Description |
| --- | --- | --- | --- |
| role | String | required | role name or role id |

Entity fields and attributes

| Grouper name | Type | Required | Description |
| --- | --- | --- | --- |
| role | String | required | role name or role id. Sent to Duo as role_id. Check this [doc](https://duo.com/docs/adminapi#create-administrator) for most recent available roles. e.g.   Owner, Administrator, Application Manager, User Manager, Security Analyst, Help Desk, Billing, or Read-only |
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

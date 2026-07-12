---
title: "Grouper provisioning SCIM for GitHub with organizations as groups"
space: Grouper
pageId: 28564280
version: 6
lastUpdated: 2026-07-01T05:35:23.433Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564280/Grouper+provisioning+SCIM+for+GitHub+with+organizations+as+groups
---

This is for v4.13.1+ and v5.10.2+.

GitHub allows SCIM users to be created in organizations. So the basic Grouper SCIM GitHub provisioner will send users to one organization. If you wanted to provision to multiple organizations, you would need to create multiple provisioners each pointing to one organization. Most GitHub deployments have many groups in GitHub (which map to organizations), so this previous way did not scale well in that circumstance.

Each provisionable group represents an organization. The url part of the organization (recommended to be alphanumeric), will map from the group (e.g. from group extension or display extension or metadata). Grouper will not create / update / delete groups (as organizations). The entities are configured as select only too, since the users in organizations in GitHub are really memberships since there is no single user list.

Note this is just memberships. GitHub has roles attached to memberships. e.g. readonly, committed, admin, etc. This provisioner just does basic users in groups via SCIM, so whatever the organizations are mapped to will need a role assigned to its membership in a project or group.

## External system

Note you need to not put an organization in the external system since this will be appended for each provisionable group

You will also need to change the external system test for the external system test case appropriately (add an existing organization)

## Configuration

You need to operate (and only select) groups, map the id to be organization (e.g. from group extension or display extension or metadata). The matching of the memberships is a little different too since its based on the matching id of the entities

```
provisioner.githubProvisioner.acceptHeader = application/vnd.github.v3+json
provisioner.githubProvisioner.bearerTokenExternalSystemConfigId = githubExternalSystem
provisioner.githubProvisioner.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.githubProvisioner.customizeEntityCrud = true
provisioner.githubProvisioner.customizeGroupCrud = true
provisioner.githubProvisioner.debugLog = true
provisioner.githubProvisioner.deleteGroups = false
provisioner.githubProvisioner.entity2advanced = true
provisioner.githubProvisioner.entityAttributeValueCache0has = true
provisioner.githubProvisioner.entityAttributeValueCache0nullChecksInScript = true
provisioner.githubProvisioner.entityAttributeValueCache0source = grouper
provisioner.githubProvisioner.entityAttributeValueCache0translationContinueCondition = \u0024{subject != null}
provisioner.githubProvisioner.entityAttributeValueCache0translationScript = \u0024{subject.getAttributeValue('email')}
provisioner.githubProvisioner.entityAttributeValueCache0type = subjectTranslationScript
provisioner.githubProvisioner.entityAttributeValueCache2entityAttribute = id
provisioner.githubProvisioner.entityAttributeValueCache2has = true
provisioner.githubProvisioner.entityAttributeValueCache2source = target
provisioner.githubProvisioner.entityAttributeValueCache2type = entityAttribute
provisioner.githubProvisioner.entityAttributeValueCacheHas = true
provisioner.githubProvisioner.entityMatchingAttribute0name = userName
provisioner.githubProvisioner.entityMatchingAttributeCount = 1
provisioner.githubProvisioner.groupMatchingAttribute0name = id
provisioner.githubProvisioner.groupMatchingAttributeCount = 1
provisioner.githubProvisioner.hasTargetEntityLink = true
provisioner.githubProvisioner.insertGroups = false
provisioner.githubProvisioner.logAllObjectsVerbose = true
provisioner.githubProvisioner.logCommandsAlways = true
provisioner.githubProvisioner.makeChangesToEntities = false
provisioner.githubProvisioner.membership2AdvancedOptions = true
provisioner.githubProvisioner.membershipMatchingIdExpression = \u0024{new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntity().retrieveAttributeValueString('userName'))}
provisioner.githubProvisioner.numberOfEntityAttributes = 5
provisioner.githubProvisioner.numberOfGroupAttributes = 1
provisioner.githubProvisioner.operateOnGrouperEntities = true
provisioner.githubProvisioner.operateOnGrouperGroups = true
provisioner.githubProvisioner.operateOnGrouperMemberships = true
provisioner.githubProvisioner.provisioningType = membershipObjects
provisioner.githubProvisioner.scimType = Github
provisioner.githubProvisioner.selectAllEntities = true
provisioner.githubProvisioner.selectAllGroups = false
provisioner.githubProvisioner.selectEntities = true
provisioner.githubProvisioner.showAdvanced = true
provisioner.githubProvisioner.startWith = this is start with read only
provisioner.githubProvisioner.subjectSourcesToProvision = jdbc
provisioner.githubProvisioner.targetEntityAttribute.0.name = id
provisioner.githubProvisioner.targetEntityAttribute.1.name = userName
provisioner.githubProvisioner.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.githubProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectId
provisioner.githubProvisioner.targetEntityAttribute.2.name = givenName
provisioner.githubProvisioner.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.githubProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = name
provisioner.githubProvisioner.targetEntityAttribute.3.name = familyName
provisioner.githubProvisioner.targetEntityAttribute.3.translateExpressionType = grouperProvisioningEntityField
provisioner.githubProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField = name
provisioner.githubProvisioner.targetEntityAttribute.4.name = emailValue
provisioner.githubProvisioner.targetEntityAttribute.4.translateExpression = \u0024{gcGrouperSyncMember.getEntityAttributeValueCache0()}
provisioner.githubProvisioner.targetEntityAttribute.4.translateExpressionType = translationScript
provisioner.githubProvisioner.targetGroupAttribute.0.name = id
provisioner.githubProvisioner.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.githubProvisioner.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = extension

```

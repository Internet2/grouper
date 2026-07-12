---
title: "Grouper provisioning SCIM for Atlassian"
space: Grouper
pageId: 28564203
version: 7
lastUpdated: 2026-07-01T05:35:31.646Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564203/Grouper+provisioning+SCIM+for+Atlassian
---

This is for Grouper v4.4+

## Description

- Atlassian supports SCIM to provision to Atlassian Access (free for higher ed) per organization
- If you want the same groups provisioned to multiple applications, have the applications in the same Atlassian organization. If you want distinct groups in different products in Atlassian, create an organization for each product.
- External users can be provisioned to groups in Atlassian. If the domain of the user is claimed and configured for SSO then the user will use their SSO. If not, then they create an account in Atlassian
- To allow external users to log in with their SSO, you can have a SAML proxy (e.g. satosa), and use email forwarding
- Users in Atlassian cloud have an email address which is the same as their login id
- Note that the SCIM integration with Atlassian does not manage all groups in the organization. If there are groups that were created manually, they will not be available to manage over SCIM. Only the groups created by SCIM can be managed by SCIM
- It seems that users cannot be updated by SCIM especially if they were created outside of SCIM

## External system

Create a SCIM security token in Atlassian admin

## Handling external users

This example shows how to use Grouper local entities to model external entities in Atlassian cloud.

If there is a user in Atlassian cloud who is not in a subject source, you can add an external entity and set the display extension.

## Provisioner config

Config file

```
provisioner.atlassianCloudJira.acceptHeader = application/json
provisioner.atlassianCloudJira.addDisabledFullSyncDaemon = true
provisioner.atlassianCloudJira.addDisabledIncrementalSyncDaemon = true
provisioner.atlassianCloudJira.bearerTokenExternalSystemConfigId = atlassianCloudJira
provisioner.atlassianCloudJira.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.atlassianCloudJira.customizeEntityCrud = true
provisioner.atlassianCloudJira.customizeGroupCrud = true
provisioner.atlassianCloudJira.customizeMembershipCrud = true
provisioner.atlassianCloudJira.deleteEntities = false
provisioner.atlassianCloudJira.deleteGroupsIfNotExistInGrouper = true
provisioner.atlassianCloudJira.deleteMembershipsIfNotExistInGrouper = true
provisioner.atlassianCloudJira.entityAttributeValueCache0entityAttribute = id
provisioner.atlassianCloudJira.entityAttributeValueCache0has = true
provisioner.atlassianCloudJira.entityAttributeValueCache0source = target
provisioner.atlassianCloudJira.entityAttributeValueCache0type = entityAttribute
provisioner.atlassianCloudJira.entityAttributeValueCache1has = true
provisioner.atlassianCloudJira.entityAttributeValueCache1source = grouper
provisioner.atlassianCloudJira.entityAttributeValueCache1translationScript = \u0024{subject.sourceId == 'grouperEntities' ? subject.getAttributeValue('displayExtension') \u003A subject.getAttributeValue('eppn')}
provisioner.atlassianCloudJira.entityAttributeValueCache1type = subjectTranslationScript
provisioner.atlassianCloudJira.entityAttributeValueCacheHas = true
provisioner.atlassianCloudJira.entityMatchingAttribute0name = userName
provisioner.atlassianCloudJira.entityMatchingAttributeCount = 1
provisioner.atlassianCloudJira.groupAttributeValueCache0groupAttribute = id
provisioner.atlassianCloudJira.groupAttributeValueCache0has = true
provisioner.atlassianCloudJira.groupAttributeValueCache0source = target
provisioner.atlassianCloudJira.groupAttributeValueCache0type = groupAttribute
provisioner.atlassianCloudJira.groupAttributeValueCacheHas = true
provisioner.atlassianCloudJira.groupMatchingAttribute0name = displayName
provisioner.atlassianCloudJira.groupMatchingAttributeCount = 1
provisioner.atlassianCloudJira.hasTargetEntityLink = true
provisioner.atlassianCloudJira.hasTargetGroupLink = true
provisioner.atlassianCloudJira.logAllObjectsVerbose = true
provisioner.atlassianCloudJira.logAllObjectsVerboseToLogFile = false
provisioner.atlassianCloudJira.makeChangesToEntities = true
provisioner.atlassianCloudJira.numberOfEntityAttributes = 4
provisioner.atlassianCloudJira.numberOfGroupAttributes = 2
provisioner.atlassianCloudJira.operateOnGrouperEntities = true
provisioner.atlassianCloudJira.operateOnGrouperGroups = true
provisioner.atlassianCloudJira.operateOnGrouperMemberships = true
provisioner.atlassianCloudJira.provisioningType = membershipObjects
provisioner.atlassianCloudJira.scimType = generic
provisioner.atlassianCloudJira.selectAllEntities = true
provisioner.atlassianCloudJira.selectMemberships = false
provisioner.atlassianCloudJira.showAdvanced = true
provisioner.atlassianCloudJira.startWith = this is start with read only
provisioner.atlassianCloudJira.subjectSourcesToProvision = grouperEntities,pennperson
provisioner.atlassianCloudJira.targetEntityAttribute.0.name = id
provisioner.atlassianCloudJira.targetEntityAttribute.1.name = userName
provisioner.atlassianCloudJira.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.atlassianCloudJira.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = entityAttributeValueCache1
provisioner.atlassianCloudJira.targetEntityAttribute.2.name = emailValue
provisioner.atlassianCloudJira.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.atlassianCloudJira.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = entityAttributeValueCache1
provisioner.atlassianCloudJira.targetEntityAttribute.3.name = displayName
provisioner.atlassianCloudJira.targetEntityAttribute.3.translateExpressionType = grouperProvisioningEntityField
provisioner.atlassianCloudJira.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField = name
provisioner.atlassianCloudJira.targetGroupAttribute.0.name = id
provisioner.atlassianCloudJira.targetGroupAttribute.1.name = displayName
provisioner.atlassianCloudJira.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.atlassianCloudJira.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = extension
provisioner.atlassianCloudJira.updateEntities = false
provisioner.atlassianCloudJira.updateGroups = false

```

Config UI

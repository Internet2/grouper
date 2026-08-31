---
title: "Grouper provisioning SCIM for Tableau"
space: Grouper
pageId: 28564262
version: 3
lastUpdated: 2026-07-01T05:35:25.507Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564262/Grouper+provisioning+SCIM+for+Tableau
---

Grouper SCIM can provision groups, entities and memberships to Tableau.

## External system

Go to settings in Tableau and enable SCIM, use the URL and the token

## Groups

The displayName SCIM attribute for groups can be mapped to something in Grouper. If you want special chars perhaps use displayExtension. Otherwise the extension. The groups in Grouper can be in the same folder so there are no conflicts. Map the id as usual for SCIM

## Entities

There is a bizarre Tableau name issue (in 2024, maybe fixed subsequently) where the user's first and last name is set in SAML but cannot be set in SCIM. These attributes can be mapped

- id
- email
- familyName
- givenName
- userName
- displayName

## SampleConfig

```
provisioner.TableauProvisioner.addDisabledFullSyncDaemon = true
provisioner.TableauProvisioner.addDisabledIncrementalSyncDaemon = true
provisioner.TableauProvisioner.bearerTokenExternalSystemConfigId = Tableau
provisioner.TableauProvisioner.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.TableauProvisioner.customizeEntityCrud = true
provisioner.TableauProvisioner.customizeGroupCrud = true
provisioner.TableauProvisioner.customizeMembershipCrud = false
provisioner.TableauProvisioner.deleteEntities = false
provisioner.TableauProvisioner.deleteGroups = false
provisioner.TableauProvisioner.entityAttributeValueCache0entityAttribute = id
provisioner.TableauProvisioner.entityAttributeValueCache0has = true
provisioner.TableauProvisioner.entityAttributeValueCache0source = target
provisioner.TableauProvisioner.entityAttributeValueCache0type = entityAttribute
provisioner.TableauProvisioner.entityAttributeValueCache1entityAttribute = userName
provisioner.TableauProvisioner.entityAttributeValueCache1has = true
provisioner.TableauProvisioner.entityAttributeValueCache1source = target
provisioner.TableauProvisioner.entityAttributeValueCache1type = entityAttribute
provisioner.TableauProvisioner.entityAttributeValueCache2has = true
provisioner.TableauProvisioner.entityAttributeValueCache2source = grouper
provisioner.TableauProvisioner.entityAttributeValueCache2translationScript = ${subject.getAttributeValue('sn')}
provisioner.TableauProvisioner.entityAttributeValueCache2type = subjectTranslationScript
provisioner.TableauProvisioner.entityAttributeValueCache3has = true
provisioner.TableauProvisioner.entityAttributeValueCache3source = grouper
provisioner.TableauProvisioner.entityAttributeValueCache3translationScript = ${subject.getAttributeValue('givenName')}
provisioner.TableauProvisioner.entityAttributeValueCache3type = subjectTranslationScript
provisioner.TableauProvisioner.entityAttributeValueCacheHas = true
provisioner.TableauProvisioner.entityMatchingAttribute0name = userName
provisioner.TableauProvisioner.entityMatchingAttribute1name = id
provisioner.TableauProvisioner.entityMatchingAttributeCount = 2
provisioner.TableauProvisioner.groupAttributeValueCache0groupAttribute = id
provisioner.TableauProvisioner.groupAttributeValueCache0has = true
provisioner.TableauProvisioner.groupAttributeValueCache0source = target
provisioner.TableauProvisioner.groupAttributeValueCache0type = groupAttribute
provisioner.TableauProvisioner.groupAttributeValueCache1groupAttribute = displayName
provisioner.TableauProvisioner.groupAttributeValueCache1has = true
provisioner.TableauProvisioner.groupAttributeValueCache1source = target
provisioner.TableauProvisioner.groupAttributeValueCache1type = groupAttribute
provisioner.TableauProvisioner.groupAttributeValueCacheHas = true
provisioner.TableauProvisioner.groupMatchingAttribute0name = displayName
provisioner.TableauProvisioner.groupMatchingAttributeCount = 1
provisioner.TableauProvisioner.hasTargetEntityLink = true
provisioner.TableauProvisioner.hasTargetGroupLink = true
provisioner.TableauProvisioner.logAllObjectsVerbose = true
provisioner.TableauProvisioner.logCommandsAlways = true
provisioner.TableauProvisioner.makeChangesToEntities = true
provisioner.TableauProvisioner.numberOfEntityAttributes = 6
provisioner.TableauProvisioner.numberOfGroupAttributes = 2
provisioner.TableauProvisioner.operateOnGrouperEntities = true
provisioner.TableauProvisioner.operateOnGrouperGroups = true
provisioner.TableauProvisioner.operateOnGrouperMemberships = true
provisioner.TableauProvisioner.provisioningType = membershipObjects
provisioner.TableauProvisioner.scimType = generic
provisioner.TableauProvisioner.selectAllEntities = true
provisioner.TableauProvisioner.selectAllGroups = true
provisioner.TableauProvisioner.showAdvanced = true
provisioner.TableauProvisioner.startWith = this is start with read only
provisioner.TableauProvisioner.subjectSourcesToProvision = fischerLdapProd
provisioner.TableauProvisioner.targetEntityAttribute.0.insert = false
provisioner.TableauProvisioner.targetEntityAttribute.0.name = id
provisioner.TableauProvisioner.targetEntityAttribute.0.showAdvancedAttribute = true
provisioner.TableauProvisioner.targetEntityAttribute.0.showAttributeCrud = true
provisioner.TableauProvisioner.targetEntityAttribute.1.name = emailValue
provisioner.TableauProvisioner.targetEntityAttribute.1.translateExpression = ${grouperProvisioningEntity.getSubjectId() + '@isu.edu'}
provisioner.TableauProvisioner.targetEntityAttribute.1.translateExpressionType = translationScript
provisioner.TableauProvisioner.targetEntityAttribute.2.name = familyName
provisioner.TableauProvisioner.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.TableauProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = entityAttributeValueCache2
provisioner.TableauProvisioner.targetEntityAttribute.3.name = givenName
provisioner.TableauProvisioner.targetEntityAttribute.3.translateExpressionType = grouperProvisioningEntityField
provisioner.TableauProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField = entityAttributeValueCache3
provisioner.TableauProvisioner.targetEntityAttribute.4.name = userName
provisioner.TableauProvisioner.targetEntityAttribute.4.translateExpression = ${grouperProvisioningEntity.getSubjectId() + '@isu.edu'}
provisioner.TableauProvisioner.targetEntityAttribute.4.translateExpressionType = translationScript
provisioner.TableauProvisioner.targetEntityAttribute.5.name = displayName
provisioner.TableauProvisioner.targetEntityAttribute.5.translateExpressionType = grouperProvisioningEntityField
provisioner.TableauProvisioner.targetEntityAttribute.5.translateFromGrouperProvisioningEntityField = name
provisioner.TableauProvisioner.targetGroupAttribute.0.insert = false
provisioner.TableauProvisioner.targetGroupAttribute.0.name = id
provisioner.TableauProvisioner.targetGroupAttribute.0.showAdvancedAttribute = true
provisioner.TableauProvisioner.targetGroupAttribute.0.showAttributeCrud = true
provisioner.TableauProvisioner.targetGroupAttribute.1.name = displayName
provisioner.TableauProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.TableauProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = displayExtension
provisioner.TableauProvisioner.updateEntities = false
provisioner.TableauProvisioner.updateGroups = false
```

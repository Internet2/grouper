---
title: "Grouper provisioning SCIM for OpenAI - ChatGPT"
space: Grouper
pageId: 28564317
version: 3
lastUpdated: 2026-07-01T05:35:19.375Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564317/Grouper+provisioning+SCIM+for+OpenAI+-+ChatGPT
---

Grouper SCIM provisioning works with ChatGPT.

This is a simple configuration for users. You might be able to provision groups/memberships too. As of July 2024 this is very new on the OpenAI side.

Existing users are not seen by SCIM. So you should do this when you setup your account with ChatGPT. If you want to manage existing users it seems you need to see what roles they have in ChatGPT, delete them from the UI, provision them from Grouper, and assign the roles/etc again in the UI. It does not appear SCIM can manage if a user is an admin or not, and perhaps other things.

## External system

Go to chatgpt.com or whatever your admin URL is. Click on Identity & Provisioning, and setup a SCIM directory sync. Do a provider which is not vendor specific and get the URL and bearer token and setup an HTTP bearer token external system in Grouper

## Configure the provisioner

In this case users have a predefined suffix for this sandbox account.

```
provisioner.chatgpt_prod.addDisabledFullSyncDaemon = true
provisioner.chatgpt_prod.addDisabledIncrementalSyncDaemon = true
provisioner.chatgpt_prod.bearerTokenExternalSystemConfigId = chatgpt_prod
provisioner.chatgpt_prod.class = edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner
provisioner.chatgpt_prod.entityAttributeValueCache0entityAttribute = id
provisioner.chatgpt_prod.entityAttributeValueCache0has = true
provisioner.chatgpt_prod.entityAttributeValueCache0source = target
provisioner.chatgpt_prod.entityAttributeValueCache0type = entityAttribute
provisioner.chatgpt_prod.entityAttributeValueCache1entityAttribute = emailValue
provisioner.chatgpt_prod.entityAttributeValueCache1has = true
provisioner.chatgpt_prod.entityAttributeValueCache1source = target
provisioner.chatgpt_prod.entityAttributeValueCache1type = entityAttribute
provisioner.chatgpt_prod.entityAttributeValueCache2has = true
provisioner.chatgpt_prod.entityAttributeValueCache2source = grouper
provisioner.chatgpt_prod.entityAttributeValueCache2translationScript = \u0024{subject.getAttributeValue('first_name')}
provisioner.chatgpt_prod.entityAttributeValueCache2type = subjectTranslationScript
provisioner.chatgpt_prod.entityAttributeValueCache3has = true
provisioner.chatgpt_prod.entityAttributeValueCache3source = grouper
provisioner.chatgpt_prod.entityAttributeValueCache3translationScript = \u0024{subject.getAttributeValue('last_name')}
provisioner.chatgpt_prod.entityAttributeValueCache3type = subjectTranslationScript
provisioner.chatgpt_prod.entityAttributeValueCacheHas = true
provisioner.chatgpt_prod.entityMatchingAttribute0name = id
provisioner.chatgpt_prod.entityMatchingAttribute1name = emailValue
provisioner.chatgpt_prod.entityMatchingAttributeCount = 2
provisioner.chatgpt_prod.hasTargetEntityLink = true
provisioner.chatgpt_prod.logAllObjectsVerbose = true
provisioner.chatgpt_prod.makeChangesToEntities = true
provisioner.chatgpt_prod.numberOfEntityAttributes = 7
provisioner.chatgpt_prod.operateOnGrouperEntities = true
provisioner.chatgpt_prod.scimType = generic
provisioner.chatgpt_prod.selectAllEntities = true
provisioner.chatgpt_prod.showAdvanced = true
provisioner.chatgpt_prod.startWith = this is start with read only
provisioner.chatgpt_prod.subjectSourcesToProvision = pennperson
provisioner.chatgpt_prod.targetEntityAttribute.0.insert = false
provisioner.chatgpt_prod.targetEntityAttribute.0.name = id
provisioner.chatgpt_prod.targetEntityAttribute.0.showAdvancedAttribute = true
provisioner.chatgpt_prod.targetEntityAttribute.0.showAttributeCrud = true
provisioner.chatgpt_prod.targetEntityAttribute.0.update = false
provisioner.chatgpt_prod.targetEntityAttribute.1.name = emailValue
provisioner.chatgpt_prod.targetEntityAttribute.1.translateExpression = \u0024{ grouperProvisioningEntity.subjectIdentifier0 + '@suffix.school.edu' }
provisioner.chatgpt_prod.targetEntityAttribute.1.translateExpressionType = translationScript
provisioner.chatgpt_prod.targetEntityAttribute.2.name = displayName
provisioner.chatgpt_prod.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.chatgpt_prod.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = name
provisioner.chatgpt_prod.targetEntityAttribute.3.name = userName
provisioner.chatgpt_prod.targetEntityAttribute.3.nullChecksInScript = true
provisioner.chatgpt_prod.targetEntityAttribute.3.showAdvancedAttribute = true
provisioner.chatgpt_prod.targetEntityAttribute.3.showAttributeValidation = true
provisioner.chatgpt_prod.targetEntityAttribute.3.translateExpression = \u0024{ grouperProvisioningEntity.subjectIdentifier0 + '@suffix.school.edu' }
provisioner.chatgpt_prod.targetEntityAttribute.3.translateExpressionType = translationScript
provisioner.chatgpt_prod.targetEntityAttribute.3.translationContinueCondition = \u0024{grouperProvisioningEntity.subjectIdentifier0 != null }
provisioner.chatgpt_prod.targetEntityAttribute.3.unprovisionableIfNull = true
provisioner.chatgpt_prod.targetEntityAttribute.4.name = emailType
provisioner.chatgpt_prod.targetEntityAttribute.4.translateExpressionType = staticValues
provisioner.chatgpt_prod.targetEntityAttribute.4.translateFromStaticValues = work
provisioner.chatgpt_prod.targetEntityAttribute.5.name = givenName
provisioner.chatgpt_prod.targetEntityAttribute.5.translateExpressionType = grouperProvisioningEntityField
provisioner.chatgpt_prod.targetEntityAttribute.5.translateFromGrouperProvisioningEntityField = entityAttributeValueCache2
provisioner.chatgpt_prod.targetEntityAttribute.6.name = familyName
provisioner.chatgpt_prod.targetEntityAttribute.6.translateExpressionType = grouperProvisioningEntityField
provisioner.chatgpt_prod.targetEntityAttribute.6.translateFromGrouperProvisioningEntityField = entityAttributeValueCache3

```

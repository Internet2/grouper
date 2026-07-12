---
title: "Grouper provisioning custom metadata"
space: Grouper
pageId: 28555763
version: 16
lastUpdated: 2026-07-01T05:37:31.754Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555763/Grouper+provisioning+custom+metadata
---

> The info on this page applies to Grouper v4 and above.

A provisioner can identify some metadata (specific to that provisioner) for various object types (stems, groups, subjects, memberships) and use those while provisioning.

## Configure metadata

## 

## Configure externalized text

## Assign metadata

## Example 1

An example is a provisioner could have a group name mapping as a provisioning attribute, so not all groups in grouper need to uniformly map to groups in the target.

There can be a default value if not assigned to an object

```
${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_emailListName'), grouperProvisioningGroup.extension)}
```

## Example 2

The AD admin would like "O365" in the group description attribute to signify that the group should be provisioned from Active Directory to O365.

1. Change the description attribute to be an expression which uses the metadata if there, or the group description if not.
  
  
  ```
  ${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_o365'), grouperProvisioningGroup.retrieveAttributeValueString('description'))}
  ```
2. Create a metadata in the provisioning configuration named md_grouper_o365 which is a drop down with one value "O365"
3. Configure the externalized text (can do this from UI):  
  Note: myAdProvisioner is the provisioner configId
  
  
  ```
  md_grouper_o365_myAdProvisioner_label = O365:
  md_grouper_o365_myAdProvisioner_description = Select O365 to have this group provisioned from AD to O365
  ```

## Example 3

Import config into grouper-loader.properties. The below is an example for provisioning EDUPersonEntitlement. It will prompt the user enabling the provisioner to choose which entitlement prefix the entitlement should have from the list. The following values need to be updated before importing this into your instance:

provisioner.ldapEduEntitlementProvisioner.metadata.0.dropdownValues  
provisioner.ldapEduEntitlementProvisioner.ldapExternalSystemConfigId  
provisioner.ldapEduEntitlementProvisioner.userSearchBaseDn

```
provisioner.ldapEduEntitlementProvisioner.addDisabledFullSyncDaemon = true
provisioner.ldapEduEntitlementProvisioner.addDisabledIncrementalSyncDaemon = true
provisioner.ldapEduEntitlementProvisioner.allowPolicyGroupOverride = false
provisioner.ldapEduEntitlementProvisioner.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.ldapEduEntitlementProvisioner.configureMetadata = true
provisioner.ldapEduEntitlementProvisioner.customizeEntityCrud = true
provisioner.ldapEduEntitlementProvisioner.customizeGroupCrud = true
provisioner.ldapEduEntitlementProvisioner.customizeMembershipCrud = true
provisioner.ldapEduEntitlementProvisioner.deleteEntities = false
provisioner.ldapEduEntitlementProvisioner.deleteGroups = false
provisioner.ldapEduEntitlementProvisioner.deleteMemberships = true
provisioner.ldapEduEntitlementProvisioner.deleteMembershipsIfNotExistInGrouper = true
provisioner.ldapEduEntitlementProvisioner.deleteValueIfManagedByGrouper = true
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache0entityAttribute = ldap_dn
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache0has = true
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache0source = target
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache0type = entityAttribute
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache1entityAttribute = uid
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache1has = true
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache1source = target
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCache1type = entityAttribute
provisioner.ldapEduEntitlementProvisioner.entityAttributeValueCacheHas = true
provisioner.ldapEduEntitlementProvisioner.entityMatchingAttribute0name = uid
provisioner.ldapEduEntitlementProvisioner.entityMatchingAttributeCount = 1
provisioner.ldapEduEntitlementProvisioner.entityMembershipAttributeName = eduPersonEntitlement
provisioner.ldapEduEntitlementProvisioner.entityMembershipAttributeValue = groupAttributeValueCache0
provisioner.ldapEduEntitlementProvisioner.errorHandlingShow = true
provisioner.ldapEduEntitlementProvisioner.errorHandlingTargetObjectDoesNotExistIsAnError = false
provisioner.ldapEduEntitlementProvisioner.groupAttributeValueCache0groupAttribute = entitlementValue
provisioner.ldapEduEntitlementProvisioner.groupAttributeValueCache0has = true
provisioner.ldapEduEntitlementProvisioner.groupAttributeValueCache0source = grouper
provisioner.ldapEduEntitlementProvisioner.groupAttributeValueCache0type = groupAttribute
provisioner.ldapEduEntitlementProvisioner.groupAttributeValueCacheHas = true
provisioner.ldapEduEntitlementProvisioner.hasTargetEntityLink = true
provisioner.ldapEduEntitlementProvisioner.insertEntities = false
provisioner.ldapEduEntitlementProvisioner.insertGroups = false
provisioner.ldapEduEntitlementProvisioner.insertMemberships = true
provisioner.ldapEduEntitlementProvisioner.ldapExternalSystemConfigId = ldapProd
provisioner.ldapEduEntitlementProvisioner.logAllObjectsVerbose = true
provisioner.ldapEduEntitlementProvisioner.logCommandsOnError = true
provisioner.ldapEduEntitlementProvisioner.makeChangesToEntities = true
provisioner.ldapEduEntitlementProvisioner.metadata.0.dropdownValues = %Insert comma separated list of prefixes here%
provisioner.ldapEduEntitlementProvisioner.metadata.0.formElementType = dropdown
provisioner.ldapEduEntitlementProvisioner.metadata.0.name = md_grouper_eduEntitlement
provisioner.ldapEduEntitlementProvisioner.metadata.0.required = true
provisioner.ldapEduEntitlementProvisioner.metadata.0.showForFolder = true
provisioner.ldapEduEntitlementProvisioner.metadata.0.showForGroup = true
provisioner.ldapEduEntitlementProvisioner.numberOfEntityAttributes = 3
provisioner.ldapEduEntitlementProvisioner.numberOfGroupAttributes = 1
provisioner.ldapEduEntitlementProvisioner.numberOfMetadata = 1
provisioner.ldapEduEntitlementProvisioner.onlyProvisionPolicyGroups = true
provisioner.ldapEduEntitlementProvisioner.operateOnGrouperEntities = true
provisioner.ldapEduEntitlementProvisioner.operateOnGrouperGroups = true
provisioner.ldapEduEntitlementProvisioner.operateOnGrouperMemberships = true
provisioner.ldapEduEntitlementProvisioner.provisioningType = entityAttributes
provisioner.ldapEduEntitlementProvisioner.selectAllEntities = true
provisioner.ldapEduEntitlementProvisioner.selectGroups = false
provisioner.ldapEduEntitlementProvisioner.selectMemberships = true
provisioner.ldapEduEntitlementProvisioner.showAdvanced = true
provisioner.ldapEduEntitlementProvisioner.showAssigningProvisioning = true
provisioner.ldapEduEntitlementProvisioner.showProvisioningDiagnostics = true
provisioner.ldapEduEntitlementProvisioner.startWith = this is start with read only
provisioner.ldapEduEntitlementProvisioner.subjectSourcesToProvision = grouperSubjectSource
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.0.name = ldap_dn
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.0.showAdvancedAttribute = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.0.showAttributeCrud = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.0.update = false
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.1.name = uid
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.1.showAdvancedAttribute = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.1.showAttributeCrud = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectIdentifier0
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.1.update = false
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.2.multiValued = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.2.name = eduPersonEntitlement
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.2.showAdvancedAttribute = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.2.showAttributeCrud = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.2.showAttributeValueSettings = true
provisioner.ldapEduEntitlementProvisioner.targetEntityAttribute.2.update = true
provisioner.ldapEduEntitlementProvisioner.targetGroupAttribute.0.name = entitlementValue
provisioner.ldapEduEntitlementProvisioner.targetGroupAttribute.0.nullChecksInScript = true
provisioner.ldapEduEntitlementProvisioner.targetGroupAttribute.0.translateExpression = \u0024{grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_eduEntitlement')+ grouperProvisioningGroup.extension}
provisioner.ldapEduEntitlementProvisioner.targetGroupAttribute.0.translateExpressionType = translationScript
provisioner.ldapEduEntitlementProvisioner.updateEntities = true
provisioner.ldapEduEntitlementProvisioner.updateGroups = false
provisioner.ldapEduEntitlementProvisioner.userSearchBaseDn = %user search baseDN for your environment%
```

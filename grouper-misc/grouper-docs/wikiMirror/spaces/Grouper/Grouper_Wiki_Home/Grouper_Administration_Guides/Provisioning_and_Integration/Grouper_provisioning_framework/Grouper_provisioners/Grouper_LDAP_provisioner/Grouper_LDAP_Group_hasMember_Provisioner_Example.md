---
title: "Grouper LDAP Group 'hasMember' Provisioner Example"
space: Grouper
pageId: 28560235
version: 4
lastUpdated: 2023-05-17T16:18:41.148Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560235/Grouper+LDAP+Group+hasMember+Provisioner+Example
---

This provisioner is dependent on LDAP groups already existing. We manage the existence of LDAP groups with another provisioner that creates objectClass groupOfNames and populates the member attribute

Raw Configurations (End Result):

```java
provisioner.hasMember.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.hasMember.customizeEntityCrud = true
provisioner.hasMember.customizeGroupCrud = true
provisioner.hasMember.customizeMembershipCrud = true
provisioner.hasMember.deleteGroups = false
provisioner.hasMember.deleteMembershipsIfNotExistInGrouper = true
provisioner.hasMember.deleteMembershipsOnlyInTrackedGroups = false
provisioner.hasMember.entityAttributeValueCache0entityAttribute = eduPersonPrincipalName
provisioner.hasMember.entityAttributeValueCache0has = true
provisioner.hasMember.entityAttributeValueCache0source = grouper
provisioner.hasMember.entityAttributeValueCache0type = entityAttribute
provisioner.hasMember.entityAttributeValueCacheHas = true
provisioner.hasMember.entityMatchingAttribute0name = eduPersonPrincipalName
provisioner.hasMember.entityMatchingAttributeCount = 1
provisioner.hasMember.groupAttributeValueCache0groupAttribute = ldap_dn
provisioner.hasMember.groupAttributeValueCache0has = true
provisioner.hasMember.groupAttributeValueCache0source = target
provisioner.hasMember.groupAttributeValueCache0type = groupAttribute
provisioner.hasMember.groupAttributeValueCacheHas = true
provisioner.hasMember.groupDnType = flat
provisioner.hasMember.groupMatchingAttribute0name = o
provisioner.hasMember.groupMatchingAttributeCount = 1
provisioner.hasMember.groupMembershipAttributeName = hasMember
provisioner.hasMember.groupMembershipAttributeValue = entityAttributeValueCache0
provisioner.hasMember.groupRdnAttribute = cn
provisioner.hasMember.groupSearchBaseDn = ou=groups,dc=example,dc=edu
provisioner.hasMember.hasTargetEntityLink = true
provisioner.hasMember.hasTargetGroupLink = true
provisioner.hasMember.insertGroups = false
provisioner.hasMember.ldapExternalSystemConfigId = ldap
provisioner.hasMember.numberOfEntityAttributes = 2
provisioner.hasMember.numberOfGroupAttributes = 4
provisioner.hasMember.operateOnGrouperEntities = true
provisioner.hasMember.operateOnGrouperGroups = true
provisioner.hasMember.operateOnGrouperMemberships = true
provisioner.hasMember.provisioningType = groupAttributes
provisioner.hasMember.selectAllEntities = true
provisioner.hasMember.selectEntities = true
provisioner.hasMember.subjectSourcesToProvision = ldap
provisioner.hasMember.targetEntityAttribute.0.name = eduPersonPrincipalName
provisioner.hasMember.targetEntityAttribute.0.translateExpressionType = grouperProvisioningEntityField
provisioner.hasMember.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField = subjectId
provisioner.hasMember.targetEntityAttribute.1.name = ldap_dn
provisioner.hasMember.targetGroupAttribute.0.name = ldap_dn
provisioner.hasMember.targetGroupAttribute.1.name = cn
provisioner.hasMember.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.hasMember.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = name
provisioner.hasMember.targetGroupAttribute.2.name = hasMember
provisioner.hasMember.targetGroupAttribute.3.name = o
provisioner.hasMember.targetGroupAttribute.3.translateExpressionType = grouperProvisioningGroupField
provisioner.hasMember.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField = idIndex
provisioner.hasMember.updateGroups = false
provisioner.hasMember.userSearchBaseDn = ou=people,dc=example,dc=edu
```

Creating with the starts with procedure:

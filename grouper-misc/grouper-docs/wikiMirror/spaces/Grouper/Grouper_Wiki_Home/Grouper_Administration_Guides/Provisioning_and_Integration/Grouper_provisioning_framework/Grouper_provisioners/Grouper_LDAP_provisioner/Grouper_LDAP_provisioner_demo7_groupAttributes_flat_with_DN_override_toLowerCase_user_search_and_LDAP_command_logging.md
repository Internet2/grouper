---
title: "Grouper LDAP provisioner demo7 groupAttributes flat with DN override, toLowerCase user search, and LDAP command logging"
space: Grouper
pageId: 28560264
version: 5
lastUpdated: 2022-07-27T18:58:27.282Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560264/Grouper+LDAP+provisioner+demo7+groupAttributes+flat+with+DN+override+toLowerCase+user+search+and+LDAP+command+logging
---

> The info on this page applies to Grouper 2.6 and above.

## Requirements

- Groups with attribute for memberships
- Membership attribute value is user DN
- Users looked up by lowercase netId which is a subjectIdentifier which is subjectAttribute0
- Flat (each Grouper group name translates to an LDAP RDN (CN) in an OU)
- DN's can be overridden so they can live outside of the normal flat OU

[Youtube demo](https://youtu.be/TvWCjOx3WrM)

## Config

```
provisioner.ldapTest.allowLdapGroupDnOverride = true
provisioner.ldapTest.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.ldapTest.deleteGroups = true
provisioner.ldapTest.deleteGroupsIfGrouperCreated = true
provisioner.ldapTest.deleteMemberships = true
provisioner.ldapTest.deleteMembershipsIfGrouperCreated = true
provisioner.ldapTest.groupDnType = flat
provisioner.ldapTest.groupSearchBaseDn = ou=Groups,dc=example,dc=edu
provisioner.ldapTest.hasTargetEntityLink = true
provisioner.ldapTest.hasTargetGroupLink = true
provisioner.ldapTest.insertGroups = true
provisioner.ldapTest.insertMemberships = true
provisioner.ldapTest.ldapExternalSystemConfigId = personLdap
provisioner.ldapTest.logAllObjectsVerbose = true
provisioner.ldapTest.logCommandsAlways = true
provisioner.ldapTest.numberOfEntityAttributes = 3
provisioner.ldapTest.numberOfGroupAttributes = 5
provisioner.ldapTest.operateOnGrouperEntities = true
provisioner.ldapTest.operateOnGrouperGroups = true
provisioner.ldapTest.operateOnGrouperMemberships = true
provisioner.ldapTest.provisioningType = groupAttributes
provisioner.ldapTest.selectAllEntities = true
provisioner.ldapTest.selectEntities = true
provisioner.ldapTest.selectGroups = true
provisioner.ldapTest.selectMemberships = true
provisioner.ldapTest.showAdvanced = true
provisioner.ldapTest.subjectSourcesToProvision = jdbc
provisioner.ldapTest.targetEntityAttribute.0.fieldName = name
provisioner.ldapTest.targetEntityAttribute.0.isFieldElseAttribute = true
provisioner.ldapTest.targetEntityAttribute.0.select = true
provisioner.ldapTest.targetEntityAttribute.0.translateToMemberSyncField = memberToId2
provisioner.ldapTest.targetEntityAttribute.1.isFieldElseAttribute = false
provisioner.ldapTest.targetEntityAttribute.1.matchingId = true
provisioner.ldapTest.targetEntityAttribute.1.name = uid
provisioner.ldapTest.targetEntityAttribute.1.searchAttribute = true
provisioner.ldapTest.targetEntityAttribute.1.select = true
provisioner.ldapTest.targetEntityAttribute.1.translateExpression = ${grouperProvisioningEntity.retrieveAttributeValueString('subjectIdentifier0').toLowerCase()}
provisioner.ldapTest.targetEntityAttribute.1.translateExpressionType = translationScript
provisioner.ldapTest.targetEntityAttribute.2.isFieldElseAttribute = false
provisioner.ldapTest.targetEntityAttribute.2.multiValued = true
provisioner.ldapTest.targetEntityAttribute.2.name = objectClass
provisioner.ldapTest.targetEntityAttribute.2.select = true
provisioner.ldapTest.targetEntityAttribute.2.translateExpressionType = staticValues
provisioner.ldapTest.targetEntityAttribute.2.translateFromStaticValues = person
provisioner.ldapTest.targetGroupAttribute.0.fieldName = name
provisioner.ldapTest.targetGroupAttribute.0.insert = true
provisioner.ldapTest.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.ldapTest.targetGroupAttribute.0.select = true
provisioner.ldapTest.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = name
provisioner.ldapTest.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.ldapTest.targetGroupAttribute.0.update = true
provisioner.ldapTest.targetGroupAttribute.1.insert = true
provisioner.ldapTest.targetGroupAttribute.1.isFieldElseAttribute = false
provisioner.ldapTest.targetGroupAttribute.1.name = cn
provisioner.ldapTest.targetGroupAttribute.1.select = true
provisioner.ldapTest.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = name
provisioner.ldapTest.targetGroupAttribute.1.update = true
provisioner.ldapTest.targetGroupAttribute.2.insert = true
provisioner.ldapTest.targetGroupAttribute.2.isFieldElseAttribute = false
provisioner.ldapTest.targetGroupAttribute.2.multiValued = true
provisioner.ldapTest.targetGroupAttribute.2.name = objectClass
provisioner.ldapTest.targetGroupAttribute.2.select = true
provisioner.ldapTest.targetGroupAttribute.2.translateExpressionType = staticValues
provisioner.ldapTest.targetGroupAttribute.2.translateFromStaticValues = posixGroup
provisioner.ldapTest.targetGroupAttribute.2.update = true
provisioner.ldapTest.targetGroupAttribute.3.insert = true
provisioner.ldapTest.targetGroupAttribute.3.isFieldElseAttribute = false
provisioner.ldapTest.targetGroupAttribute.3.matchingId = true
provisioner.ldapTest.targetGroupAttribute.3.name = gidNumber
provisioner.ldapTest.targetGroupAttribute.3.searchAttribute = true
provisioner.ldapTest.targetGroupAttribute.3.select = true
provisioner.ldapTest.targetGroupAttribute.3.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapTest.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField = idIndexString
provisioner.ldapTest.targetGroupAttribute.3.update = true
provisioner.ldapTest.targetGroupAttribute.4.isFieldElseAttribute = false
provisioner.ldapTest.targetGroupAttribute.4.membershipAttribute = true
provisioner.ldapTest.targetGroupAttribute.4.multiValued = true
provisioner.ldapTest.targetGroupAttribute.4.name = description
provisioner.ldapTest.targetGroupAttribute.4.translateFromMemberSyncField = memberToId2
provisioner.ldapTest.updateGroups = true
provisioner.ldapTest.userSearchBaseDn = ou=People,dc=example,dc=edu

```

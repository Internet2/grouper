---
title: "Grouper LDAP provisioner demo6 groupAttributes flat with DN override and troubleshooting"
space: Grouper
pageId: 28560088
version: 8
lastUpdated: 2026-07-01T05:36:23.664Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560088/Grouper+LDAP+provisioner+demo6+groupAttributes+flat+with+DN+override+and+troubleshooting
---

> The info on this page applies to Grouper 2.6 and above.

## Requirements

- Groups with attribute for memberships
- Membership attribute value is user DN
- Users looked up by netId which is a subjectIdentifier which is subjectAttribute0
- Flat (each Grouper group name translates to an LDAP RDN (CN) in an OU)
- DN's can be overridden so they can live outside of the normal flat OU

[Youtube demo](https://www.youtube.com/watch?v=I84miJMJmfo)

Note: probably should select minimal attributes for entities, i.e. do not select objectClass

## Configuration

Provisioner in grouper-loader.properties

```
provisioner.ldapGroupsWithOverride.allowLdapGroupDnOverride = true
provisioner.ldapGroupsWithOverride.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.ldapGroupsWithOverride.deleteGroups = true
provisioner.ldapGroupsWithOverride.deleteGroupsIfNotExistInGrouper = true
provisioner.ldapGroupsWithOverride.deleteMemberships = true
provisioner.ldapGroupsWithOverride.deleteMembershipsIfNotExistInGrouper = true
provisioner.ldapGroupsWithOverride.groupDnType = flat
provisioner.ldapGroupsWithOverride.groupSearchBaseDn = ou=GrouperGroups,dc=example,dc=edu
provisioner.ldapGroupsWithOverride.hasTargetEntityLink = true
provisioner.ldapGroupsWithOverride.hasTargetGroupLink = true
provisioner.ldapGroupsWithOverride.insertGroups = true
provisioner.ldapGroupsWithOverride.insertMemberships = true
provisioner.ldapGroupsWithOverride.ldapExternalSystemConfigId = personLdap
provisioner.ldapGroupsWithOverride.logAllObjectsVerbose = true
provisioner.ldapGroupsWithOverride.numberOfEntityAttributes = 3
provisioner.ldapGroupsWithOverride.numberOfGroupAttributes = 5
provisioner.ldapGroupsWithOverride.operateOnGrouperEntities = true
provisioner.ldapGroupsWithOverride.operateOnGrouperGroups = true
provisioner.ldapGroupsWithOverride.operateOnGrouperMemberships = true
provisioner.ldapGroupsWithOverride.provisioningType = groupAttributes
provisioner.ldapGroupsWithOverride.selectAllEntities = true
provisioner.ldapGroupsWithOverride.selectEntities = true
provisioner.ldapGroupsWithOverride.selectGroups = true
provisioner.ldapGroupsWithOverride.selectMemberships = true
provisioner.ldapGroupsWithOverride.showAdvanced = true
provisioner.ldapGroupsWithOverride.subjectSourcesToProvision = personLdapSource
provisioner.ldapGroupsWithOverride.targetEntityAttribute.0.fieldName = name
provisioner.ldapGroupsWithOverride.targetEntityAttribute.0.isFieldElseAttribute = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.0.select = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.0.translateToMemberSyncField = memberToId2
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.isFieldElseAttribute = false
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.matchingId = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.name = mail
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.searchAttribute = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.select = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.ldapGroupsWithOverride.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = attribute__subjectIdentifier0
provisioner.ldapGroupsWithOverride.targetEntityAttribute.2.isFieldElseAttribute = false
provisioner.ldapGroupsWithOverride.targetEntityAttribute.2.multiValued = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.2.name = objectClass
provisioner.ldapGroupsWithOverride.targetEntityAttribute.2.select = true
provisioner.ldapGroupsWithOverride.targetEntityAttribute.2.translateExpression = ${grouperUtil.toSet('person')}
provisioner.ldapGroupsWithOverride.targetEntityAttribute.2.translateExpressionType = translationScript
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.fieldName = name
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.insert = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.select = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.translateExpression = ${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), 'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup.name) + ',ou=GrouperGroups,dc=example,dc=edu')}
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.translateExpressionType = translationScript
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.ldapGroupsWithOverride.targetGroupAttribute.0.update = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.insert = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.isFieldElseAttribute = false
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.name = cn
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.select = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.translateExpression = ${edu.internet2.middleware.grouper.util.GrouperUtil.ldapConvertDnToSpecificValue(grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_ldapGroupDnOverride'), 'cn=' + edu.internet2.middleware.grouper.util.GrouperUtil.ldapEscapeRdnValue(grouperProvisioningGroup.name) + ',ou=GrouperGroups,dc=example,dc=edu'))}
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.translateExpressionType = translationScript
provisioner.ldapGroupsWithOverride.targetGroupAttribute.1.update = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.insert = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.isFieldElseAttribute = false
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.matchingId = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.name = gidNumber
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.searchAttribute = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.select = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = idIndexString
provisioner.ldapGroupsWithOverride.targetGroupAttribute.2.update = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.insert = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.isFieldElseAttribute = false
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.multiValued = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.name = objectClass
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.select = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.translateExpression = ${grouperUtil.toSet('top', 'posixGroup')}
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.translateExpressionType = translationScript
provisioner.ldapGroupsWithOverride.targetGroupAttribute.3.update = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.4.isFieldElseAttribute = false
provisioner.ldapGroupsWithOverride.targetGroupAttribute.4.membershipAttribute = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.4.multiValued = true
provisioner.ldapGroupsWithOverride.targetGroupAttribute.4.name = description
provisioner.ldapGroupsWithOverride.targetGroupAttribute.4.translateFromMemberSyncField = memberToId2
provisioner.ldapGroupsWithOverride.updateGroups = true
provisioner.ldapGroupsWithOverride.userSearchBaseDn = ou=People,dc=example,dc=edu
  
```

External system in grouper-loader.properties

```
ldap.personLdap.pass = *******
ldap.personLdap.searchResultHandlers = org.ldaptive.handler.DnAttributeEntryHandler,edu.internet2.middleware.grouper.ldap.ldaptive.GrouperRangeEntryHandler
ldap.personLdap.uiTestAttributeName = dc
ldap.personLdap.uiTestExpectedValue = example
ldap.personLdap.uiTestFilter = (dc=example)
ldap.personLdap.uiTestSearchDn = dc=example,dc=edu
ldap.personLdap.uiTestSearchScope = OBJECT_SCOPE
ldap.personLdap.url = ldap://localhost:389
ldap.personLdap.user = cn=admin,dc=example,dc=edu

```

Daemon jobs in grouper-loader.properties

```
otherJob.ldapGroupsWithOverrideFull.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob
otherJob.ldapGroupsWithOverrideFull.provisionerConfigId = ldapGroupsWithOverride
otherJob.ldapGroupsWithOverrideFull.quartzCron = 39 53 6 * * ?

changeLog.consumer.ldapGroupsWithOverrideIncremental.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.ldapGroupsWithOverrideIncremental.provisionerConfigId = ldapGroupsWithOverride
changeLog.consumer.ldapGroupsWithOverrideIncremental.publisher.class = edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer
changeLog.consumer.ldapGroupsWithOverrideIncremental.publisher.debug = false
changeLog.consumer.ldapGroupsWithOverrideIncremental.quartzCron = 0 * * * * ?
  
```

Subject source in subject.properties

```
subjectApi.source.myPeople.adapterClass = edu.internet2.middleware.grouper.subj.GrouperLdapSourceAdapter2_5
subjectApi.source.myPeople.attribute.0.name = uid
subjectApi.source.myPeople.attribute.0.translationType = sourceAttributeSameAsSubjectAttribute
subjectApi.source.myPeople.attribute.1.name = mail
subjectApi.source.myPeople.attribute.1.subjectIdentifier = true
subjectApi.source.myPeople.attribute.1.translationType = sourceAttributeSameAsSubjectAttribute
subjectApi.source.myPeople.attribute.2.name = name
subjectApi.source.myPeople.attribute.2.sourceAttribute = cn
subjectApi.source.myPeople.attribute.2.translationType = sourceAttribute
subjectApi.source.myPeople.attribute.3.name = description
subjectApi.source.myPeople.attribute.3.translation = ${subject_attribute__cn + ' (' +source_attribute__uid + ') - ' + source_attribute__businesscategory + ' - ' + source_attribute__edupersonaffiliation}
subjectApi.source.myPeople.attribute.3.translationType = translation
subjectApi.source.myPeople.extraAttributesFromSource = businessCategory, eduPersonAffiliation
subjectApi.source.myPeople.id = personLdapSource
subjectApi.source.myPeople.name = My LDAP
subjectApi.source.myPeople.numberOfAttributes = 4
subjectApi.source.myPeople.param.Description_AttributeType.value = description
subjectApi.source.myPeople.param.Name_AttributeType.value = name
subjectApi.source.myPeople.param.SubjectID_AttributeType.value = uid
subjectApi.source.myPeople.param.emailAttributeName.value = mail
subjectApi.source.myPeople.param.findSubjectByIdOnCheckConfig.value = true
subjectApi.source.myPeople.param.findSubjectByIdentifiedOnCheckConfig.value = true
subjectApi.source.myPeople.param.findSubjectByStringOnCheckConfig.value = true
subjectApi.source.myPeople.param.ldapServerId.value = personLdap
subjectApi.source.myPeople.param.netId.value = mail
subjectApi.source.myPeople.param.stringToFindOnCheckConfig.value = aa
subjectApi.source.myPeople.param.subjectIdToFindOnCheckConfig.value = aanderson
subjectApi.source.myPeople.param.subjectIdentifierToFindOnCheckConfig.value = aanderson@example.edu
subjectApi.source.myPeople.search.search.param.filter.value = (&(|(|(uid=%TERM%)(cn=*%TERM%*))(uid=%TERM%*))(objectclass=person))
subjectApi.source.myPeople.search.searchSubject.param.base.value = ou=People,dc=example,dc=edu
subjectApi.source.myPeople.search.searchSubject.param.filter.value = (uid=%TERM%)
subjectApi.source.myPeople.search.searchSubject.param.scope.value = SUBTREE_SCOPE
subjectApi.source.myPeople.search.searchSubjectByIdentifier.param.filter.value = (mail=%TERM%)
subjectApi.source.myPeople.searchAttribute.0.attributeName = description
subjectApi.source.myPeople.searchAttributeCount = 1
subjectApi.source.myPeople.sortAttribute.0.attributeName = name
subjectApi.source.myPeople.sortAttributeCount = 1
subjectApi.source.myPeople.types = person

```

---
title: "Grouper LDAP provisioner demo3 groupAttributes bushy subjectId"
space: Grouper
pageId: 28560340
version: 6
lastUpdated: 2026-07-01T05:35:46.668Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560340/Grouper+LDAP+provisioner+demo3+groupAttributes+bushy+subjectId
---

> The info on this page applies to Grouper 2.6 and above.

## Requirements

- Groups with attribute for memberships
- No users
- Bushy (each Grouper folder translates to an LDAP OU)
- Membership attribute value is subjectId

**Note: use "ou" instead of "OU" for folder rdn or the full sync will find meaningless updates on each run. A full sync with no work to do should find 0 changes.**

[Youtube demo](https://www.youtube.com/watch?v=SvcXb7koRYk)

## Configuration

Provisioner in grouper-loader.properties

```
provisioner.ldapPosixGroups.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.ldapPosixGroups.deleteGroups = true
provisioner.ldapPosixGroups.deleteGroupsIfNotExistInGrouper = true
provisioner.ldapPosixGroups.deleteMemberships = true
provisioner.ldapPosixGroups.deleteMembershipsIfNotExistInGrouper = true
provisioner.ldapPosixGroups.folderObjectClasses = organizationalUnit
provisioner.ldapPosixGroups.folderRdnAttribute = ou
provisioner.ldapPosixGroups.groupDnType = bushy
provisioner.ldapPosixGroups.groupSearchAllFilter = (&(objectClass=posixGroup)(gidNumber=*))
provisioner.ldapPosixGroups.groupSearchBaseDn = ou=Groups2,dc=example,dc=edu
provisioner.ldapPosixGroups.groupSearchFilter = (&(gidNumber=${targetGroup.retrieveAttributeValue('gidNumber')})(objectClass=posixGroup))
provisioner.ldapPosixGroups.hasTargetGroupLink = true
provisioner.ldapPosixGroups.insertGroups = true
provisioner.ldapPosixGroups.insertMemberships = true
provisioner.ldapPosixGroups.ldapExternalSystemConfigId = personLdap
provisioner.ldapPosixGroups.logAllObjectsVerbose = true
provisioner.ldapPosixGroups.numberOfGroupAttributes = 5
provisioner.ldapPosixGroups.operateOnGrouperGroups = true
provisioner.ldapPosixGroups.operateOnGrouperMemberships = true
provisioner.ldapPosixGroups.provisioningType = groupAttributes
provisioner.ldapPosixGroups.selectGroups = true
provisioner.ldapPosixGroups.selectMemberships = true
provisioner.ldapPosixGroups.showAdvanced = true
provisioner.ldapPosixGroups.subjectSourcesToProvision = personLdapSource
provisioner.ldapPosixGroups.targetGroupAttribute.0.fieldName = name
provisioner.ldapPosixGroups.targetGroupAttribute.0.insert = true
provisioner.ldapPosixGroups.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.ldapPosixGroups.targetGroupAttribute.0.select = true
provisioner.ldapPosixGroups.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapPosixGroups.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = name
provisioner.ldapPosixGroups.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.ldapPosixGroups.targetGroupAttribute.0.update = true
provisioner.ldapPosixGroups.targetGroupAttribute.1.insert = true
provisioner.ldapPosixGroups.targetGroupAttribute.1.isFieldElseAttribute = false
provisioner.ldapPosixGroups.targetGroupAttribute.1.name = cn
provisioner.ldapPosixGroups.targetGroupAttribute.1.select = true
provisioner.ldapPosixGroups.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapPosixGroups.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = extension
provisioner.ldapPosixGroups.targetGroupAttribute.1.update = true
provisioner.ldapPosixGroups.targetGroupAttribute.2.insert = true
provisioner.ldapPosixGroups.targetGroupAttribute.2.isFieldElseAttribute = false
provisioner.ldapPosixGroups.targetGroupAttribute.2.matchingId = true
provisioner.ldapPosixGroups.targetGroupAttribute.2.name = gidNumber
provisioner.ldapPosixGroups.targetGroupAttribute.2.searchAttribute = true
provisioner.ldapPosixGroups.targetGroupAttribute.2.select = true
provisioner.ldapPosixGroups.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapPosixGroups.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = idIndexString
provisioner.ldapPosixGroups.targetGroupAttribute.3.insert = true
provisioner.ldapPosixGroups.targetGroupAttribute.3.isFieldElseAttribute = false
provisioner.ldapPosixGroups.targetGroupAttribute.3.multiValued = true
provisioner.ldapPosixGroups.targetGroupAttribute.3.name = objectClass
provisioner.ldapPosixGroups.targetGroupAttribute.3.select = true
provisioner.ldapPosixGroups.targetGroupAttribute.3.translateExpression = ${grouperUtil.toSet('top', 'posixGroup')}
provisioner.ldapPosixGroups.targetGroupAttribute.3.translateExpressionType = translationScript
provisioner.ldapPosixGroups.targetGroupAttribute.3.update = true
provisioner.ldapPosixGroups.targetGroupAttribute.4.isFieldElseAttribute = false
provisioner.ldapPosixGroups.targetGroupAttribute.4.membershipAttribute = true
provisioner.ldapPosixGroups.targetGroupAttribute.4.multiValued = true
provisioner.ldapPosixGroups.targetGroupAttribute.4.name = description
provisioner.ldapPosixGroups.targetGroupAttribute.4.translateFromMemberSyncField = subjectId
provisioner.ldapPosixGroups.updateGroups = true

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
otherJob.ldapPosixGroupsFull.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob
otherJob.ldapPosixGroupsFull.provisionerConfigId = ldapPosixGroups
otherJob.ldapPosixGroupsFull.quartzCron = 41 7 4 * * ?

changeLog.consumer.ldapPosixGroupsIncremental.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.ldapPosixGroupsIncremental.provisionerConfigId = ldapPosixGroups
changeLog.consumer.ldapPosixGroupsIncremental.publisher.class = edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer
changeLog.consumer.ldapPosixGroupsIncremental.publisher.debug = false
changeLog.consumer.ldapPosixGroupsIncremental.quartzCron = 0 * * * * ?

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
subjectApi.source.myPeople.param.subjectIdentifierToFindOnCheckConfig.value = aanderson@example.com
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

---
title: "Grouper LDAP provisioner demo4 entityAttributes with group dn"
space: Grouper
pageId: 28560214
version: 8
lastUpdated: 2026-07-01T05:36:02.285Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560214/Grouper+LDAP+provisioner+demo4+entityAttributes+with+group+dn
---

> The info on this page applies to Grouper 2.6 and above.

## Requirements

- Entities with attribute for groups
- Groups need to be looked up
- Groups are bushy
- User attribute value is for memberships is the group dn
- Groups are looked up by gidNumber = idIndex
- Users are looked up by subjectId
- Users exist in LDAP and otherwise (besides memberships) shouldn't be edited

**Note: might want to schedule the entity attribute incremental daemon to run after the group attribute incremental daemon, but before the change log temp to change log, e.g. 40 * * * * ?**

[Youtube demo](https://www.youtube.com/watch?v=qZU1-xTUbwM)

## Configuration

Provisioner in grouper-loader.properties

```
provisioner.ldapUserAttributes.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.ldapUserAttributes.deleteMemberships = true
provisioner.ldapUserAttributes.deleteMembershipsIfNotExistInGrouper = true
provisioner.ldapUserAttributes.groupSearchAllFilter = (&(objectClass=posixGroup)(gidNumber=*))
provisioner.ldapUserAttributes.groupSearchBaseDn = ou=Groups2,dc=example,dc=edu
provisioner.ldapUserAttributes.groupSearchFilter = (&(gidNumber=${targetGroup.retrieveAttributeValue('gidNumber')})(objectClass=posixGroup))
provisioner.ldapUserAttributes.hasTargetEntityLink = true
provisioner.ldapUserAttributes.hasTargetGroupLink = true
provisioner.ldapUserAttributes.insertMemberships = true
provisioner.ldapUserAttributes.ldapExternalSystemConfigId = personLdap
provisioner.ldapUserAttributes.logAllObjectsVerbose = true
provisioner.ldapUserAttributes.numberOfEntityAttributes = 3
provisioner.ldapUserAttributes.numberOfGroupAttributes = 2
provisioner.ldapUserAttributes.operateOnGrouperEntities = true
provisioner.ldapUserAttributes.operateOnGrouperGroups = true
provisioner.ldapUserAttributes.operateOnGrouperMemberships = true
provisioner.ldapUserAttributes.provisioningType = entityAttributes
provisioner.ldapUserAttributes.selectAllEntities = true
provisioner.ldapUserAttributes.selectEntities = true
provisioner.ldapUserAttributes.selectGroups = true
provisioner.ldapUserAttributes.selectMemberships = true
provisioner.ldapUserAttributes.showAdvanced = true
provisioner.ldapUserAttributes.subjectSourcesToProvision = personLdapSource
provisioner.ldapUserAttributes.targetEntityAttribute.0.fieldName = name
provisioner.ldapUserAttributes.targetEntityAttribute.0.isFieldElseAttribute = true
provisioner.ldapUserAttributes.targetEntityAttribute.0.select = true
provisioner.ldapUserAttributes.targetEntityAttribute.0.translateToMemberSyncField = memberToId2
provisioner.ldapUserAttributes.targetEntityAttribute.1.isFieldElseAttribute = false
provisioner.ldapUserAttributes.targetEntityAttribute.1.matchingId = true
provisioner.ldapUserAttributes.targetEntityAttribute.1.name = uid
provisioner.ldapUserAttributes.targetEntityAttribute.1.searchAttribute = true
provisioner.ldapUserAttributes.targetEntityAttribute.1.select = true
provisioner.ldapUserAttributes.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.ldapUserAttributes.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectId
provisioner.ldapUserAttributes.targetEntityAttribute.2.isFieldElseAttribute = false
provisioner.ldapUserAttributes.targetEntityAttribute.2.membershipAttribute = true
provisioner.ldapUserAttributes.targetEntityAttribute.2.multiValued = true
provisioner.ldapUserAttributes.targetEntityAttribute.2.name = description
provisioner.ldapUserAttributes.targetEntityAttribute.2.translateFromGroupSyncField = groupToId2
provisioner.ldapUserAttributes.targetGroupAttribute.0.fieldName = name
provisioner.ldapUserAttributes.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.ldapUserAttributes.targetGroupAttribute.0.select = true
provisioner.ldapUserAttributes.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.ldapUserAttributes.targetGroupAttribute.1.isFieldElseAttribute = false
provisioner.ldapUserAttributes.targetGroupAttribute.1.matchingId = true
provisioner.ldapUserAttributes.targetGroupAttribute.1.name = gidNumber
provisioner.ldapUserAttributes.targetGroupAttribute.1.searchAttribute = true
provisioner.ldapUserAttributes.targetGroupAttribute.1.select = true
provisioner.ldapUserAttributes.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.ldapUserAttributes.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = idIndexString
provisioner.ldapUserAttributes.updateEntities = true
provisioner.ldapUserAttributes.userSearchAllFilter = (uid=*)
provisioner.ldapUserAttributes.userSearchBaseDn = ou=People,dc=example,dc=edu
provisioner.ldapUserAttributes.userSearchFilter = (uid=${targetEntity.retrieveAttributeValue('uid')})

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
changeLog.consumer.ldapUserAttributesIncremental.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.ldapUserAttributesIncremental.provisionerConfigId = ldapUserAttributes
changeLog.consumer.ldapUserAttributesIncremental.publisher.class = edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer
changeLog.consumer.ldapUserAttributesIncremental.publisher.debug = false
changeLog.consumer.ldapUserAttributesIncremental.quartzCron = 40 * * * * ?

otherJob.ldapUserAttributesFull.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob
otherJob.ldapUserAttributesFull.provisionerConfigId = ldapUserAttributes
otherJob.ldapUserAttributesFull.quartzCron = 19 9 5 * * ?

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

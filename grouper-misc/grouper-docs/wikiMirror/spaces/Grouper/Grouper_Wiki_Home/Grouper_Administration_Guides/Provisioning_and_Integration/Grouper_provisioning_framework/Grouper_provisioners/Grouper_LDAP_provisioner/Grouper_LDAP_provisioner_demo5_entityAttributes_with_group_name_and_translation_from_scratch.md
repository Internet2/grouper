---
title: "Grouper LDAP provisioner demo5 entityAttributes with group name and translation from scratch"
space: Grouper
pageId: 28559961
version: 9
lastUpdated: 2026-07-01T05:36:36.569Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559961/Grouper+LDAP+provisioner+demo5+entityAttributes+with+group+name+and+translation+from+scratch
---

> The info on this page applies to Grouper 2.6 and above.

## Requirements

- Entities with attribute for groups
- Groups are in Grouper and not necessarily in LDAP
- User attribute value is for memberships is the translated group name
  
  - Do not delete other values of that attribute outside of Grouper
- Users are looked up by subjectId
- Users exist in LDAP and otherwise (besides memberships) shouldn't be edited

**Note, you need Grouper 2.5.57+ or 2.6.0+**

[Youtube demo](https://www.youtube.com/watch?v=k2VN2VTJ6cE)

## Configuration and commands

Download git

```
wget https://github.com/Internet2/grouper/archive/refs/heads/GROUPER_2_6_BRANCH.zip
unzip GROUPER_2_6_BRANCH.zip
```

Build openldap container with data

```
cd grouper-GROUPER_2_6_BRANCH/grouper-misc/openldap-dinkel-grouper/
docker build -t openldap-dinkel-grouper .
```

Run openldap container with data

```
docker run -d -p 389:389 --name openldap-dinkel-grouper \
--mount type=bind,source='/mnt/c/Users/mchyzer-local/Downloads/grouperDemo/grouper-GROUPER_2_6_BRANCH/grouper-misc/openldap-dinkel-grouper/ldap-seed-data',target=/etc/ldap/prepopulate \
-e SLAPD_PASSWORD=secret -e SLAPD_CONFIG_PASSWORD=secret -e SLAPD_DOMAIN=example.edu openldap-dinkel-grouper
```

Connect to openldap

```
host: localhost:389
user: cn=admin,dc=example,dc=edu
pass: secret
```

Add an external system in grouper (grouper-loader.properties)

```
ldap.personLdap.pass = secret
ldap.personLdap.uiTestAttributeName = dc
ldap.personLdap.uiTestExpectedValue = example
ldap.personLdap.uiTestFilter = (dc=example)
ldap.personLdap.uiTestSearchDn = dc=example,dc=edu
ldap.personLdap.uiTestSearchScope = OBJECT_SCOPE
ldap.personLdap.url = ldap://localhost:389
ldap.personLdap.user = cn=admin,dc=example,dc=edu 
```

Subject source

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
subjectApi.source.myPeople.param.stringToFindOnCheckConfig.value = jo
subjectApi.source.myPeople.param.subjectIdToFindOnCheckConfig.value = aanderson
subjectApi.source.myPeople.param.subjectIdentifierToFindOnCheckConfig.value = aclark@example.edu
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

Provisioner in grouper-loader.properties

```
provisioner.ldapEntityEntitlements.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.ldapEntityEntitlements.deleteMemberships = true
provisioner.ldapEntityEntitlements.deleteMembershipsIfGrouperDeleted = true
provisioner.ldapEntityEntitlements.hasTargetEntityLink = true
provisioner.ldapEntityEntitlements.insertMemberships = true
provisioner.ldapEntityEntitlements.ldapExternalSystemConfigId = personLdap
provisioner.ldapEntityEntitlements.logAllObjectsVerbose = true
provisioner.ldapEntityEntitlements.numberOfEntityAttributes = 4
provisioner.ldapEntityEntitlements.numberOfGroupAttributes = 1
provisioner.ldapEntityEntitlements.operateOnGrouperEntities = true
provisioner.ldapEntityEntitlements.operateOnGrouperGroups = true
provisioner.ldapEntityEntitlements.operateOnGrouperMemberships = true
provisioner.ldapEntityEntitlements.provisioningType = entityAttributes
provisioner.ldapEntityEntitlements.selectAllEntities = true
provisioner.ldapEntityEntitlements.selectEntities = true
provisioner.ldapEntityEntitlements.selectMemberships = true
provisioner.ldapEntityEntitlements.showAdvanced = true
provisioner.ldapEntityEntitlements.subjectSourcesToProvision = personLdapSource
provisioner.ldapEntityEntitlements.targetEntityAttribute.0.fieldName = name
provisioner.ldapEntityEntitlements.targetEntityAttribute.0.isFieldElseAttribute = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.0.select = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.0.translateToMemberSyncField = memberToId2
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.isFieldElseAttribute = false
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.matchingId = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.name = uid
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.searchAttribute = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.select = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.translateExpressionType = grouperProvisioningEntityField
provisioner.ldapEntityEntitlements.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField = subjectId
provisioner.ldapEntityEntitlements.targetEntityAttribute.2.isFieldElseAttribute = false
provisioner.ldapEntityEntitlements.targetEntityAttribute.2.multiValued = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.2.name = objectClass
provisioner.ldapEntityEntitlements.targetEntityAttribute.2.translateExpression = ${grouperUtil.toSet('person')}
provisioner.ldapEntityEntitlements.targetEntityAttribute.2.translateExpressionType = translationScript
provisioner.ldapEntityEntitlements.targetEntityAttribute.3.isFieldElseAttribute = false
provisioner.ldapEntityEntitlements.targetEntityAttribute.3.membershipAttribute = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.3.multiValued = true
provisioner.ldapEntityEntitlements.targetEntityAttribute.3.name = description
provisioner.ldapEntityEntitlements.targetEntityAttribute.3.translateFromGroupSyncField = groupFromId2
provisioner.ldapEntityEntitlements.targetGroupAttribute.0.isFieldElseAttribute = false
provisioner.ldapEntityEntitlements.targetGroupAttribute.0.name = entitlementValue
provisioner.ldapEntityEntitlements.targetGroupAttribute.0.translateExpression = ${'urn:mace:ufl.edu:g:' + grouperProvisioningGroup.name}
provisioner.ldapEntityEntitlements.targetGroupAttribute.0.translateExpressionType = translationScript
provisioner.ldapEntityEntitlements.targetGroupAttribute.0.translateGrouperToGroupSyncField = groupFromId2
provisioner.ldapEntityEntitlements.updateEntities = true
provisioner.ldapEntityEntitlements.userSearchBaseDn = ou=People,dc=example,dc=edu
  
```

Daemon jobs in grouper-loader.properties

```
changeLog.consumer.ldapEntityEntitlementsIncremental.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.ldapEntityEntitlementsIncremental.provisionerConfigId = ldapEntityEntitlements
changeLog.consumer.ldapEntityEntitlementsIncremental.publisher.class = edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer
changeLog.consumer.ldapEntityEntitlementsIncremental.publisher.debug = false
changeLog.consumer.ldapEntityEntitlementsIncremental.quartzCron = 0 * * * * ?

otherJob.ldapEntityEntitlementsFull.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob
otherJob.ldapEntityEntitlementsFull.provisionerConfigId = ldapEntityEntitlements
otherJob.ldapEntityEntitlementsFull.quartzCron = 18 7 5 * * ?
  
```

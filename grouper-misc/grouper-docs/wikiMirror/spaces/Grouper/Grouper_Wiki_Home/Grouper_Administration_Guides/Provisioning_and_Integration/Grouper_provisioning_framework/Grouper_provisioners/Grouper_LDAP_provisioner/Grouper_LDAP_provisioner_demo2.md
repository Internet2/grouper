---
title: "Grouper LDAP provisioner demo2"
space: Grouper
pageId: 28560293
version: 8
lastUpdated: 2026-07-01T05:35:54.211Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560293/Grouper+LDAP+provisioner+demo2
---

> The info on this page applies to Grouper 2.6 and above.

Provision subjectId to an attribute in ldap. In this case, the posixGroups normally use the member attribute, but that attribute is for DNs, so if we are provisioning subjectId's, we can use the description attribute which is freeform.

## Configuration

grouper.properties (using the dinkel ldap with grouper schema)

```
ldap.personLdap.url = ldap://localhost:389
ldap.personLdap.user = cn=admin,dc=example,dc=edu
ldap.personLdap.pass = secret
```

grouper-loader.properties

```
otherJob.testLdapProvisionerFull.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob
otherJob.testLdapProvisionerFull.provisionerConfigId = testLdapProvisioner
otherJob.testLdapProvisionerFull.quartzCron = 0 0 4 * * ?
provisioner.testLdapProvisioner.class = edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
provisioner.testLdapProvisioner.createGroupDuringDiagnostics = true
provisioner.testLdapProvisioner.deleteGroupDuringDiagnostics = true
provisioner.testLdapProvisioner.deleteGroups = true
provisioner.testLdapProvisioner.deleteGroupsIfNotExistInGrouper = true
provisioner.testLdapProvisioner.deleteMemberships = true
provisioner.testLdapProvisioner.deleteMembershipsIfNotExistInGrouper = true
provisioner.testLdapProvisioner.groupDnType = flat
provisioner.testLdapProvisioner.groupSearchAllFilter = (objectClass=posixGroup)
provisioner.testLdapProvisioner.groupSearchBaseDn = ou=Groups,dc=example,dc=edu
provisioner.testLdapProvisioner.hasTargetGroupLink = true
provisioner.testLdapProvisioner.insertGroups = true
provisioner.testLdapProvisioner.insertMemberships = true
provisioner.testLdapProvisioner.ldapExternalSystemConfigId = personLdap
provisioner.testLdapProvisioner.logAllObjectsVerbose = true
provisioner.testLdapProvisioner.numberOfGroupAttributes = 5
provisioner.testLdapProvisioner.operateOnGrouperGroups = true
provisioner.testLdapProvisioner.operateOnGrouperMemberships = true
provisioner.testLdapProvisioner.provisioningType = groupAttributes
provisioner.testLdapProvisioner.selectAllGroupsDuringDiagnostics = true
provisioner.testLdapProvisioner.selectGroups = true
provisioner.testLdapProvisioner.selectMemberships = true
provisioner.testLdapProvisioner.showAdvanced = true
provisioner.testLdapProvisioner.showProvisioningDiagnostics = true
provisioner.testLdapProvisioner.subjectSourcesToProvision = jdbc
provisioner.testLdapProvisioner.targetGroupAttribute.0.fieldName = name
provisioner.testLdapProvisioner.targetGroupAttribute.0.insert = true
provisioner.testLdapProvisioner.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.testLdapProvisioner.targetGroupAttribute.0.select = true
provisioner.testLdapProvisioner.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.testLdapProvisioner.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = name
provisioner.testLdapProvisioner.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.testLdapProvisioner.targetGroupAttribute.0.update = true
provisioner.testLdapProvisioner.targetGroupAttribute.1.insert = true
provisioner.testLdapProvisioner.targetGroupAttribute.1.isFieldElseAttribute = false
provisioner.testLdapProvisioner.targetGroupAttribute.1.matchingId = true
provisioner.testLdapProvisioner.targetGroupAttribute.1.name = gidNumber
provisioner.testLdapProvisioner.targetGroupAttribute.1.searchAttribute = true
provisioner.testLdapProvisioner.targetGroupAttribute.1.select = true
provisioner.testLdapProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.testLdapProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = idIndex
provisioner.testLdapProvisioner.targetGroupAttribute.1.valueType = long
provisioner.testLdapProvisioner.targetGroupAttribute.2.insert = true
provisioner.testLdapProvisioner.targetGroupAttribute.2.isFieldElseAttribute = false
provisioner.testLdapProvisioner.targetGroupAttribute.2.name = cn
provisioner.testLdapProvisioner.targetGroupAttribute.2.select = true
provisioner.testLdapProvisioner.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.testLdapProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = name
provisioner.testLdapProvisioner.targetGroupAttribute.2.update = true
provisioner.testLdapProvisioner.targetGroupAttribute.3.insert = true
provisioner.testLdapProvisioner.targetGroupAttribute.3.isFieldElseAttribute = false
provisioner.testLdapProvisioner.targetGroupAttribute.3.multiValued = true
provisioner.testLdapProvisioner.targetGroupAttribute.3.name = objectClass
provisioner.testLdapProvisioner.targetGroupAttribute.3.select = true
provisioner.testLdapProvisioner.targetGroupAttribute.3.translateExpression = ${grouperUtil.toSet('top', 'posixGroup')}
provisioner.testLdapProvisioner.targetGroupAttribute.3.translateExpressionType = translationScript
provisioner.testLdapProvisioner.targetGroupAttribute.4.isFieldElseAttribute = false
provisioner.testLdapProvisioner.targetGroupAttribute.4.membershipAttribute = true
provisioner.testLdapProvisioner.targetGroupAttribute.4.multiValued = true
provisioner.testLdapProvisioner.targetGroupAttribute.4.name = description
provisioner.testLdapProvisioner.targetGroupAttribute.4.translateFromMemberSyncField = subjectId
provisioner.testLdapProvisioner.targetGroupAttribute.4.valueType = string
provisioner.testLdapProvisioner.testGroupName = test:testGroup
provisioner.testLdapProvisioner.testSubjectIdOrIdentifier = test.subject.0
provisioner.testLdapProvisioner.updateGroups = true

```

## Assign provisionable

## Run provisioner

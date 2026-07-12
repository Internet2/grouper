---
title: "Grouper LDAP provisioner v2.5 use case PA"
space: Grouper
pageId: 28560286
version: 22
lastUpdated: 2026-07-01T05:35:55.176Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560286/Grouper+LDAP+provisioner+v2.5+use+case+PA
---

## Provisioning config

TODO

PSPNG config (legacy)

```
changeLog.consumer.pspng_oneprod.groupSearchAttributes = cn,gidNumber,samAccountName,objectclass
changeLog.consumer.pspng_oneprod.userSearchFilter = employeeID=${subject.id}
changeLog.consumer.pspng_oneprod.allGroupsSearchFilter = objectclass=group
changeLog.consumer.pspng_oneprod.groupCreationLdifTemplate = dn: cn=${group.name}||cn: ${group.name}||objectclass: group||gidNumber: ${group.idIndex}
changeLog.consumer.pspng_oneprod.singleGroupSearchFilter = (&(objectclass=group)(gidNumber=${idIndex}))
changeLog.consumer.pspng_oneprod.groupSearchBaseDn = OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu
changeLog.consumer.pspng_oneprod.userSearchBaseDn = DC=one,DC=upenn,DC=edu
changeLog.consumer.pspng_oneprod.grouperIsAuthoritative = true
changeLog.consumer.pspng_oneprod.userSearchAttributes = dn,cn,uid,mail,samAccountName, uidNumber,objectclass,employeeID
changeLog.consumer.pspng_oneprod.ldapPoolName = oneProdAd
changeLog.consumer.pspng_oneprod.isActiveDirectory = true
changeLog.consumer.pspng_oneprod.memberAttributeValueFormat = ${ldapUser.getDn()}
changeLog.consumer.pspng_oneprod.memberAttributeName = member
changeLog.consumer.pspng_oneprod.type = edu.internet2.middleware.grouper.pspng.LdapGroupProvisioner
```

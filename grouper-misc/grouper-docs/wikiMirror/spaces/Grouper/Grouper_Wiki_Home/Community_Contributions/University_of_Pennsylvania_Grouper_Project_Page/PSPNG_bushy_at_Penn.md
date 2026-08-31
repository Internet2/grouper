---
title: "PSPNG bushy at Penn"
space: Grouper
pageId: 28544269
version: 7
lastUpdated: 2026-07-01T05:48:41.003Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544269/PSPNG+bushy+at+Penn
---

We are switching to bushy active directory at U Penn since the cn length of 64 does not let us provision our groups. This is also easier to delegate access. We will provision folders for apps that need it, or groups that need it.

Make sure you are pointing to an AD domain name with active/standby load balancing or to the primary node. Or there could be CNF conflict groups in AD created.

**Install and patch pspng** (see PSPNG at Penn)

**Configure LDAP connection**

```
#note the URL should start with ldap: or ldaps: if it is SSL.
#It should contain the server and port (optional if not default), and baseDn,
#e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
ldap.pennKiteAd.url = ldaps://someServer.upenn.edu:636

#optional, if authenticated
ldap.pennKiteAd.user = someUserName

#optional, if authenticated, note the password can be stored encrypted in an external file
ldap.pennKiteAd.pass = ************
```

Configure bushy provisioning in pspng

```
changeLog.consumer.pspng_activedirectoryFull.class = edu.internet2.middleware.grouper.pspng.PspChangelogConsumerShim
changeLog.consumer.pspng_activedirectoryFull.type = edu.internet2.middleware.grouper.pspng.LdapGroupProvisioner
changeLog.consumer.pspng_activedirectoryFull.quartzCron = 0 * * * * ?
changeLog.consumer.pspng_activedirectoryFull.ldapPoolName = pennKiteAd
changeLog.consumer.pspng_activedirectoryFull.grouperIsAuthoritative = true
changeLog.consumer.pspng_activedirectoryFull.isActiveDirectory = true
changeLog.consumer.pspng_activedirectoryFull.memberAttributeName = member
changeLog.consumer.pspng_activedirectoryFull.memberAttributeValueFormat = ${ldapUser.getDn()}
changeLog.consumer.pspng_activedirectoryFull.groupSearchBaseDn = OU=GrouperFull,OU=LocalAuth,DC=kite,DC=upenn,DC=edu
changeLog.consumer.pspng_activedirectoryFull.allGroupsSearchFilter = objectclass=group
changeLog.consumer.pspng_activedirectoryFull.grouperIsAuthoritative = true
changeLog.consumer.pspng_activedirectoryFull.singleGroupSearchFilter = (&(objectclass=group)(gidNumber=${idIndex}))
changeLog.consumer.pspng_activedirectoryFull.groupCreationLdifTemplate = dn: ${utils.bushyDn(group.name, "cn", "ou")}||cn: ${group.extension}||objectclass: group||gidNumber: ${group.idIndex}
changeLog.consumer.pspng_activedirectoryFull.userSearchBaseDn = DC=kite,DC=upenn,DC=edu
changeLog.consumer.pspng_activedirectoryFull.userSearchFilter = employeeID=${subject.id}
changeLog.consumer.pspng_activedirectoryFull.userSearchAttributes = dn,cn,uid,mail,samAccountName, uidNumber,objectclass,employeeID
changeLog.consumer.pspng_activedirectoryFull.groupSearchAttributes = cn,gidNumber,samAccountName,objectclass
```

**Assign provision_to and pspng_activedirectoryFull to folders**:

**Run the pspng for this target (see PSPNG documentation). See Jira OU in LDAP**

**Look at one group**:

This is the next generation of the Grouper Provisioning System, aka PSP-NG.

Its general structure should be ready to provision various targets, but its current
implementation is limited to provisioning LDAP targets like:
  -(Unix) LDAP Groups: (GroupOfUniqueNames, GroupOfNames, PosixGroup)
  -Active Directory Groups
  -LDAP Attributes (like eduPersonEntitlement)

CONFIGURATION:
Configuration is done via the grouper-loader.properties files, with a paragraph for each
provisioning destination, as well as an additional paragraph that enables and configures
FullSync operation.

There are several configuration options documented in the following spreadsheet, but most 
are optional:
https://docs.google.com/spreadsheets/d/1FenN3hICohYR6cvr8Zxuk11VNyt82clvuouZQHgTT-w
(this spreadsheet needs to move to the Grouper Wiki)

GROUP SELECTION:
Groups can be enabled and disabled based on Group or Stem information and attributes. By default,
two multivalued attributes are used -- etc:attribute:userData:provision_to and 
...:do_not_provision_to -- where the provisioner name is listed as values to enable or disable
downstream provisioning.  Beyond this default, the group-selecting filter is a JEXL expression
and can utilize Group information and Stem & Group attributes.

EXAMPLE CONFIGURATIONS:

GROUP OF UNIQUE NAMES:
changeLog.consumer.pspng_groupOfUniqueNames.class = edu.internet2.middleware.grouper.pspng.PspChangelogConsumerShim
changeLog.consumer.pspng_groupOfUniqueNames.type = edu.internet2.middleware.grouper.pspng.LdapGroupProvisioner
changeLog.consumer.pspng_groupOfUniqueNames.quartzCron = 0 * * * * ?
changeLog.consumer.pspng_groupOfUniqueNames.ldapPoolName = opendj
changeLog.consumer.pspng_groupOfUniqueNames.memberAttributeName = uniqueMember
changeLog.consumer.pspng_groupOfUniqueNames.memberAttributeValueFormat = ${ldapUser.getDn()}
changeLog.consumer.pspng_groupOfUniqueNames.groupSearchBaseDn = ou=grouper,ou=groups,dc=example,dc=edu
changeLog.consumer.pspng_groupOfUniqueNames.allGroupsSearchFilter = objectclass=groupOfUniqueNames
changeLog.consumer.pspng_groupOfUniqueNames.singleGroupSearchFilter = (&(objectclass=groupOfUniqueNames)(cn=${group.name}))
changeLog.consumer.pspng_groupOfUniqueNames.groupCreationLdifTemplate = dn: cn=${group.name}||cn: ${group.name}||objectclass: groupOfUniqueNames
changeLog.consumer.pspng_groupOfUniqueNames.userSearchBaseDn = cn=users,dc=example,dc=edu
changeLog.consumer.pspng_groupOfUniqueNames.userSearchFilter = uid=${subject.id}

POSIX GROUPS:
changeLog.consumer.pspng_posixGroup.class = edu.internet2.middleware.grouper.pspng.PspChangelogConsumerShim
changeLog.consumer.pspng_posixGroup.type = edu.internet2.middleware.grouper.pspng.LdapGroupProvisioner
changeLog.consumer.pspng_posixGroup.quartzCron = 0 * * * * ?
changeLog.consumer.pspng_posixGroup.ldapPoolName = opendj
changeLog.consumer.pspng_posixGroup.memberAttributeName = memberUid
changeLog.consumer.pspng_posixGroup.memberAttributeValueFormat = ${ldapUser.getStringValue("uid")}
changeLog.consumer.pspng_posixGroup.groupSearchBaseDn = ou=grouper-posix,ou=groups,dc=example,dc=edu
changeLog.consumer.pspng_posixGroup.allGroupsSearchFilter = objectclass=posixGroup
changeLog.consumer.pspng_posixGroup.singleGroupSearchFilter = (&(objectclass=posixGrouper)(cn=${group.name}))
# Obviously, gidNumber should be based on a grouper-group attribute
changeLog.consumer.psong_posixGroup.groupCreationLdifTemplate = dn: cn=posix-${group.name}||cn: posix-${group.name}||objectclass: posixGroup||objectclass: groupOfNames||gidNumber: ${new(java.util.Random).nextInt()}
changeLog.consumer.pspng_posixGroup.userSearchBaseDn = cn=users,dc=example,dc=edu
changeLog.consumer.pspng_posixGroup.userSearchFilter = uid=${subject.id}

ACTIVE DIRECTORY GROUPS
changeLog.consumer.pspng_activedirectory.class = edu.internet2.middleware.grouper.pspng.PspChangelogConsumerShim
changeLog.consumer.pspng_activedirectory.type = edu.internet2.middleware.grouper.pspng.LdapGroupProvisioner
changeLog.consumer.pspng_activedirectory.quartzCron = 0 * * * * ?
changeLog.consumer.pspng_activedirectory.ldapPoolName = active_directory
changeLog.consumer.pspng_activedirectory.memberAttributeName = member
changeLog.consumer.pspng_activedirectory.memberAttributeValueFormat = ${ldapUser.getDn()}
changeLog.consumer.pspng_activedirectory.groupSearchBaseDn = ou=grouper,ou=groups,dc=example,dc=edu
changeLog.consumer.pspng_activedirectory.allGroupsSearchFilter = objectclass=group
changeLog.consumer.pspng_activedirectory.singleGroupSearchFilter = (&(objectclass=group)(cn=${group.name}))
changeLog.consumer.pspng_activedirectory.groupCreationLdifTemplate = dn: cn=${group.name}||cn: ${group.name}||objectclass: group
changeLog.consumer.pspng_activedirectory.userSearchBaseDn = cn=users,dc=example,dc=edu
changeLog.consumer.pspng_activedirectory.userSearchFilter = samAccountName=${subject.id}


USER ATTRIBUTES
changeLog.consumer.pspng_attributes.class = edu.internet2.middleware.grouper.pspng.PspChangelogConsumerShim
changeLog.consumer.pspng_attributes.type = edu.internet2.middleware.grouper.pspng.LdapAttributeProvisioner
changeLog.consumer.pspng_attributes.quartzCron = 0 * * * * ?
changeLog.consumer.pspng_attributes.retryOnError = true
changeLog.consumer.pspng_attributes.ldapPoolName = opendj
changeLog.consumer.pspng_attributes.provisionedAttributeName = eduPersonEntitlement
changeLog.consumer.pspng_attributes.provisionedAttributeValueFormat = g:${group.name}
changeLog.consumer.pspng_attributes.userSearchBaseDn = cn=users,dc=example,dc=edu
changeLog.consumer.pspng_attributes.userSearchFilter = uid=${subject.id}


FULL SYNC
changeLog.psp.fullSync.class = edu.internet2.middleware.grouper.pspng.FullSyncStarter
changeLog.psp.fullSync.quartzCron = 0 0 5 * * ?
# This happens in the background, so should usually be enabled, and should _definitely_
# be enabled when new provisioners are added 
changeLog.psp.fullSync.runAtStartup = true


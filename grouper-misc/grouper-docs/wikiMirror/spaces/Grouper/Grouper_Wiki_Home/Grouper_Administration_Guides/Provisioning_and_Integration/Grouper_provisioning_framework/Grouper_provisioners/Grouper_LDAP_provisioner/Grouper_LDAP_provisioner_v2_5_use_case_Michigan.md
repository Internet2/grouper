---
title: "Grouper LDAP provisioner v2.5 use case Michigan"
space: Grouper
pageId: 28560348
version: 12
lastUpdated: 2026-07-01T05:35:44.665Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560348/Grouper+LDAP+provisioner+v2.5+use+case+Michigan
---

There are two LDAP use cases from University of Michigan that will be ready in Grouper v2.5.40

## ***Outdated wiki***

## To do for 2.5.42

1. Directory for pems to get added to trust store
2. Incrementals based on your current config
3. Update the labels and descriptions in LDAP config to be more descriptive (describe entity link and translations better)
4. Compare provisioning configs and see what difference is
5. Diagnostics page?
6. Add example filter for "entity search filter" / "entity search all" (the "group search" equivalents have examples) in the provisioning setup

## Provision to AD, users exist

Two use cases:

1. Software distribution management (dozens of groups)
2. Printing (couple hundred groups)

| Category | Requirement | Description |
| --- | --- | --- |
| **Memberships** | provisioningType | groupAttributes |
| **Entities** | entity link | look up entities to get DN to use in group members attribute value |
|  | ldap is not subject source | subject source is somewhere else |
|  | should include all subjects | do not create subjects not found, should not error out. full sync will fix |
|  | there is no eligibility group | provision all subjects |
|  | select which sources to provision |  |
| **Subjects** | subject link | look up subjects to get netId |
|  | try to use subjectIdentifer0 | if the grouper member table has subject identifier0, use that, otherwise resolve the subject |
|  | does USDU update subject identifier0? | check that USDU updates that value. or is it updated during provisioning from members table? |
|  | do not provision subjects without a netId | fixed in full sync |
| **Groups** | grouper provisions to one OU | specify an OU for flat provisioning |
|  | cn is group name | cn is group name |
|  | if group name is more than 64, then skip | this should not be marked as error since it shouldnt retry. will try at next full sync? |
|  | groups and folders will be selected for provisioning |  |
|  | groups can have no members or can have no member attribute |  |
|  | delete groups in ldap which arent in grouper |  |
|  | group attributes: objectClass, dn, cn, member,, sAMAccountName |  |
|  | group uuid mapped to ldap attribute umichDirectoryID | instead of idIndex |

## Provision to difference AD, not all users exist

Similar to above but create users that dont exist

| Category | Requirement | Description |
| --- | --- | --- |
| **Entities** | createIfNotFound | create entities that dont exist |
|  | create in one OU | specify an OU to create subjects in |
|  | attributes to use when creating | netId, name?, email?  These are from subject source? |

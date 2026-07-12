---
title: "Initializing Administration of Privileges"
space: Grouper
pageId: 28545234
version: 19
lastUpdated: 2026-07-01T05:47:35.052Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545234/Initializing+Administration+of+Privileges
---

> The **wheel group** is a long-standing core feature, present in all currently supported releases. The settings below live in `grouper.properties` (defaults ship in `grouper.base.properties`).
> 
>  **Privileges:** bootstrapping the wheel group runs as **GrouperSystem** (GrouperShell acts as GrouperSystem / root). Editing `grouper.properties` requires server-side file access and a Grouper restart. Once the wheel group is established, its members can act with root-like privileges and manage further wheel members from the UI or GrouperShell.

 

## Overview

 GrouperSystem is the root-like principal used to manage assignment of privileges in Grouper. In addition to GrouperSystem, externally authenticated members of the **wheel group** can choose when to act with root-like privileges.

 If you've enabled the wheel group, you must create it and add members. GrouperShell (GSH) acts as GrouperSystem and can bootstrap the necessary folder (naming stem), group, and memberships.

 

## Enabling the wheel group

 The wheel group is enabled and named in `grouper.properties`:

 
```text
# A wheel group allows you to enable non-GrouperSystem subjects to act
# like a root user when interacting with the registry.
groups.wheel.use                      = true

# Set to the name of the group you want to treat as the wheel group.
# The members of this group will be treated as root-like users.
groups.wheel.group                    = etc:sysadmingroup

```

 

## Automatically creating the wheel group

 By default, Grouper auto-creates system groups (including the wheel group) on startup when the configuration check is enabled. This is controlled in `grouper.properties`:

 
```text
configuration.autocreate.system.groups = true

```

 

## Using GrouperShell to create the wheel group

 To create the wheel group manually using GrouperShell:

 
```text
gsh 0% addRootStem("etc", "Grouper Administration")
stem: name='etc' displayName='Grouper Administration' uuid='f7687876-2c94-4635-997c-f2793fb8152d'
gsh 1% addGroup("etc", "sysadmingroup", "SysAdmin Group")
group: name='etc:sysadmingroup' displayName='Grouper Administration:SysAdmin Group' uuid='6f77fb36-b466-481a-84a7-7af609f1ad09'

```

 

## Adding members to the wheel group

 Whether you've set the wheel group to be automatically created, or you've used GrouperShell to create it, you'll need to add members to the wheel group. Once the wheel group is established and things are working, the person designated as wheel can use the UI or GrouperShell to manage other wheel members. Here is an example using GrouperShell:

 
```text
gsh 0% addMember("etc:sysadmingroup", "SD00125")
true

```

 In this example "SD00125" is the subjectId of a person, as determined outside of GSH by, in this case, an LDAP query to a directory that acts as a subject source to Grouper:

 
```text
% ldapsearch -b dc=kitn,dc=edu uid=tbarton
dn: kitnEduPersonRegId=SD00125,ou=people,dc=kitn,dc=edu
objectClass: top
objectClass: person
objectClass: inetOrgPerson
objectClass: kitnEduPerson
kitnEduPersonRegId: SD00125
cn: Barton, Tom
sn: Barton
description: Professor, Mathematics
uid: tbarton

```

 

## See also

 [Grouper glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary)

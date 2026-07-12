---
title: "Grouper rules privileges inheritance on UI"
space: Grouper
pageId: 28548700
version: 21
lastUpdated: 2026-07-01T05:43:46.732Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548700/Grouper+rules+privileges+inheritance+on+UI
---

In Grouper 2.3+ there is support for privileges inheritance in the new UI. Inherited privileges were previously available by assigning attributes in a specific way or by using GSH. Note: inherited privileges are also displayed in the normal privilege screens. If they are removed from the object privilege screen, they will be automatically reassigned within a period of time (e.g. within a day, when the rule daemon runs which is configurable).

> Grouper Privileges (ADMIN, UPDATE, READ, VIEW, etc.) are defined in the [Grouper Glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary).

There are 7 screens to control and view inherited privileges.

### Patch improvements

In grouper newer than these patches:

grouper_v2_3_0_api_patch_90  
grouper_v2_3_0_ui_patch_38

These Jiras are implemented:

[GRP-1663: inherited privileges should revoke those privileges to subobjects](https://bugs.internet2.edu/jira/browse/GRP-1663)  
[GRP-1664: do not add admin privileges to root or wheel when creating objects](https://bugs.internet2.edu/jira/browse/GRP-1664)  
[GRP-1665: do not add admin privileges to inherited admins](https://bugs.internet2.edu/jira/browse/GRP-1665)  
[GRP-1667: a folder inherited privilege should apply to the assigned folder](https://bugs.internet2.edu/jira/browse/GRP-1667)

### Screen: View or assign inherited privileges in a folder

On a folder screen, if you are an ADMIN (and you can manage inherited privileges, see below), you can click "More -> Privileges inherited to objects in folder"

Click "Add members" to add a new inherited privilege

Select a member and the type to "Assign to" (which could be multiple types at once)

You can delete direct inherited privileges (which are assigned to this folder). To delete inherited indirect entries, click on that folder and delete from there

### Screen: View inherited privileges that affect a group

If you are an ADMIN of a group and can view inherited privileges, pull a group up on the UI and click "More -> This group's privileges inherited from folders"

### Screen: View inherited privileges that affect a folder

If you are an ADMIN of a folder and can view inherited privileges, pull a folder up on the UI and click "More -> This folder's privileges inherited from ancestor folders"

### Screen: View inherited privileges that affect an attribute definition

If you are an ADMIN of an attribute definition and can view inherited privileges, pull an attribute definition up on the UI and click "More -> This attribute's privileges inherited from ancestor folders"

### Screen: View inherited privileges that affect a subject

If you can view inherited privileges, pull an entity up on the UI and click "More -> This subject's privileges inherited from folders"

### Screen: View inherited privileges assigned to members of a group

This is similar to seeing privileges assigned to a subject. Its a little confusing. This is not the privileges of this group that are affected by inherited privileges.

If you can view inherited privileges, pull a group you can VIEW up on the UI and click "More -> This subject's privileges inherited from folders"

### Screen: View all inherited privileges in registry

View all inherited privileges in the registry that you are allowed to see.

Click on Miscellaneous (if you can see inherited privs you will see this link)

Click "Inherited privileges"

### Privileges required to manage inherited privileges

This section describes who is allowed to view or assign inherited privileges.

In order to see rules configuration for inherited privileges on the UI, you need to be able to be an admin (stemAdmin) on a folder which is affected by the rule. If you want fewer people to be able to see the rule, you can adjust these in grouper.properties.

```
# require admin (GrouperSysAdmin or wheel group) to update inherited privileges
uiV2.privilegeInheritanceUpdateRequireAdmin = false

# require admin (GrouperSysAdmin or wheel group) to read inherited privileges
uiV2.privilegeInheritanceReadRequireAdmin = false

# require admin (GrouperSysAdmin or wheel group) to update inherited privileges
uiV2.privilegeInheritanceUpdateRequireGroup = 

# require admin (GrouperSysAdmin or wheel group) to read inherited privileges
uiV2.privilegeInheritanceReadRequireGroup = 

```

Note, you dont need to be able to read attributes on the assigned (parent or ancestor folder) to be able to see the privilege inheritance. You also do not need privileges on rule attributes. If you want to require rules attributes privileges set this in the grouper-ui.properties.

```
# if this is true you dont even need to be able to 
uiV2.privilegeInheritanceDoesntRequireRulesPrivileges = false
```

If that is set to false, then to see the inherited privileges, you need to be able to READ the two attributeDefs for rules type and attributes

You can control the global screen (note, there is no paging as of 2.3.0)

```
# if show miscellaneous link
uiV2.showMiscellaneousLink = true

# if show global inherited privileges link
uiV2.showGlobalInheritedPrivilegesLink = true
```

sfd

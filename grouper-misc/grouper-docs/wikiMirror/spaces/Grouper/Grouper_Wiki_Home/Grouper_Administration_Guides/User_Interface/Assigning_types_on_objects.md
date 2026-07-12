---
title: "Assigning types on objects"
space: Grouper
pageId: 28545122
version: 58
lastUpdated: 2026-07-12T15:26:39.482Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545122/Assigning+types+on+objects
---

### Overview

Grouper allows you to assign tags (called types) to objects such as folders and groups. There can be metadata on the assignment.

Grouper types helps support and clarify the usage of types as defined in the [Grouper Deployment Guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541813/Grouper+Deployment+Guide).

This is a more structured and consistent way of accomplishing what could also done through the [attribute framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework).

### **Attribute definitions**

| Definition | Assigned to | Purpose | Value | Cardinality |
| --- | --- | --- | --- | --- |
| grouperObjectTypeDef | folder, group | identify a group type | marker | Multi assign |
| grouperObjectTypeValueDef | folder assignment, group assignment | name/value pairs | String | Single assign, single valued |

### Attribute names

| Name | Definition | Value |
| --- | --- | --- |
| grouperObjectTypeMarker | grouperObjectTypeDef | <none> |
| grouperObjectTypeName | grouperObjectTypeValueDef | ref, basis, policy,etc, bundle, org, test, service, app. See the [TIER Grouper Deployment Guide](http://doi.org/10.26869/TI.25.1) for descriptions. |
| grouperObjectTypeDataOwner | grouperObjectTypeValueDef | e.g. Registrar's office owns this data |
| grouperObjectTypeMembersDescription | grouperObjectTypeValueDef | human readable description |
| grouperObjectTypeDirectAssignment | grouperObjectTypeValueDef | if this is directly assigned or inherited |
| grouperObjectTypeServiceName | grouperObjectTypeValueDef | name of the service that this app falls under |
| grouperObjectTypeOwnerStemId | grouperObjectTypeValueDef | if this is not a direct assignment, then this is the stem id where it is inherited from |

### Assign type on UI

On a folder or group, a menu item under the "more actions" button will say "Type." (Shown in screenshot below.) This "Type" option will only show if user can edit types.

Table should show which are assigned, if inherited or not. There is also the option to add more assignments. If assigned, call the keep inherited method below.

Note: implement the attributes like "Deprovisioning" used attributes

Group or Folder ADMINs can assign types

### View on UI

Note, if a user can view a group, they can view this attribute assignment from a high level.

On the group or folder screen show what type(s) this group is

Show the data owner and service description if applicable

Show this all in one paragraph. Show toolips on each type

As part of types, the loader summary should be moved from the loader "view" screen to this paragraph too. It should read as follows: This group is managed by loader group[testSqlSimple](https://grouperdemo.internet2.edu/grouper_v2_3/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=0f539d0778c846dfb154a6529252a9d2), last fully loaded on Sat Dec 01 06:00:04 UTC 2018 with summary: 167 total, 0 inserted, 5 deleted, 2 updated.

E.g. shows on group UI: Group types: ref, readonly. The data owner is: the registrar's office. The member description is: faculty, staff, temps.

If the type is "app", and it is an indirect assignment, and the folder which assigns it has a display extension of "Wiki", then followup with a sentence (note, the folder should be linkable to that folder): This is in the Wiki app.

If the type is "service", and it is an indirect assignment, and the folder which assigns it has a display extension of "Student systems", then followup with a sentence (note, the folder should be linkable to that folder): This is the Student systems service.

### Available Types

| Type | Owner type | Tooltip |
| --- | --- | --- |
| ref | group/folder | Reference groups are institutionally meaningful cohorts used in access policy. |
| basis | group/folder | Basis groups represent arcane codes or attributes from external systems are used generally in reference groups and not directly in access policy. |
| readonly | group/folder | Read-only groups should not have membership changes except by the process that manages the group; perhaps external from the central authorization system |
| policy | group/folder | Access policy groups are used by downstream systems to allow or deny users access to services or resources. |
| bundle | group/folder | Bundles are reference groups which aggregate multiple other reference groups. Reference groups are institutionally meaningful cohorts used in access policy. |
| security | group/folder | Security groups are collections of entities who have from access privilege on a group/folder/attribute, e.g. studentSystemAdmins. |
| org | group/folder | "Org" or organization groups or folders are delegated to and owned by organizations in the community. |
| test | group/folder | Test groups or folders are not used in production systems. They could be for dev, test, performance, etc environments. |
| app | group/folder | App groups or folders exist to be used in a specific application. |
| service | group/folder | A service is a collection of one or many apps that comprise of a service offered to users. |

### Screenshots

Use the "More actions" button to access Type.

List of available Type settings for a folder

Assigning a Type setting to a folder

Shows Type setting and specifies if inherited or not.   
*Note, we plan to add text that states that type assignments and metadata can be viewed by anyone who can VIEW a group.*

Type info is shown under the folder name to make it easily accessible.

The screenshots above show Types on folders. Similar Type configuration can be set on groups as well.

### Keep inherited attributes up do date

A daemon should work like [deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning) where inherited attributes are kept up to date.   
When an attribute is assigned, it should call the propagate method for this object and subobjects if a stem.

### To do now

- Put type name first, then direct/indirect
- When settings changed, it should use logic like [deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning) where it updates the hierarchy tree
- Types and metadata should be displayed on object page under the description (not in "more")

### To do later

- Source basis and ref metadata from loader
- Clarify the difference between app and service
- If there is a blank attribute, it should be unassigned
- Use this as a means to search for things

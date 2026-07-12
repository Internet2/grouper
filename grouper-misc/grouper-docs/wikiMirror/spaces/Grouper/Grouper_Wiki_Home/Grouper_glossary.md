---
title: "Grouper glossary"
space: Grouper
pageId: 28541893
version: 74
lastUpdated: 2026-07-12T15:26:02.134Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary
---

Terms with Grouper-specific meaning are defined below, along with other Grouper concepts. An understanding of these terms will enable you to take full advantage of all that Grouper has to offer.

Grouper [terminology used in the Grouper UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543683/UI+Terminology) differs from some of the terms defined below to help the UI to present group management tasks in a manner more readily understandable by non-technical users. The terminology used in developer and system administrator oriented documentation remains unchanged.

Other Grouper glossaries

| **TERM** | **DEFINITION** | UI Translation (where applicable) |
| --- | --- | --- |
| *Access Privileges* | Privileges that determine what a ***Subject*** can do with a ***Group***. They are:    - **ADMIN** - can assign access privileges and manage all group information, - **UPDATE** - can manage membership of the group (implies **VIEW**), - **READ** - can see the membership of the group (implies **VIEW**), and - **VIEW** - can see the group. - **GROUP_ATTR_READ** - can read attributes assigned to the group. Note that the subject must also have **ATTR_READ** privilege on the attributeDef. - **GROUP_ATTR_UPDATE** - can assign attributes to the group. Note that the subject must also have **ATTR_UPDATE** privilege on the attributeDef.      In addition, a group may have options for its members to: - **OPTIN** - can add self to the membership, and - **OPTOUT** - can remove self from membership. | Subject is a UI "entity" |
| ***Attribute*** | Grouper supports two broad categories of attributes:       1. Attributes used to attach metadata to various objects in the registry. For information see the [Attribute Framework documentation.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework)      2. A single-valued string associated with a ***Group*** or a ***Naming Stem***. By default, Grouper supports six of these attributes:    - **id** - a Grouper-assigned, globally unique identifier. - **extension**- the relative name of the group or naming stem within its parent naming stem; the contribution of a single element, such as a group or a naming stem, to the cumulative name. - **name** - used to facilitate searching for groups by name, it is a read-only string representation of the logical ordered pair of *(parent stem, extension)*. This attribute is system-maintained. The string representation of the *name* attribute is: *<parent stem>:<extension>*. - **displayExtension** - a displayed form of the extension. - **displayName**- used to facilitate searching for groups by the displayed name, it is a read-only string representation of the logical ordered pair of *(displayName of parent stem, displayExtension)*. This attribute is system-maintained. The string representation of the *displayName* attribute is: *<displayName of parent stem>:<displayExtension*>. - **description** - a description of the group or naming stem. | - id is the UI "UUID" - extension is the UI "ID" - name is the UI "ID path" - displayExtension is the UI "name" - displayName is the UI "path" |
| ***Composite Group*** | A ***Group*** whose ***Membership*** is determined by combining the membership lists of two other groups, without listing its members explicitly. These two groups are called its ***Factor Groups***. Three methods of combining the factor groups' memberships are supported:    - **union** - all subjects must be a member of one OR the other factor group,      e.g., Group Z = members of either Group X ***OR*** Group Y, or Z = X U Y. - **intersection** - all subjects that are members of the first factor group AND the second factor group,      e.g., Group Z = members of both Group X ***AND*** Group Y, or Z = X ∩ Y. - **relative complement** - all members of the first factor group that are NOT members of the second factor group.      e.g., Group Z = members of Group X ***AND NOT*** Group Y, or Z = X - Y. |  |
| ***Direct Membership*** | A ***Subject*** that is listed in the ***Membership*** list of a ***Group*** has a direct membership in the group. Also see ***Indirect Membership***. | Subject is a UI "entity" |
| ***Factor Group*** | A ***Group*** in combination (**union, intersection,** or **relative complement)** with that of another factor group, which defines the membership of a resulting ***Composite Group***. |  |
| ***Folder*** | A place to organize objects in Grouper, most commonly a place to contain groups. Also called a ***Stem*** or ***Naming Stem***. |  |
| ***Group*** | A list of ***Subjects*** having ***Membership*** in the group, together with other attributes about the group. A list can have zero or more entries. In Grouper, a list contains only subject references, and an attribute is a single-valued string. A group must be created in an existing ***Naming Stem (or just Stem)***. If a group is made a member, i.e., a ***Subgroup***, of another group, the members of the group will also be made members. By default, a Grouper group has:    - six naming ***Attributes***, - a **description** attribute, and - a **members** list.      This information model can be extended to include additional site-defined attributes and lists. | naming stem is a UI "folder" |
| *Group Math* | Any combination of groups for the purpose of creating another group based on the memberships of those groups. See ***Composite Group***. |  |
| ***Indirect Membership*** | A ***Subject*** that is a member of a ***Subgroup*** of a ***Group***, or a member of a ***Factor Group*** that contributes positively to a group's membership, has an indirect membership in the group. Also see ***Direct Membership***. |  |
| ***List*** | A multi-valued list of ***Subject*** references. The ***direct members*** of a group are the values of the group's **members** list. Lists are also used to identify which subjects have which ***Naming*** or ***Access Privileges***. |  |
| ***Member*** | Any ***Subject*** in the membership list of at least one group. Also, a ***Member*** of a ***Group*** is any ***Subject*** with a ***Direct*** or ***Indirect Membership*** in the ***Group***. |  |
| ***Membership*** | The direct-only, indirect-only, or direct plus indirect members of a ***Group***. A specific variety of membership is determined by context or configuration, i.e., the default User Interface allows the user to select among these three types of membership where appropriate. |  |
| ***Naming Privileges*** | These privileges determine what a ***Subject*** can do with a ***Naming Stem***. They are:    - **CREATE** - can create groups, attributes, and subfolders in the stem. - **ADMIN** - can create groups, attributes, and subfolders in the stem. Also can delete the stem or assign any privilege to any entity. - **STEM_ATTR_READ** - can read attributes assigned to the stem. Note that the subject must also have **ATTR_READ** privilege on the attributeDef. - **STEM_ATTR_UPDATE** - can assign attributes to the stem. Note that the subject must also have **ATTR_UPDATE** privilege on the attributeDef. | Naming privileges are now referred to as Creation privileges. |
| ***Naming Stem*** | A string that forms the leading part of a ***Group's*** name. By linking the ability to create groups to a specified naming stem (via the **CREATE** privilege), the possibility that different groups can be given the same name is substantially reduced, and the name of each group can be made to reflect something about the authority under which it was created. Also called a **Folder**   ...see Examples below. | Stem is a UI "folder" |
| ***Stem*** | A synonym for a ***Naming Stem***or***Folder*** | Stem is a UI "folder" |
| ***Subgroup*** | A ***Group*** that is a ***Direct Member*** of another group. |  |
| ***Subject*** | An abstraction of any object whose ***Memberships*** are to be managed by Grouper. Most Grouper deployments will manage subjects that represent people and groups, but computers, accounts, services, or any other type of object maintained in a back-end identity store may be presented as subjects to Grouper by use of the Subject API. | Subject is a UI "Entity" |
| ***Subject Source*** | One of the configured (generally external) places where subjects (entities) can be looked up and added to groups or assigned permissions. Each source has an unchanging and unique ID. Subjects have ID's also, but the source itself has an ID ("subject source id"), which is used in web services and UI imports and other places. [Configure a subject source.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548079/Connecting+to+a+subject+source) |  |
| ***Subject*** Id | This is an unchanging (generally opaque) identifier that will be stored in the Grouper database (along with subject source id) to represent each subject when it is used (e.g. added to a group or assigned permissions). This ID must be unique in the source. Note: if removing an unresolvable subject from a group, this is the only way to reference the subject. |  |
| ***Subject*** Identifier | This is an attribute of the subject which can be used to identify the subject. Note, the Subject ID should not also be a Subject Identifier. This is not used in the Grouper database to lookup users, and can change. Examples of this are: netID and EPPN. |  |
| ***Type*** | There are a few uses for this term in Grouper.    - **Type** is used in Grouper 2.4 patch Grouper 2.4 patch (grouper_v2_4_0_api_patch_13) to allow you to tag an object**. [Assigning types on objects](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545122/Assigning+types+on+objects)** - **Group Type** - (deprecated in Grouper 2.2 and above, but functionality still supported using the [Attribute Framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework).) Each ***Group*** has one or more group types associated with it. The Grouper distribution contains support for a single group type called "**base**", but sites may register additional types, together with the attributes and lists associated with them, within their Grouper installation. Doing so enables management of groups with a richer information model or a more diverse set of information models. Note, the addition of "Role" to Grouper adds a field on Groups called typeOfGroup which can be "group", "role" (group which can have permissions assigned), and "entity" (group object which cannot have members and is used for example as an application principal - **Subject Type** - the Subject API v0.2.1 that Grouper 1.0 relies on uses the notion of a subject type, such as "person", "group", or "computer", etc. |  |

## Examples

#### **Step 1: Create a Root Folder**

In the example below, a root folder is first created. Note: creating a folder is required prior to the creation of any groups.

***Naming Stem*** **uofc**

| ***attribute*** | ***value*** |
| --- | --- |
| *folder* | empty |
| *extension* | uofc |
| *displayExtension* | The University Of Chicago |
| *name* | uofc |
| *displayName* | The University Of Chicago |

#### **Step 2: Create a Group**

Next, a group may be created using the "uofc" folder.

***Group*** **uofc:exec_council**

| ***attribute*** | ***value*** |
| --- | --- |
| *folder* | uofc |
| *extension* | exec_council |
| *displayExtension* | Executive Council |
| *name* | uofc:exec_council |
| *displayName* | The University of Chicago:Executive Council |

#### **Step 3: Create a Subordinate Folder and Group**

Name and displayName values propagate down through subordinate naming stems, e.g the Biological Sciences Division within U of C:

***Folder*** **uofc:bsd**

| ***attribute*** | ***value*** |
| --- | --- |
| *folder* | uofc |
| *extension* | bsd |
| *displayExtension* | Biological Sciences Division |
| *name* | uofc:bsd |
| *displayName* | The University Of Chicago:Biological Sciences Division |

Again, a group is created, e.g., the Enterprise Information Systems staff, with the above naming stem, and is displayed as follows:

***Group*** **uofc:bsd:eis_staff**

| ***attribute*** | ***value*** |
| --- | --- |
| *folder* | uofc:bsd |
| *extension* | eis_staff |
| *displayExtension* | Enterprise Information Systems staff |
| *name* | uofc:bsd:eis_staff |
| *displayName* | The University Of Chicago:Biological Sciences Division:Enterprise Information Systems staff |

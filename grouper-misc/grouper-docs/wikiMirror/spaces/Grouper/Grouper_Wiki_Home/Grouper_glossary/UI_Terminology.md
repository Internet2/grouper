---
title: "UI Terminology"
space: Grouper
pageId: 28543683
version: 22
lastUpdated: 2026-07-01T05:49:14.839Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543683/UI+Terminology
---

## Terminology in the Grouper user interface

See also the [main glossary](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541893/Grouper+glossary).

The table below maps Grouper user interface terminology by category, showing the old term (used prior to `v1.3`), the current term, and a description.

> The terms in the "Current term" column have been used in the Grouper user interface since `v1.3` and remain current (confirmed in `v7`). The "Old term" column predates `v1.3` and is kept for historical reference.

| **Category** | **Old term (prior to v1.3)** | **Current term** | **Definition / description** |
| --- | --- | --- | --- |
| UI labels | Privilegees | Entities with privileges | The entities that hold a given privilege on a folder or group. |
| Subject | Entity | An abstract item that may be a member of a group. The two most common types of entities are "person" and "group". (Other entity types may also be used, for example to describe computers or applications.) |
| is a direct privilegee | has direct privileges | Holds the privilege directly (assigned to this entity). |
| is an indirect privilegee | has indirect privileges | Holds the privilege indirectly, through membership in a group that has the privilege. |
| Extension | ID | The system identifier for a folder or group (the last component of the path). It is shown in the UI and used in scripting and web services. |
| Name | ID path | The full system path: the concatenation of the folder hierarchy and the object's ID that uniquely identifies it. It is shown in the UI and used when moving or copying objects, in scripting, and in web services. |
| Display extension | Name | The display name of the folder or group, shown when browsing or searching. |
| Display name | Path | The display path: the concatenation of the display names of the folders (and group) that lead to the object's unique location. |
| Hierarchy | stem [conceptual] | Folder | A fundamental unit (container) of the hierarchy that can have a parent (a folder or the root) and children (folders or groups). |
| group | group | A type of entity made up of members. |
| Manage Stem | Manage folders | The "Manage folders" panel, where you create or edit the folders within the hierarchy and add groups to it. |
| Hierarchy privilege | stem [privilege] | Create folder | The ability to create child folders or branches in the hierarchy. |
| Create | Create group | Add or create the name for a new group at this folder (location) in the hierarchy. The entity that creates a group is given Admin rights to it by default. This does not provide access to manage the group (add membership or edit attributes). |
| Stem privilege | Creation privileges | The folder privileges (such as Create and Admin) that allow an entity to create groups and subfolders in a folder. |
| Navigation | saved subjects | Entity workspace | A session-specific area where you can store entities you will need, for example to build compound (composite) groups. |
| Saved groups | Group workspace | A session-specific area where you can store groups you will need, for example to build compound (composite) groups. |
| — | Folder workspace | A session-specific area where you can store folders you will need, for example as the destination when moving or copying an object. |
| Search subjects | Search | Search for entities, groups, or folders. |
| Administrative | grouperAll | EveryEntity | If a privilege is granted to EveryEntity, it applies to all entities (subjects). Note: this is not recommended and should generally be granted to a reference group instead. |
| GrouperSystem | GrouperSysAdmin | The highest-level administrative entity of the system. |
| WheelGroup | SysadminGroup | All members of this group have full system admin privileges (see [Initializing Administration of Privileges](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545234/Initializing+Administration+of+Privileges) for more on the wheel / SysAdmin group). |
| Group privilege | Admin | Admin | Entity (typically a group or person) may modify the membership of this group, delete the group, or assign privileges for the group. |
| Member | Member | Any entity (typically a group or person) that is a part of this group. |
| Optin | Optin | Entity (typically a group or person) may choose to join this group. |
| Optout | Optout | Entity (typically a group or person) may choose to leave this group. |
| Read | Read | Entity (typically a group or person) may see the membership list for this group. |
| Update | Update | Entity (typically a group or person) may modify the membership of this group. |
| View | View | Entity (typically a group or person) may see that this group exists. |

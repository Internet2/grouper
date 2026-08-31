---
title: "UI Requirements"
space: GrIntDev
pageId: 48793731
version: 35
lastUpdated: 2026-07-12T17:03:29.258Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793731/UI+Requirements
---

[Main UI Redesign Page](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792897/Grouper+UI+redesign+v2.2)

 

### Grouper UI Requirements defined by function

 

##### Filter

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| Browse | View full path to selected folder or group | Y |
| Browse | Browse filtered groups as a complete list. | Y |
| Browse | Browse filtered groups in a collapsible folder hierarchy. | ? |
| Browse | Adjust the number of entries in filter results (10, 25, 50, 100). | Y |
| My Groups | Filter for all groups in which the user has membership. | Y |
| Join | Filter for all groups for which the user has permissions to join. | ? |
| Manager | Filter for all groups for which the user has permissions to update membership lists or assign privileges. | Y |
| Create | Filter for all folders in which the user has permissions to create groups. | Y |

 

##### Search

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| Search | Search by group name. | Y |
| Search | Search by folder name. | Y |
| Data Source | Specify a data source in which to search | ? |
| Search | Display results by Path, Name, or ID Path | ? |
| Advanced Search | Search against up to five attributes | ? |
| Advanced Search | Apply or/and/not criteria to attribute search | ? |
| Advanced Search | Search against the presence of up to three group types | ? |
| Advanced Search | Apply or/and/not criteria to group type search | ? |

 

##### Manage Groups

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| View | View a summary of group data, including:     - Name - Path - Description - ID - ID Path - Alternate ID Path - UUID - Types | Y |
| View | View entities with certain privileges for the given group:     - Admin - Update - Read - View - OptIn - OptOut | Y |
| View | View audit log for a group. | Y |
| View Advanced | View entity details (advanced or additional properties) | ? |
| View Advanced | Advanced filtering options for the selected entity:     - Show groups where this entity is an DIRECT member - Show groups where this entity is an INDIRECT member - Show all groups where this entity is a member (DIRECT or INDIRECT) - Show all GROUPS where this entity has the privilege ADMIN \| UPDATE \| READ \| VIEW \| OPTIN \| OPTOUT - Show all FOLDERS where this entity has the privilege Create group\|Create folder - Go to advanced search for ANY group privileges | ? |
| Edit | Delete a group. | Y |
| Edit | Edit a group's properties, including:     - Name - ID - Alternate ID Path - Description - Assign privileges to everyone - Select group types | Y |
| Edit | Edit a group's types and attributes. | Y |
| Edit | Move a group to another folder. | Y |
| Edit | Copy a group to another folder. | Y |
| Privileges | Search for any entity and add to list of entities which have priveleges for a given group. | ? |
| Privileges | For each entity associated with the group, assign ADMIN \| UPDATE \| READ \| VIEW \| OPTIN \| OPTOUT priveleges | Y |
| Create Group | Create a group. | Y |
| Create Group | Find all folders where user has permissions to create a group (duplicate from Filter section; just repeating for clarity) | Y |
| Create Group | Search folders in which user has permissions to create a group. | ? |
| Create Group/Advanced Search | Advanced search against folders in which user has permissions to create a group. | ? |
| Create Group/Advanced Search | Search against several attributes with optional and/or/not criteria. | ? |
| Create Group/Advanced Search | Specify starting point in path for search. | ? |
| Create Group/Advanced Search | Display results by Folder path, Folder name, or Folder ID path. | ? |

 

##### Manage Members in a Group

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| View | View all members of a group. | Y |
| View | Filter members by direct and indirect membership, or show all. | Y |
| View | Export members to a tab- or comma-delimited file. | Y |
| Invite | Invite external people to be members of a group. | Y |
| Invite | Invite via email address or Login ID. | Y |
| Edit | Import members from a tab- or comma-delimited file. | Y |
| Edit | Remove one or more, or all, members of the selected group. | Y |
| Edit | Add members by browsing folders and groups from the latest filtered list (see pain points). | ? |
| Edit | Add members by searching for a member directly. | Y |
| Edit | Create a composite group. | Y |
| Edit | Assign privileges to newly-added members - Member\|OptIn\|OptOut\|View\|Read\|Update\|Admin | Y |
| Edit | Edit privileges for existing members. | Y |

 

##### Manage Folders

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| View | View audit log for a folder. | Y |
| Edit | Edit Folder name, Folder ID, and Description. | Y |
| Edit | Copy a folder to another folder. | Y |
| Edit | Move a folder to another folder. | Y |
| Edit | Copy/move a folder/group to "here" (the currently selected folder). <em>May be possible to implement this in a more generic way so that it's not four separate functions.</em> | ? |
| Create | Create a folder. | Y |
| Search | For a given folder, find all entities with Create Group or Create Folder privileges. | ? |

 

##### Attributes

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| Create | Create an attribute. | ? |
| Create | Create an attribute name. | ? |
| Edit | Edit an attribute. | ? |
| Edit | Edit an attribute name. | ? |

 

##### Permissions and Actions

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| Create | Create a permission (attribute definition where type is permission). | ? |
| Create | Create actions associated with a permission definition. | ? |
| Edit | Assign permission to Groups and/or memberships. | ? |
| Create | Create a permission name (attribute names where associated attribute definition is of type "permission.") | ? |
| Inheritance | Specify that a permission can imply or be implied by another permission. | ? |
| Inheritance | Specify that an action can imply or be implied by another action. | ? |

 

##### Roles

 

| Component | Requirement | v2.2? |
| --- | --- | --- |
| Create | Create a role. | ? |
| Edit | Assign a group to a role? | ? |
| Edit | Assign a membership list to a role? | ? |
| Edit | Assign a permission to a role. | ? |
| Inheritance | Specify that a role can imply or be implied by other roles. | ? |

 

#### Top New Features

 

| Name | Description | v2.2? |
| --- | --- | --- |
| Batch Membership Updates | Provide the ability to assign/unassign multiple groups to/from a subject on the same screen. In other words, manage memberships from the subject info page rather than from the group page. This would allow the ability to more easily add a single subject to multiple groups. Similarly, allow for a batch import of groups to which a subject should be assigned. | Y |
| Attribute Handling at Objects | Provide the ability to manage attributes for groups, memberships, etc. on the same page that handles the object. Currently, attributes are managed in a separate view. | Y |
| Multi-language Support | Language could be set at install or (better) the user could select a different language than the default one. | ? |
| Roles & Permissions | Improve the roles and permissions workflow. | ? |
| Group Relationships | Provide a visual display of groups impacting the membership of a given group. The display would illustrate subgroups and composite factors, perhaps expanded by levels. | ? |
| Action Timer | When viewing the details of a group, display when changes to that group will next be provisioned to downstream systems, or updated from an external source. | ? |
| Clone Groups | Provide the ability to clone an existing group, including all attributes and values, renaming attributes in the process so that they are specific to the newly-cloned group. | Y |

 

#### Existing Pain Points

 

| Function | Section | Requirement |
| --- | --- | --- |
| Manage Groups | Members | When adding members, you have the option to "Browse folders and groups for members." The list of folders/groups is determined by the last filter which was applied (e.g. "My Memberships" or "Manage Groups"). [See UI Favorites and Preferences](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824692/Grouper+UI+favorites+and+preferences+user+data) |
| Manage Groups | Edit | Not clear what the difference is between "Privileges assigned to everyone" (on the group edit screen) and the list of permissions on the "Change privileges on members" screen. I got to the latter by going to manage members -> clicking "member by 2 paths" -> clicking "Change privileges" -> clicking "which has privilege :ADMIN :UPDATE" |
| Filter | Browse | Difficult to understand "where you are" in the site (whether breadcrumb, nav, etc.). |
| Search | Search | The search box at the bottom of each screen searches only against the groups included in that view. For example, if you are on the "My Memberships" screen, the search only searches against groups in which you are a member. Given the position of the search box, this is not obvious. |
| Manage Members | Edit | In order to edit privileges for an already-existing member of a group, you have to first view the group, then click "Add Members", and then check off the members you want to edit and then click "Assign Privileges." It's a lot of clicks and it's not obvious that you have to go to "Add Members." |
| Manage Members | Edit | Editing privileges in the Lite UI is also a bit tricky. You have to:     - Click on the Lite UI menu - Select "Groups and roles" - Search for the group you want to edit - Click "Edit group/role" - Click the 'Privileges" button |

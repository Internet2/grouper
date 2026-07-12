---
title: "Move and Copy API/GSH"
space: Grouper
pageId: 28549273
version: 5
lastUpdated: 2026-07-01T05:42:23.438Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549273/Move+and+Copy+API+GSH
---

### Group Copy

The GroupCopy class allows you to copy groups from one folder to another. The user must have READ access on the group being copied and CREATE access on the folder where the new group is getting created.

If the folder already has a group with the same extension, the new group will have ".2" appended to the extension. And if the group that's being copied is a composite group, then the new group will also be a composite group with the same composite type (union, complement or intersection) and the same left and right factors.

Finally, there are several options that you can specify. All options default to true.

| API Method | Description |
| --- | --- |
| copyPrivilegesOfGroup(boolean) | Whether to copy the access privileges of the group. If this option is selected, you must have READ access to all privileges. |
| copyGroupAsPrivilege(boolean) | Whether to copy access and naming privileges where the group is a member. For instance, if you are copying Group X and Group X has admin privileges to Group Y, then if this option is enabled, after Group X is copied, the new group will also have admin privileges to Group Y. If this option is selected, you must have access to add privileges to the other groups and folders. |
| copyListMembersOfGroup(boolean) | Whether to copy list memberships of the group. If this option is selected and this group has custom lists, you must have read access to them. |
| copyListGroupAsMember(boolean) | Whether to copy list memberships where the group is a member. For instance, if you are copying Group X and Group X is a member of Group Y, then if this option is enabled, after Group X is copied, the new group will also be a member of Group Y. If this option is selected, you must have access to add memberships to the other groups. |
| copyAttributes(boolean) | Whether to copy attributes. If this option is selected, you must have READ access to all attributes. These are attributes that are added to GroupTypes. This does not include attributes in the new attribute framework in v1.5.0. |

Example usage in GSH

```
gsh 1% help("GroupCopy")
GroupCopy help:
There is an object: GroupCopy which has various chaining methods,
which should be ended with a save() method.

GroupCopy groupCopy = new GroupCopy(group, stem)  Create a new instance.
GroupCopy groupCopy.copyPrivilegesOfGroup(boolean)  Whether to copy privileges of the group.  Default is true.
GroupCopy groupCopy.copyGroupAsPrivilege(boolean)  Whether to copy privileges where this group is a member.  Default is true.
GroupCopy groupCopy.copyListMembersOfGroup(boolean)  Whether to copy the list memberships of the group.  Default is true.
GroupCopy groupCopy.copyListGroupAsMember(boolean)  Whether to copy list memberships where this group is a member.  Default is true.
GroupCopy groupCopy.copyAttributes(boolean)  Whether to copy attributes.  Default is true.
Group groupCopy.save()  Copies the group.

 Examples:

gsh 1% new GroupCopy(group, stem).copyAttributes(false).save()

 -or- (without chaining)

gsh 2% groupCopy = new GroupCopy(group, stem);

gsh 3% groupCopy.copyAttributes(false);

gsh 4% groupCopy.save();

 -or- (if you want to use the default options)

gsh 5% group.copy(stem);

```

### Group Move

The GroupMove class allows you to move a group from one folder to another. The user must have ADMIN access on the group being moved and CREATE access on the folder where the group is getting moved. If a group with the same name exists in the new folder you'll get an exception.

Finally, there's one option you can specify, which defaults to true.

| API Method | Description |
| --- | --- |
| assignAlternateName(boolean) | Whether to assign the old name of the group as an alternate name of the group after the move. This allows API methods like GroupFinder.findByName() to find the group using the old and new names and can make it easier to transition from the old name to the new name. |

Example usage in GSH

```
gsh 2% help("GroupMove")
GroupMove help:
There is an object: GroupMove which has various chaining methods,
which should be ended with a save() method.

GroupMove groupMove = new GroupMove(group, stem)  Create a new instance.
GroupMove groupMove.assignAlternateName(boolean)  Whether to add the current name of the group to the group's alternate names list.  Default is true.
void groupMove.save()  Moves the group.

 Examples:

gsh 1% new GroupMove(group, stem).assignAlternateName(false).save()

 -or- (without chaining)

gsh 2% groupMove = new GroupMove(group, stem);

gsh 3% groupMove.assignAlternateName(false);

gsh 4% groupMove.save();

 -or- (if you want to use the default options)

gsh 5% group.move(stem);

```

### Folder Copy

The StemCopy class allows you to copy folders from one folder to another. The user must STEM access on the destination folder. Note that the user does not need to have any special access to child folders and groups of the folder being copied. However, the user is not automatically given any special access to the new folders and groups. So if the user copies a folder that has a group that the user cannot read, the new folder will also contain a copy of the group but the user still will not have read access.

If the set of groups being copied includes a composite group along with its two factors, then in the new folder, a new composite will get created using factors from the new folder. However, if the set of groups being copied includes a composite group but not its two factors, then in the new folder, a new composite will get created using factors from the folder being copied.

Sites can limit the users that can copy folders by setting a property in the grouper.properties file.

```
# If this property is set, then to copy a stem, a user must be a member of the defined group.
# Note that users in the wheel group will have access regardless of this property.
security.stem.groupAllowedToCopyStem = etc:someAdminGroup

```

Finally, there are several options that you can specify. All options default to true.

| API Method | Description |
| --- | --- |
| copyPrivilegesOfStem(boolean) | Whether to copy the naming privileges of the folder and child folders. |
| copyPrivilegesOfGroup(boolean) | Whether to copy the access privileges of the child groups. |
| copyGroupAsPrivilege(boolean) | Whether to copy access and naming privileges where the child group is a member. For instance, if you are copying Group X and Group X has admin privileges to Group Y, then if this option is enabled, after Group X is copied, the new group will also have admin privileges to Group Y. If this option is selected, you must have access to add privileges to the other groups and folders. |
| copyListMembersOfGroup(boolean) | Whether to copy list memberships of the child groups. |
| copyListGroupAsMember(boolean) | Whether to copy list memberships where the child group is a member. For instance, if you are copying Group X and Group X is a member of Group Y, then if this option is enabled, after Group X is copied, the new group will also be a member of Group Y. If this option is selected, you must have access to add memberships to the other groups. |
| copyAttributes(boolean) | Whether to copy attributes. These are attributes that are added to GroupTypes. This does not include attributes in the new attribute framework in v1.5.0. |

Example usage in GSH

```
gsh 4% help("StemCopy")
StemCopy help:
There is an object: StemCopy which has various chaining methods,
which should be ended with a save() method.

StemCopy stemCopy = new StemCopy(stemToCopy, destinationStem)  Create a new instance.
StemCopy stemCopy.copyPrivilegesOfStem(boolean)  Whether to copy privileges of stems.  Default is true.
StemCopy stemCopy.copyPrivilegesOfGroup(boolean)  Whether to copy privileges of groups.  Default is true.
StemCopy stemCopy.copyGroupAsPrivilege(boolean)  Whether to copy privileges where groups are a member.  Default is true.
StemCopy stemCopy.copyListMembersOfGroup(boolean)  Whether to copy the list memberships of groups.  Default is true.
StemCopy stemCopy.copyListGroupAsMember(boolean)  Whether to copy list memberships where groups are a member.  Default is true.
StemCopy stemCopy.copyAttributes(boolean)  Whether to copy attributes.  Default is true.
Stem stemCopy.save()  Copies the stem.

 Examples:

gsh 1% new StemCopy(stemToCopy, destinationStem).copyAttributes(false).save()

 -or- (without chaining)

gsh 2% stemCopy = new StemCopy(stemToCopy, destinationStem);

gsh 3% stemCopy.copyAttributes(false);

gsh 4% stemCopy.save();

 -or- (if you want to use the default options)

gsh 5% stem.copy(destinationStem);

```

### Folder Move

The StemMove class allows you to move a folder from one folder to another. The user must have STEM access on both the folder being moved and the destination folder. Note that the user does not need to have any special access to child folders and groups of the folder being moved. However, all child folders and groups will be renamed appropriately. Also, if a folder with the same name exists in the new folder you'll get an exception.

Sites can limit the users that can move folders by setting a property in the grouper.properties file.

```
# If this property is set, then to move a stem, in addition to having the appropriate stem privileges for the stem being moved and the destination stem,
# a user must also be a member of the defined group.  Note that users in the wheel group will have access regardless of this property.
security.stem.groupAllowedToMoveStem = etc:someAdminGroup

```

Finally, there's one option you can specify, which defaults to true.

| API Method | Description |
| --- | --- |
| assignAlternateName(boolean) | Whether to assign the old name of the groups in the folder as an alternate name of the groups after the move. This allows API methods like GroupFinder.findByName() to find the groups using the old and new names and can make it easier to transition from the old name to the new name. |

Example usage in GSH

```
gsh 3% help("StemMove")
StemMove help:
There is an object: StemMove which has various chaining methods,
which should be ended with a save() method.

StemMove stemMove = new StemMove(stemToMove, destinationStem)  Create a new instance.
StemMove stemMove.assignAlternateName(boolean)  Whether to add the current names of the affected groups to the groups' alternate names list.  Default is true.
void stemMove.save()  Moves the stem.

 Examples:

gsh 1% new StemMove(stemToMove, destinationStem).assignAlternateName(false).save()

 -or- (without chaining)

gsh 2% stemMove = new StemMove(stemToMove, destinationStem);

gsh 3% stemMove.assignAlternateName(false);

gsh 4% stemMove.save();

 -or- (if you want to use the default options)

gsh 5% stem.move(destinationStem);

```

Grouper UI screenshot

### Web Services Approach

It is also possible to use web services to move groups from one stem to another. Here is sample code

| POST /grouper-ws/servicesRest/v2_2_000/groups  {  "WsRestGroupSaveRequest":  {  "wsGroupToSaves":  [  {  "wsGroup":  {  "name":"UDL:ACT:PROJ:PROJ:test_ss_dossier:GT-REFIDENTNUM"  },  "wsGroupLookup":  {  "groupName":"UDL:ACT:PROJ:test_ss_dossier:GT-REFIDENTNUM"  }  }  ],  "params":  [  {  "paramName":"moveOrCopy",  "paramValue":"move"  },  {  "paramName":"moveOrCopyToStemName",  "paramValue":"UDL:ACT:PROJ"  }  ]  }  } |
| --- |

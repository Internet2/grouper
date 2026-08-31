---
title: "GrouperShell (gsh) Group insert / update / delete (GroupSave)"
space: Grouper
pageId: 28547475
version: 18
lastUpdated: 2026-07-01T05:46:41.530Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547475/GrouperShell+gsh+Group+insert+update+delete+GroupSave
---

The GroupSave class is the recommended and support way to insert/update/delete a group. This class was introduced in v1.4+ , some options added later.

Sample call

> Group groupAbc = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();

Sample using GroupSave results

> GroupSave groupSave = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true); Group groupAbc = groupSave.save(); System.out.println(groupSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample to delete

> new GroupSave().assignName("a:b:c").assignSaveMode("DELETE").save();

To edit just one field (the description) for existing group a:b:c

> new GroupSave().assignName("a:b:c").assignDescription("new description").assignReplaceAllSettings(false).save();

To edit just one field (the idIndex) for existing group a:b:c

Note, that new idIndex cant be in use by another group (query grouper_groups_v for the id_index col) and should not be above the current pointer (which you can adjust in the table grouper_table_index)

> new GroupSave().assignName("a:b:c").assignIdIndex(12345).assignReplaceAllSettings(false).save();

Rename a group to same parent folder

> new GroupSave().assignGroupNameToEdit("folder:oldName").assignName("folder:newName").assignReplaceAllSettings(false).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GroupSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GroupSave.html)

## Legacy methods

This methods are not recommended:

| Command | Description |
| --- | --- |
| addGroup(parent stem name, extension, displayExtension) | Add group to registry |
| delGroup(name) | Delete group from registry |
| getGroupAttr(group name, attr) | Get value of group attribute |
| setGroupAttr(group name, attr, value) | Set value of group attribute |

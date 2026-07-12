---
title: "GrouperShell (gsh) Group copy (GroupCopy)"
space: Grouper
pageId: 28548194
version: 3
lastUpdated: 2026-07-01T05:45:05.982Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548194/GrouperShell+gsh+Group+copy+GroupCopy
---

Use this class to copy a group to another stem.

Sample call to copy everything

> GroupCopy groupCopy = new GroupCopy(child_group, stem); Group newGroup = groupCopy.copyPrivilegesOfGroup(true).copyGroupAsPrivilege(true) .copyListMembersOfGroup(true).copyListGroupAsMember(true).copyAttributes(true) .save();

  

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GroupCopy.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GroupCopy.html)

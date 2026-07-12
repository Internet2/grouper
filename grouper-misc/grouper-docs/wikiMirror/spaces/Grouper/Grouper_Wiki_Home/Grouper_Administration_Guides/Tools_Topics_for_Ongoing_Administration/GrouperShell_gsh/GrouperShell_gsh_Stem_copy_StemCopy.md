---
title: "GrouperShell (gsh) Stem copy (StemCopy)"
space: Grouper
pageId: 28548670
version: 3
lastUpdated: 2026-07-01T05:43:51.061Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548670/GrouperShell+gsh+Stem+copy+StemCopy
---

Use this class to copy a stem to another stem.

Sample call to copy everything from source to target

> StemCopy stemCopy = new StemCopy(stem_copy_source, stem_copy_target); Stem newStem = stemCopy.copyPrivilegesOfStem(true).copyPrivilegesOfGroup(true) .copyGroupAsPrivilege(true).copyListMembersOfGroup(true) .copyListGroupAsMember(true).copyAttributes(true).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/StemCopy.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/StemCopy.html)

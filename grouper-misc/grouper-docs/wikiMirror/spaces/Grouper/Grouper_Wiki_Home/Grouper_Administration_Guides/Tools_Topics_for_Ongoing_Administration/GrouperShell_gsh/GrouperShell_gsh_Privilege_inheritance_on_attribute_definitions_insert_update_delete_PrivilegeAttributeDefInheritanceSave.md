---
title: "GrouperShell (gsh) Privilege inheritance on attribute definitions insert / update / delete (PrivilegeAttributeDefInheritanceSave)"
space: Grouper
pageId: 28548169
version: 5
lastUpdated: 2026-07-01T05:45:09.424Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548169/GrouperShell+gsh+Privilege+inheritance+on+attribute+definitions+insert+update+delete+PrivilegeAttributeDefInheritanceSave
---

Use this class to add/edit/delete privileges on attribute defs inside a folder for a subject

Sample call

> SaveResultType saveResultType = new PrivilegeAttributeDefInheritanceSave() .assignStem(stem) .assignStemScope(Scope.SUB) .assignSubject(subject) .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN) .save(); System.out.println(saveResultType); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete privileges on attribute defs inside a folder for a subject

> new PrivilegeAttributeDefInheritanceSave() .assignStem(stem) .assignStemScope(Scope.SUB) .assignSubject(subject) .assignSaveMode(SaveMode.DELETE) .save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/PrivilegeAttributeDefInheritanceSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/PrivilegeAttributeDefInheritanceSave.html)

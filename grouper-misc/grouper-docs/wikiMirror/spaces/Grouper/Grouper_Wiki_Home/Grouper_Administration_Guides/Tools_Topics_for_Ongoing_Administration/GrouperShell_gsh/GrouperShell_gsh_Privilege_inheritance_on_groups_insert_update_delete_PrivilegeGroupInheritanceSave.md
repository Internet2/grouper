---
title: "GrouperShell (gsh) Privilege inheritance on groups insert / update / delete (PrivilegeGroupInheritanceSave)"
space: Grouper
pageId: 28548634
version: 6
lastUpdated: 2026-07-01T05:43:57.748Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548634/GrouperShell+gsh+Privilege+inheritance+on+groups+insert+update+delete+PrivilegeGroupInheritanceSave
---

Use this class to add/edit/delete privileges on groups inside a folder for a subject

Sample call

> SaveResultType saveResultType = new PrivilegeGroupInheritanceSave() .assignStem(stem) .assignStemScope(Scope.SUB) .assignSubject(subject) .addPrivilege(AccessPrivilege.ADMIN) .addPrivilege(AccessPrivilege.OPTIN) .save(); System.out.println(saveResultType); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete privileges on groups inside a folder for a subject

> new PrivilegeGroupInheritanceSave() .assignStem(stem) .assignStemScope(Scope.SUB) .assignSubject(subject) .assignSaveMode(SaveMode.DELETE) .save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/PrivilegeGroupInheritanceSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/PrivilegeGroupInheritanceSave.html)

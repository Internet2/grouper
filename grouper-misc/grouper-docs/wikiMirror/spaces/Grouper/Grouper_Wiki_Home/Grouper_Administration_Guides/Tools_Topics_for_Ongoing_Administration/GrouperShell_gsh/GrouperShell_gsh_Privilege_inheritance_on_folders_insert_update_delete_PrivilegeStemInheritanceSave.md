---
title: "GrouperShell (gsh) Privilege inheritance on folders insert / update / delete (PrivilegeStemInheritanceSave)"
space: Grouper
pageId: 28548285
version: 5
lastUpdated: 2026-07-01T05:44:55.637Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548285/GrouperShell+gsh+Privilege+inheritance+on+folders+insert+update+delete+PrivilegeStemInheritanceSave
---

Use this class to add/edit/delete privileges on folders inside a folder for a subject

Sample call

> SaveResultType saveResultType = new PrivilegeStemInheritanceSave() .assignStem(stem) .assignStemScope(Scope.SUB) .assignSubject(subject) .addPrivilege(NamingPrivilege.STEM_ADMIN) .save(); System.out.println(saveResultType); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete privileges on folders inside a folder for a subject

> new PrivilegeStemInheritanceSave() .assignStem(stem) .assignStemScope(Scope.SUB) .assignSubject(subject) .assignSaveMode(SaveMode.DELETE) .save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/PrivilegeStemInheritanceSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/PrivilegeStemInheritanceSave.html)

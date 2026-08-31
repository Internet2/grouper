---
title: "GrouperShell (gsh) Composite insert / update / delete (CompositeSave)"
space: Grouper
pageId: 28548324
version: 8
lastUpdated: 2026-07-01T05:44:49.247Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548324/GrouperShell+gsh+Composite+insert+update+delete+CompositeSave
---

Use this class to insert or update or delete a composite

Sample call (type is complement or intersection)

> Composite composite = new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName()) .assignType("complement").save();

Sample call to delete a composite

> new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName()) .assignSaveMode(SaveMode.DELETE).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/CompositeSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/CompositeSave.html)

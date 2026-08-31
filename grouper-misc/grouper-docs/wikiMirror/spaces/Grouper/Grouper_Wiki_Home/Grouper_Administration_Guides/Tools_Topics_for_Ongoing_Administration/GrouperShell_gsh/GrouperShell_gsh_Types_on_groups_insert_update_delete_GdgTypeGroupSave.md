---
title: "GrouperShell (gsh) Types on groups insert / update / delete (GdgTypeGroupSave)"
space: Grouper
pageId: 28547669
version: 5
lastUpdated: 2026-07-01T05:46:24.116Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547669/GrouperShell+gsh+Types+on+groups+insert+update+delete+GdgTypeGroupSave
---

Use this class to add/edit/delete object types on groups

Sample call

> GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave(); GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeGroupSave .assignGroup(group) .assignType("ref") .assignDataOwner("do") .assignMemberDescription("md") .save(); System.out.println(gdgTypeGroupSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete an object type from a group

> GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave(); gdgTypeGroupSave .assignGroup(group) .assignType("ref") .assignSaveMode(SaveMode.DELETE) .save();

Sample call to update only single attribute

> GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave(); gdgTypeGroupSave .assignGroup(group) .assignType("ref") .assignReplaceAllSettings(false) .assignDataOwner("do1") .save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperTypes/GdgTypeGroupSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperTypes/GdgTypeGroupSave.html)

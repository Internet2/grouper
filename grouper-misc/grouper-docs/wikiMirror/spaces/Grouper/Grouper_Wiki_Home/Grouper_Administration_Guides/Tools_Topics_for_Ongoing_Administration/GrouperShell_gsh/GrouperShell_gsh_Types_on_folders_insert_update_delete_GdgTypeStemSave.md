---
title: "GrouperShell (gsh) Types on folders insert / update / delete (GdgTypeStemSave)"
space: Grouper
pageId: 28548640
version: 5
lastUpdated: 2026-07-01T05:43:56.604Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548640/GrouperShell+gsh+Types+on+folders+insert+update+delete+GdgTypeStemSave
---

Use this class to add/edit/delete object types on stems

Sample call

> GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave(); GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = gdgTypeStemSave .assignStem(stem) .assignType("ref") .assignDataOwner("do") .assignMemberDescription("md") .save(); System.out.println(gdgTypeStemSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete an object type from a stem

> GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave(); gdgTypeStemSave .assignStem(stem) .assignType("ref") .assignSaveMode(SaveMode.DELETE) .save();

Sample call to update only single attribute

> GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave(); gdgTypeStemSave .assignStem(stem) .assignType("ref") .assignReplaceAllSettings(false) .assignDataOwner("do1") .save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperType/GdgTypeStemSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/grouperType/GdgTypeStemSave.html)

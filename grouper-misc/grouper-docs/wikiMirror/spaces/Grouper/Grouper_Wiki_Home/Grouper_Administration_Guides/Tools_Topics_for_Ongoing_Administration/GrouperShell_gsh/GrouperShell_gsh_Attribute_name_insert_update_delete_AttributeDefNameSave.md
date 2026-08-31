---
title: "GrouperShell (gsh) Attribute name insert / update / delete (AttributeDefNameSave)"
space: Grouper
pageId: 28548628
version: 5
lastUpdated: 2026-07-01T05:43:58.855Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548628/GrouperShell+gsh+Attribute+name+insert+update+delete+AttributeDefNameSave
---

Use this class to insert or update an attribute def name

Sample call

> AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef) .assignName("top:b").assignDescription("whatever").assignDisplayExtension("theB"); AttributeDefName attributeDefName = attributeDefNameSave.save(); System.out.println(attributeDefNameSave.getSaveResultType()); // INSERT, NO_CHANGE, or UPDATE

Sample call to update only one attribute

> new AttributeDefNameSave(grouperSession, attributeDef) .assignName("top:b").assignDisplayExtension("theB").assignReplaceAllSettings(false).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/AttributeDefNameSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/AttributeDefNameSave.html)

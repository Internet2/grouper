---
title: "GrouperShell (gsh) Attribute definition insert / update / delete (AttributeDefSave)"
space: Grouper
pageId: 28547797
version: 5
lastUpdated: 2026-07-01T05:46:09.484Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547797/GrouperShell+gsh+Attribute+definition+insert+update+delete+AttributeDefSave
---

Use this class to insert or update an attribute definition

Sample call

> AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession) .assignName("top:b").assignDescription("whatever").assignValueType(AttributeDefValueType.string).assignMultiValued(true); AttributeDef attributeDef = attributeDefSave.save(); System.out.println(attributeDefSave.getSaveResultType()); // INSERT, NO_CHANGE, or UPDATE

Sample call to update only one attribute

> new AttributeDefSave(grouperSession) .assignName("top:b").assignMultiValued(true).assignReplaceAllSettings(false).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/AttributeDefSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/AttributeDefSave.html)

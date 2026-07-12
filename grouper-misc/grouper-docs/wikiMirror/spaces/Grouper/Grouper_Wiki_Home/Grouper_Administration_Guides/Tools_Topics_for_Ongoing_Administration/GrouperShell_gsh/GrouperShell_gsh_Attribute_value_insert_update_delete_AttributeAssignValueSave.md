---
title: "GrouperShell (gsh) Attribute value insert / update / delete (AttributeAssignValueSave)"
space: Grouper
pageId: 28548646
version: 8
lastUpdated: 2026-07-01T05:43:55.558Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548646/GrouperShell+gsh+Attribute+value+insert+update+delete+AttributeAssignValueSave
---

Use this class to add/edit/delete values from attribute assigns

Sample call

> AttributeAssignValueSave attributeAssignValueSave = new AttributeAssignValueSave(); AttributeAssignValueResult attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign) .assignValue("hello").save(); System.out.println(attributeAssignValueSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample call to remove value from an attribute assign

> AttributeAssignValueSave attributeAssignValueSave = new AttributeAssignValueSave(); AttributeAssignValueResult attributeAssignValueResult = attributeAssignValueSave.assignAttributeAssign(attributeAssign) .assignAttributeAssignValueOperation(AttributeAssignValueOperation.remove_value) .assignValue("hello").save();

Sample call to add attribute and value to stem

> AttributeValueResult attributeValueResult=sympaFolder.getAttributeValueDelegate().assignValue("etc:attribute:comanagetemplate:ownerfolderid", "someValue"); gsh_builtin_gshTemplateOutput.addOutputLine("Attribute ownerfolderid assigned: " + attributeValueResult.getAttributeAssignValueResult().isChanged());

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/value/AttributeAssignValueSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/value/AttributeAssignValueSave.html)

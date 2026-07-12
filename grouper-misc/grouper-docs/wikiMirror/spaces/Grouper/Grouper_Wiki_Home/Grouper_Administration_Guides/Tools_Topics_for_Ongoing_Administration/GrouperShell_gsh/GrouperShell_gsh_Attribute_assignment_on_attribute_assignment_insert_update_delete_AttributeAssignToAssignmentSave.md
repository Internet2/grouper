---
title: "GrouperShell (gsh) Attribute assignment on attribute assignment insert /update / delete (AttributeAssignToAssignmentSave)"
space: Grouper
pageId: 28548093
version: 8
lastUpdated: 2026-07-01T05:45:22.911Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548093/GrouperShell+gsh+Attribute+assignment+on+attribute+assignment+insert+update+delete+AttributeAssignToAssignmentSave
---

Use this class to add/edit/delete attribute def names on attribute assigns.

Sample call

> import edu.internet2.middleware.grouper.attr.assign.*; AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave(); AttributeAssignResult attributeAssignResult = attributeAssignToAssignmentSave.assignAttributeAssign(attributeAssign).assignAttributeDefName(attributeDefName).save(); System.out.println(attributeAssignToAssignmentSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample call to remove attribute def name from an attribute assign

> import edu.internet2.middleware.grouper.attr.assign.*;AttributeAssignToAssignmentSave attributeAssignToAssignmentSave = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssign) .assignAttributeDefName(attributeDefName) .assignAttributeAssignOperation(AttributeAssignOperation.remove_attr) .save();

Sample call to assign attribute and metadata with values

> import edu.internet2.middleware.grouper.attr.assign.*; AttributeAssign attributeAssign = new AttributeAssignToGroupSave(). assignNameOfAttributeDefName("etc:attribute:abacJexlScript:grouperJexlScriptMarker"). assignGroupName("test:isc:astt:chris:testJexl2").save(); attributeAssign.getAttributeValueDelegate().assignValueString( "etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", "${entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup1')}");

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/assign/AttributeAssignToAssignmentSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/assign/AttributeAssignToAssignmentSave.html)

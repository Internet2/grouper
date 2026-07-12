---
title: "GrouperShell (gsh) Attribute assignment on group insert /update / delete (AttributeAssignToGroupSave)"
space: Grouper
pageId: 28547867
version: 9
lastUpdated: 2026-07-01T05:45:59.823Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547867/GrouperShell+gsh+Attribute+assignment+on+group+insert+update+delete+AttributeAssignToGroupSave
---

Use this class to add/edit/delete attribute def names on groups.

Sample call

> import edu.internet2.middleware.grouper.attr.assign.*; AttributeAssignToGroupSave attributeAssignToGroupSave = new AttributeAssignToGroupSave().assignAttributeDefName(attributeDefName).assignGroup(group); AttributeAssign attributeAssign = attributeAssignToGroupSave.save(); System.out.println(attributeAssignToGroupSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample call to remove attribute def name from a group

> import edu.internet2.middleware.grouper.attr.assign.*;new AttributeAssignToGroupSave().assignAttributeDefName(attributeDefName).assignGroup(group).assignSaveMode(SaveMode.DELETE).save();

Sample call to assign attribute and metadata with values

> import edu.internet2.middleware.grouper.attr.assign.*; AttributeAssign attributeAssign = new AttributeAssignToGroupSave(). assignNameOfAttributeDefName("etc:attribute:abacJexlScript:grouperJexlScriptMarker"). assignGroupName("test:isc:astt:chris:testJexl2").save(); attributeAssign.getAttributeValueDelegate().assignValueString( "etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", "${entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup1')}");

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/assign/AttributeAssignToGroupSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/assign/AttributeAssignToGroupSave.html)

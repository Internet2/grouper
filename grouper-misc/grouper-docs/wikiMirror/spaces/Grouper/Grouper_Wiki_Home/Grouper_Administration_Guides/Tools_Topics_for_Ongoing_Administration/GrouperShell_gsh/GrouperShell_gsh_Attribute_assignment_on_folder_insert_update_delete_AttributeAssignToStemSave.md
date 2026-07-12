---
title: "GrouperShell (gsh) Attribute assignment on folder insert /update / delete (AttributeAssignToStemSave)"
space: Grouper
pageId: 28548622
version: 7
lastUpdated: 2026-07-01T05:43:59.846Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548622/GrouperShell+gsh+Attribute+assignment+on+folder+insert+update+delete+AttributeAssignToStemSave
---

Use this class to add/edit/delete attribute def names on folders.

Sample call

> import edu.internet2.middleware.grouper.attr.assign.*; AttributeAssignToStemSave attributeAssignToStemSave = new AttributeAssignToStemSave().assignAttributeDefName(attributeDefName).assignStem(stem); AttributeAssign attributeAssign = attributeAssignToStemSave.save(); System.out.println(attributeAssignToStemSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE

Sample call to remove attribute def name from a folder

> import edu.internet2.middleware.grouper.attr.assign.*; new AttributeAssignToStemSave().assignAttributeDefName(attributeDefName).assignStem(stem).assignSaveMode(SaveMode.DELETE).save();

Sample call to assign attribute and metadata with values. Note, this example doesnt make sense since this attribute is assignable to groups, but this is how to do it.

> import edu.internet2.middleware.grouper.attr.assign.*; AttributeAssign attributeAssign = new AttributeAssignToStemSave(). assignNameOfAttributeDefName("etc:attribute:abacJexlScript:grouperJexlScriptMarker"). assignStemName("test:isc:astt:chris:testJexl2").save(); attributeAssign.getAttributeValueDelegate().assignValueString( "etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", "${entity.memberOf('test:testGroup') && !entity.memberOf('test:testGroup1')}");

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/assign/AttributeAssignToStemSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/attr/assign/AttributeAssignToStemSave.html)

// 1) no groups are provisioned

msg="Marking testFolder for all provisioners";
print("STARTING: " + msg);
gs = GrouperSession.startRootSession();
testFolderName = "testFolder";
testFolder = StemFinder.findByName(gs, testFolderName);

attributeName="etc:pspng:provision_to";
attributeDefName = AttributeDefNameFinder.findByName(attributeName, true);

// TRYING to set three multi-assign values
//
// BUG: testFolder.getAttributeDelegate().addAttribute(attributeDefName).getAttributeAssign().getAttributeValueDelegate().addValue("pspng_epe");
// ERROR: Error in method invocation: Method addValue( java.lang.String ) not found in class'edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate'

// BUG: testFolder.getAttributeDelegate().addAttribute(attributeDefName).getAttributeAssign().getAttributeValueDelegate().addValue(attributeName, "pspng_epe");
// ERROR: Attribute is not assignable to a stem attribute assignment, nameOfAttributeDefName: etc:pspng:provision_to, nameOfAttributeDef: etc:pspng:provision_to_def

// BUG: testFolder.getAttributeDelegate().addAttribute(attributeDefName).getAttributeAssign().getAttributeValueDelegate().assignValue("pspng_epe");
// ERROR: Error in method invocation: Method assignValue( java.lang.String ) not found in class'edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate

// BUG: testFolder.getAttributeDelegate().addAttribute(attributeDefName).getAttributeAssign().getAttributeValueDelegate().assignValue(attributeName+"_def", "pspng_epe");
// ERROR: Cannot find (or not allowed to find) attribute def name with name: 'etc:pspng:provision_to_def'

// BUG: testFolder.getAttributeDelegate().addAttribute(attributeDefName).getAttributeAssign().getAttributeValueDelegate().assignValue(attributeDefName.getName(), "pspng_epe");
// ERROR: Attribute is not assignable to a stem attribute assignment, nameOfAttributeDefName: etc:pspng:provision_to, nameOfAttributeDef: etc:pspng:provision_to_def


attrAssignment = testFolder.getAttributeValueDelegate().assignValue(attributeName, "pspng_epe");
attrAssignment = testFolder.getAttributeValueDelegate().assignValue(attributeName, "pspng_groups_goun");
attrAssignment = testFolder.getAttributeValueDelegate().assignValue(attributeName, "pspng_groups_posix2");

print("DONE: " + msg);

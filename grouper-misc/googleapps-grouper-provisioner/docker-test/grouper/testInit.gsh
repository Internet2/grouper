grouperSession = GrouperSession.startRootSession();
addStem("etc:attribute", "provisioningTargets", "Provisioning Targets")

attributeStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "etc:attribute:provisioningTargets", true);
attrDef = attributeStem.addChildAttributeDef("provisioningCandidatesDef", AttributeDefType.attr);
attrDef.setAssignToGroup(true);
attrDef.setAssignToStem(true);
attrDef.setValueType(AttributeDefValueType.string);
attrDef.store();

attrDefName = attributeStem.addChildAttributeDefName(attrDef,  "provisioningCandidates", "provisioningCandidates");
attrDefName.setDescription("Grants groups eligibility to be assign provisioning attributes.");
attrDefName.store();

//-------

addStem("etc:attribute:provisioningTargets", "all", "All")
attributeStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "etc:attribute:provisioningTargets:all", true);
attrDef = attributeStem.addChildAttributeDef("allProvisioningTargetDef", AttributeDefType.attr);
attrDef.setAssignToGroup(true);
attrDef.setValueType(AttributeDefValueType.string);
attrDef.store();

attrDefName = attributeStem.addChildAttributeDefName(attrDef,  "syncToLdapIMO", "syncToLdapIMO");
attrDefName.setDescription("The food to bring to parties.");
attrDefName.store();

attrDefName = attributeStem.addChildAttributeDefName(attrDef,  "allowLargeGroups", "allowLargeGroups");
attrDefName.setDescription("The food to bring to parties.");
attrDefName.store();

//-------

addRootStem("test", "test");
addStem("test", "provisionEnabledGroups", "Provision Enabled Groups");
grantPriv("test:provisionEnabledGroups", "jsmith", NamingPrivilege.CREATE);

:quit
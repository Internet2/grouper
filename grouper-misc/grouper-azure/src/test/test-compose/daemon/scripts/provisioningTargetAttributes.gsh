GrouperSession grouperSession = GrouperSession.startRootSession();
AttributeDef provisioningMarkerAttributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("etc:attribute:office365:o365SyncDef").assignToStem(true).assignToGroup(true).save();
AttributeDefName provisioningMarkerAttributeName = new AttributeDefNameSave(grouperSession, provisioningMarkerAttributeDef).assignName("etc:attribute:office365:o365Sync").save();

rootStem = addStem("", "test", "test");
rootStem.getAttributeDelegate().assignAttribute(provisioningMarkerAttributeName);

AttributeDef o365Id = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("etc:attribute:office365:o365IdDef").assignToGroup(true).assignValueType(AttributeDefValueType.string).save();
AttributeDefName o365IdName = new AttributeDefNameSave(grouperSession, o365Id).assignName("etc:attribute:office365:o365Id").save();
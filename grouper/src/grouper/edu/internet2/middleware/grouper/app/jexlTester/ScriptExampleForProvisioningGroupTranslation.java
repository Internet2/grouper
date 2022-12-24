package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForProvisioningGroupTranslation implements ScriptExample {
  
  GENERIC,
  
  REVERSE_STRING,
  
  LDAP_DN_TRANSLATION,
  
  METADATA_OVERRIDE;


  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_GROUP_TRANSLATION;
  }

}

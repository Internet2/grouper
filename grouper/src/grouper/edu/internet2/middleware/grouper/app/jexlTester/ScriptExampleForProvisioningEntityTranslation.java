package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForProvisioningEntityTranslation implements ScriptExample {

  GENERIC;


  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_ENTITY_TRANSLATION;
  }

}

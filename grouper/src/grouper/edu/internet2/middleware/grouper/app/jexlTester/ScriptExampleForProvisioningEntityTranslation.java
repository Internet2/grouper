package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForProvisioningEntityTranslation implements ScriptExample {

  GENERIC {

    public Object expectedOutput() {
      return "TestName_abc123";
    }
    
  };


  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_ENTITY_TRANSLATION;
  }

  @Override
  public abstract Object expectedOutput();

}

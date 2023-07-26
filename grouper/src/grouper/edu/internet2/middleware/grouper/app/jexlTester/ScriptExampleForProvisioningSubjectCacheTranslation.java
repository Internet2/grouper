package edu.internet2.middleware.grouper.app.jexlTester;

public enum ScriptExampleForProvisioningSubjectCacheTranslation implements ScriptExample {
  
  GENERIC {

    @Override
    public Object expectedOutput() {
      return "GrouperSysAdmin_GrouperSystem";
    }
    
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_SUBJECT_CACHE_TRANSLATION;
  }

  @Override
  public abstract Object expectedOutput();


}

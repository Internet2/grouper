package edu.internet2.middleware.grouper.app.jexlTester;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public enum ScriptExampleForProvisioningMembershipTranslation implements ScriptExample {
  
  GENERIC {

    @Override
    public Object expectedOutput() {
      return new MultiKey("test:testGroup", "12345678");
    }
    
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_MEMBERSHIP_TRANSLATION;
  }

  @Override
  public abstract Object expectedOutput();


}

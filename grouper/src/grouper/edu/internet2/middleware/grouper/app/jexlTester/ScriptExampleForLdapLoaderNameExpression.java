package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForLdapLoaderNameExpression implements ScriptExample {
  
  CONVERT_DN {

    public Object expectedOutput() {
      return "testGroup2";
    }
    
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.LDAP_LOADER_NAME_EXPRESSION;
  }
  
  @Override
  public abstract Object expectedOutput();

}

package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForLdapLoaderNameExpression implements ScriptExample {
  
  CONVERT_DN;

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.LDAP_LOADER_NAME_EXPRESSION;
  }

}

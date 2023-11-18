package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForGshTemplateShowEl implements ScriptExample {
  
  ACTION_IN_SET {

    @Override
    public Object expectedOutput() {
      return true;
    }
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.GSH_TEMPLATE_SHOW_EL;
  }

  @Override
  public abstract Object expectedOutput();

}

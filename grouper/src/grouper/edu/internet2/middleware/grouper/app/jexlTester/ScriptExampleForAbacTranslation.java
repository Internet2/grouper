package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForAbacTranslation implements ScriptExample {
  
  THREE_PART_COMPOSITE {

    @Override
    public Object expectedOutput() {
      return true;
    }
    
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.ABAC;
  }

  public abstract Object expectedOutput();

  
  
}

package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForSubjectSourceTranslation implements ScriptExample {
  
  GENERIC {

    @Override
    public Object expectedOutput() {
      return "John Smith";
    }
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.SUBJECT_SOURCE;
  }

  @Override
  public abstract Object expectedOutput();
  
  

}

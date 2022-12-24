package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForSubjectSourceTranslation implements ScriptExample {
  
  GENERIC;

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.SUBJECT_SOURCE;
  }

}

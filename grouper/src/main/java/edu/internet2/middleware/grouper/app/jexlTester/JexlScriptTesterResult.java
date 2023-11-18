package edu.internet2.middleware.grouper.app.jexlTester;


public class JexlScriptTesterResult {
  
  private boolean success;
  
  private String gshScriptThatWasExecuted;
  
  private String resultForScreen;

  
  public boolean isSuccess() {
    return success;
  }

  
  public void setSuccess(boolean success) {
    this.success = success;
  }

  
  public String getGshScriptThatWasExecuted() {
    return gshScriptThatWasExecuted;
  }

  
  public void setGshScriptThatWasExecuted(String gshScriptThatWasExecuted) {
    this.gshScriptThatWasExecuted = gshScriptThatWasExecuted;
  }

  
  public String getResultForScreen() {
    return resultForScreen;
  }

  
  public void setResultForScreen(String resultForScreen) {
    this.resultForScreen = resultForScreen;
  }
  
  

}

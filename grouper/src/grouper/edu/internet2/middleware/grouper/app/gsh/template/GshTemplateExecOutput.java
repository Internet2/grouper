package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GshTemplateExecOutput {
  
  private GshTemplateOutput gshTemplateOutput;
  
  private String gshScriptOutput;
  
  private boolean isSuccess;
  
  private boolean isValid;
  
  private RuntimeException exception;

  public boolean isSuccess() {
    return isSuccess;
  }


  
  public void setSuccess(boolean isSuccess) {
    this.isSuccess = isSuccess;
  }


  
  public boolean isValid() {
    return isValid;
  }


  
  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }


  public GshTemplateOutput getGshTemplateOutput() {
    return gshTemplateOutput;
  }

  
  public void setGshTemplateOutput(GshTemplateOutput gshTemplateOutput) {
    this.gshTemplateOutput = gshTemplateOutput;
  }

  
  public String getGshScriptOutput() {
    return gshScriptOutput;
  }

  
  public void setGshScriptOutput(String gshScriptOutput) {
    this.gshScriptOutput = gshScriptOutput;
  }



  
  public RuntimeException getException() {
    return exception;
  }



  
  public void setException(RuntimeException exception) {
    this.exception = exception;
  }
  
  public String getExceptionStack() {
    if (exception != null) {
      return GrouperUtil.getFullStackTrace(exception);
    }
    return null;
  }
  

}

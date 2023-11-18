package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang3.StringUtils;

public class GshTemplateV2testOutput {

  private GshTemplateV2test gshTemplateV2test = null;
  
  public GshTemplateV2test getGshTemplateV2test() {
    return gshTemplateV2test;
  }
  
  public void setGshTemplateV2test(GshTemplateV2test gshTemplateV2test) {
    this.gshTemplateV2test = gshTemplateV2test;
  }

  /** false if there is an exception in run logic */
  private boolean runLogicSuccess = false;
  
  /** false if there is an exception in run logic */
  public boolean isRunLogicSuccess() {
    return runLogicSuccess;
  }
  
  /** false if there is an exception in run logic */
  public void setRunLogicSuccess(boolean runLogicSuccess) {
    this.runLogicSuccess = runLogicSuccess;
  }

  /** false if there is an exception in tear down */
  private boolean tearDownSuccess = false;
  
  /** false if there is an exception in tear down */
  public boolean isTearDownSuccess() {
    return tearDownSuccess;
  }
  
  /** false if there is an exception in tear down */
  public void setTearDownSuccess(boolean tearDownSuccess) {
    this.tearDownSuccess = tearDownSuccess;
  }

  /** false if there is an exception in check result */
  private boolean checkResultSuccess = false;
  
  /** false if there is an exception in check result */
  public boolean isCheckResultSuccess() {
    return checkResultSuccess;
  }
  
  /** false if there is an exception in check result */
  public void setCheckResultSuccess(boolean checkResultSuccess) {
    this.checkResultSuccess = checkResultSuccess;
  }

  /** false if there is an exception in setup */
  private boolean setupSuccess = false;
  
  /** false if there is an exception in setup */
  public boolean isSetupSuccess() {
    return setupSuccess;
  }
  
  /** false if there is an exception in setup */
  public void setSetupSuccess(boolean setupSuccess) {
    this.setupSuccess = setupSuccess;
  }

  /** exception at any step */
  private Throwable throwable;
  
  /** exception at any step */
  public Throwable getThrowable() {
    return throwable;
  }
  
  /** exception at any step */
  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  private GshTemplateOutput gsh_builtin_gshTemplateOutput;

  public GshTemplateOutput getGsh_builtin_gshTemplateOutput() {
    return gsh_builtin_gshTemplateOutput;
  }

  
  public void setGsh_builtin_gshTemplateOutput(
      GshTemplateOutput gsh_builtin_gshTemplateOutput) {
    this.gsh_builtin_gshTemplateOutput = gsh_builtin_gshTemplateOutput;
  }

  private StringBuilder message = new StringBuilder();
  
  public void appendMessage(String string) {
    if (!StringUtils.isBlank(string)) {
      if (this.message.length() > 0 && this.message.charAt(this.message.length()-1) != ','
          && this.message.charAt(this.message.length()-1) != '.'  && this.message.charAt(this.message.length()-1) != '!'
          && this.message.charAt(this.message.length()-1) != ';') {
        this.message.append(", ");
      }
      this.message.append(string);
    }
  }

  public String getMessage() {
    return this.message.toString();
  }

  private boolean failure = false;
  
  public void setFailure(boolean b) {
    this.failure = b;
  }
  
  public boolean isFailure() {
    return failure;
  }
  
}

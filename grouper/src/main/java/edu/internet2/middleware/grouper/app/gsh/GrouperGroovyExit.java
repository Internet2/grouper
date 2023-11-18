package edu.internet2.middleware.grouper.app.gsh;


public class GrouperGroovyExit extends RuntimeException {

  private int exitCode = 0;
  
  
  public int getExitCode() {
    return exitCode;
  }

  
  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public GrouperGroovyExit() {
  }

  public GrouperGroovyExit(int theExitCode) {
    this();
    this.exitCode = theExitCode;
  }

  public GrouperGroovyExit(int theExitCode, String message) {
    this(message);
    this.exitCode = theExitCode;
  }

  
  public GrouperGroovyExit(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public GrouperGroovyExit(String message, Throwable cause) {
    super(message, cause);
  }

  public GrouperGroovyExit(String message) {
    super(message);
  }

  public GrouperGroovyExit(Throwable cause) {
    super(cause);
  }

  
  
}


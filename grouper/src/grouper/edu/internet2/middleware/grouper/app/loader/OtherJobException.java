package edu.internet2.middleware.grouper.app.loader;

public class OtherJobException extends RuntimeException {
  
  public OtherJobException(GrouperLoaderStatus grouperLoaderStatus1, String message, Throwable cause) {
    super(message, cause);
    this.grouperLoaderStatus = grouperLoaderStatus1;
  }

  public OtherJobException(GrouperLoaderStatus grouperLoaderStatus1, String message) {
    super(message);
    this.grouperLoaderStatus = grouperLoaderStatus1;
  }

  public OtherJobException(GrouperLoaderStatus grouperLoaderStatus1, Throwable cause) {
    super(cause);
    this.grouperLoaderStatus = grouperLoaderStatus1;
  }
  private GrouperLoaderStatus grouperLoaderStatus;
  
  public GrouperLoaderStatus getGrouperLoaderStatus() {
    return this.grouperLoaderStatus;
  }
}

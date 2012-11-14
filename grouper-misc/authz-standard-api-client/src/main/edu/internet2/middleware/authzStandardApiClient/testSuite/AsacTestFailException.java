package edu.internet2.middleware.authzStandardApiClient.testSuite;

/**
 * 
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AsacTestFailException extends RuntimeException {

  /**
   * 
   */
  public AsacTestFailException() {
    super();
  }

  /**
   * 
   * @param message
   * @param cause
   */
  public AsacTestFailException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 
   * @param message
   */
  public AsacTestFailException(String message) {
    super(message);
  }

  /**
   * 
   * @param cause
   */
  public AsacTestFailException(Throwable cause) {
    super(cause);
  }

}

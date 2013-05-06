package edu.internet2.middleware.subject.provider;

/**
 * exception thrown when the status queried by user is not in the statusesFromUser list
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class SubjectStatusInvalidException extends RuntimeException {

  /**
   * 
   */
  public SubjectStatusInvalidException() {
    super();
  }

  /**
   * 
   * @param message
   * @param cause
   */
  public SubjectStatusInvalidException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 
   * @param message
   */
  public SubjectStatusInvalidException(String message) {
    super(message);
  }

  /**
   * 
   * @param cause
   */
  public SubjectStatusInvalidException(Throwable cause) {
    super(cause);
  }

  
  
}

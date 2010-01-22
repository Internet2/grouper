/*
 * @author mchyzer
 * $Id: GrouperStaleObjectStateException.java,v 1.1 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.exception;


/**
 * grouper specific way to say that someone else has edited this object,
 * user should refresh object state and make changes again
 */
@SuppressWarnings("serial")
public class GrouperStaleObjectStateException extends RuntimeException {

  /**
   * 
   */
  public GrouperStaleObjectStateException() {
  }

  /**
   * @param message
   */
  public GrouperStaleObjectStateException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperStaleObjectStateException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperStaleObjectStateException(String message, Throwable cause) {
    super(message, cause);
  }

}

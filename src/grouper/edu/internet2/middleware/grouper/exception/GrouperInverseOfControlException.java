/*
 * @author mchyzer
 * $Id: GrouperInverseOfControlException.java,v 1.1 2009-02-07 20:16:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.exception;


/**
 * tunnel exceptions through inverse of control through this
 */
@SuppressWarnings("serial")
public class GrouperInverseOfControlException extends RuntimeException {

  /**
   * 
   */
  public GrouperInverseOfControlException() {
  }

  /**
   * @param message
   */
  public GrouperInverseOfControlException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperInverseOfControlException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperInverseOfControlException(String message, Throwable cause) {
    super(message, cause);
  }

}

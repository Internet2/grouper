/*
 * @author mchyzer
 * $Id: StemAddAlreadyExistsException.java,v 1.1 2009-03-15 20:20:46 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.exception;


/**
 *
 */
public class StemAddAlreadyExistsException extends StemAddException {

  /**
   * 
   */
  public StemAddAlreadyExistsException() {
    super();
  }

  /**
   * @param msg
   * @param cause
   */
  public StemAddAlreadyExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param msg
   */
  public StemAddAlreadyExistsException(String msg) {
    super(msg);
  }

  /**
   * @param cause
   */
  public StemAddAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}

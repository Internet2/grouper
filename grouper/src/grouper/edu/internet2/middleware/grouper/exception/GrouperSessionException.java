/*
 * @author mchyzer
 * $Id: GrouperSessionException.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.exception;


/**
 * Use this to tunnel exceptions through the GrouperSession
 * inverse of control
 */
@SuppressWarnings("serial")
public class GrouperSessionException extends RuntimeException {

  /**
   * @param cause
   */
  public GrouperSessionException(Throwable cause) {
    super(cause);
  }

}

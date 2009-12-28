/**
 * @author mchyzer
 * $Id: TooManyResultsWhenFilteringByGroupException.java,v 1.1 2009-12-28 06:08:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.subject;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;


/**
 * when too many results when searching for subjects and filtering by group
 */
@SuppressWarnings("serial")
public class TooManyResultsWhenFilteringByGroupException extends WsInvalidQueryException {

  /**
   * 
   */
  public TooManyResultsWhenFilteringByGroupException() {
    //nothing
  }

  /**
   * @param message
   */
  public TooManyResultsWhenFilteringByGroupException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public TooManyResultsWhenFilteringByGroupException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public TooManyResultsWhenFilteringByGroupException(String message, Throwable cause) {
    super(message, cause);

  }

}

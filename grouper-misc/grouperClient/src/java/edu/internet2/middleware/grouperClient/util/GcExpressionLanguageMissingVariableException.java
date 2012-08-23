package edu.internet2.middleware.grouperClient.util;


/**
 * @author mchyzer This class is thrown there is a missing variable in EL
 * @version $Id: NotConcurrentRevisionException.java,v 1.1 2004/05/02 05:14:59
 *          mchyzer Exp $
 */
@SuppressWarnings("serial")
public class GcExpressionLanguageMissingVariableException extends RuntimeException {

  /**
   *  
   */
  public GcExpressionLanguageMissingVariableException() {
    super();
  }

  /**
   * @param s
   */
  public GcExpressionLanguageMissingVariableException(String s) {
    super(s);
  }

  /**
   * @param message
   * @param cause
   */
  public GcExpressionLanguageMissingVariableException(String message, Throwable cause) {
    super(message, cause);
    
  }

  /**
   * @param cause
   */
  public GcExpressionLanguageMissingVariableException(Throwable cause) {
    super(cause);
    
  }
}
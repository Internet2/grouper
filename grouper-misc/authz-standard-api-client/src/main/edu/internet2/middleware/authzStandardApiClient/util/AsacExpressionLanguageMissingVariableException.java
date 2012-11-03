package edu.internet2.middleware.authzStandardApiClient.util;


/**
 * @author mchyzer This class is thrown there is a missing variable in EL
 * @version $Id: NotConcurrentRevisionException.java,v 1.1 2004/05/02 05:14:59
 *          mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AsacExpressionLanguageMissingVariableException extends RuntimeException {

  /**
   *  
   */
  public AsacExpressionLanguageMissingVariableException() {
    super();
  }

  /**
   * @param s
   */
  public AsacExpressionLanguageMissingVariableException(String s) {
    super(s);
  }

  /**
   * @param message
   * @param cause
   */
  public AsacExpressionLanguageMissingVariableException(String message, Throwable cause) {
    super(message, cause);
    
  }

  /**
   * @param cause
   */
  public AsacExpressionLanguageMissingVariableException(Throwable cause) {
    super(cause);
    
  }
}
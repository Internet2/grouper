/**
 * 
 */
package edu.internet2.middleware.grouper.ws.exceptions;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;

/**
 * @author mchyzer
 *
 */
public class GrouperWsException extends RuntimeException {

  /**
   * if should log stack
   */
  private boolean logStack = true;

  /**
   * builder method to assign if stacks should be logs
   * @param theLogStack
   * @return this for chaining
   */
  public GrouperWsException assignLogStack(boolean theLogStack) {
    this.logStack = theLogStack;
    return this;
  }
  
  /**
   * if should log stack
   * @return if log stack
   */
  public boolean isLogStack() {
    return this.logStack;
  }

  /**
   * 
   */
  public GrouperWsException() {
    
  }

  /**
   * @param message
   */
  public GrouperWsException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperWsException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperWsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public GrouperWsException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public static void logWarn(Object object) {
    if (object instanceof GrouperWsException) {
      
      GrouperWsException grouperWsException = (GrouperWsException)object;
      if (!grouperWsException.isLogStack()) {
        LOG.warn(grouperWsException.getMessage());
        return;
      }
    }
    LOG.warn(object);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperWsException.class);

  public static void logWarn(Object message, Exception exception) {
    if (exception instanceof GrouperWsException) {
      
      GrouperWsException grouperWsException = (GrouperWsException)exception;
      if (!grouperWsException.isLogStack()) {
        if (!GrouperUtil.isBlank(message)) {
          LOG.warn(message + ", " + grouperWsException.getMessage());
        } else {
          LOG.warn(grouperWsException.getMessage());
        }
        return;
      }
    }
    if (!GrouperUtil.isBlank(message)) {
      LOG.warn(message, exception);
    } else {
      LOG.warn(exception);
    }
    
  }
  public static void logError(Object object) {
    if (object instanceof GrouperWsException) {
      
      GrouperWsException grouperWsException = (GrouperWsException)object;
      if (!grouperWsException.isLogStack()) {
        LOG.error(grouperWsException.getMessage());
        return;
      }
    }
    LOG.error(object);
  }

  public static void logError(String message, Exception exception) {
    if (exception instanceof GrouperWsException) {
      
      GrouperWsException grouperWsException = (GrouperWsException)exception;
      if (!grouperWsException.isLogStack()) {
        if (!GrouperUtil.isBlank(message)) {
          LOG.error(message + ", " + grouperWsException.getMessage());
        } else {
          LOG.error(grouperWsException.getMessage());
        }
        return;
      }
    }
    if (!GrouperUtil.isBlank(message)) {
      LOG.error(message, exception);
    } else {
      LOG.error(exception);
    }
  }
  
}

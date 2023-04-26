package edu.internet2.middleware.grouper.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;


public class GrouperLogger implements Log {

  
  private Log wrappedLog = null;
  
  
  
  public GrouperLogger(Log wrappedLog) {
    this.wrappedLog = wrappedLog;
  }

  /**
   * 
   */
  private static ThreadLocal<GrouperLoggerState> grouperLoggerState = new InheritableThreadLocal<GrouperLoggerState>();
  
  
  public static GrouperLoggerState retrieveGrouperLoggerState(boolean createIfNotThere) {
    GrouperLoggerState theGrouperLoggerState = grouperLoggerState.get();
    if (createIfNotThere && theGrouperLoggerState == null) {
      theGrouperLoggerState = new GrouperLoggerState();
      assignGrouperLoggerState(theGrouperLoggerState);
    }
    return theGrouperLoggerState;
  }
  
  public static void assignGrouperLoggerState(GrouperLoggerState theGrouperLoggerState) {
    grouperLoggerState.set(theGrouperLoggerState);
  }
  
  public static void clearGrouperLoggerState() {
    grouperLoggerState.remove();
  }
  
  public static Object wrapLogMessage(Object message) {
    GrouperLoggerState grouperLoggerState = retrieveGrouperLoggerState(false);
    if (grouperLoggerState != null) {
      if (!StringUtils.isBlank(grouperLoggerState.getRequestId()) || !StringUtils.isBlank(grouperLoggerState.getCorrelationId())) {
        
        StringBuilder logMessage = new StringBuilder(GrouperUtil.stringValue(message));
        if (!StringUtils.isBlank(grouperLoggerState.getCorrelationId())) {
          logMessage.insert(0, "corrId: " + grouperLoggerState.getCorrelationId() + ", ");
        }
        if (!StringUtils.isBlank(grouperLoggerState.getRequestId())) {
          logMessage.insert(0, "reqId: " + grouperLoggerState.getRequestId() + ", ");
        }
        
        return logMessage.toString();
      }
    }
    return message;
  }
  
  @Override
  public void debug(Object message) {
    this.wrappedLog.debug(wrapLogMessage(message));
  }

  @Override
  public void debug(Object message, Throwable t) {
    this.wrappedLog.debug(wrapLogMessage(message), t);
  }

  @Override
  public void error(Object message) {
    this.wrappedLog.error(wrapLogMessage(message));
  }

  @Override
  public void error(Object message, Throwable t) {
    this.wrappedLog.error(wrapLogMessage(message), t);
  }

  @Override
  public void fatal(Object message) {
    this.wrappedLog.fatal(wrapLogMessage(message));
  }

  @Override
  public void fatal(Object message, Throwable t) {
    this.wrappedLog.fatal(wrapLogMessage(message), t);
  }

  @Override
  public void info(Object message) {
    this.wrappedLog.info(wrapLogMessage(message));
  }

  @Override
  public void info(Object message, Throwable t) {
    this.wrappedLog.info(wrapLogMessage(message), t);
  }

  @Override
  public boolean isDebugEnabled() {
    return this.wrappedLog.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return this.wrappedLog.isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return this.wrappedLog.isFatalEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return this.wrappedLog.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return this.wrappedLog.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return this.wrappedLog.isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    this.wrappedLog.trace(wrapLogMessage(message));
  }

  @Override
  public void trace(Object message, Throwable t) {
    this.wrappedLog.trace(wrapLogMessage(message), t);
  }

  @Override
  public void warn(Object message) {
    this.wrappedLog.warn(wrapLogMessage(message));
  }

  @Override
  public void warn(Object message, Throwable t) {
    this.wrappedLog.warn(wrapLogMessage(message), t);
  }

}

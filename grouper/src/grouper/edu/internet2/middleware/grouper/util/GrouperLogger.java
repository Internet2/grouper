package edu.internet2.middleware.grouper.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;


public class GrouperLogger implements Log {

  
  private Log wrappedLog2 = null;
  private long lastRetrieved = -1;

  /**
   * cache the logger, and retrieve every 10 seconds
   * @return
   */
  private Log wrappedLog() {
    // cache this for 10 seconds
    if (System.currentTimeMillis() - lastRetrieved < 1000 * 10) {
      return wrappedLog2;
    }
    wrappedLog2 = LogFactory.getLog(theClass);
    lastRetrieved = System.currentTimeMillis();
    return wrappedLog2;
  }
  
  private Class theClass = null;
  
  public GrouperLogger(Class theClassInput) {
    theClass = theClassInput;
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
    this.debug(message, null);
  }

  @Override
  public void debug(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.wrappedLog() instanceof Log4JLogger ) {
      ((Log4JLogger)this.wrappedLog()).getLogger().log(GrouperLogger.class.getName(), Level.DEBUG, wrapLogMessage, t);
    } else {
      this.wrappedLog().debug(wrapLogMessage);
    }
  }

  @Override
  public void error(Object message) {
    this.error(message, null);
  }

  @Override
  public void error(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.wrappedLog() instanceof Log4JLogger ) {
      ((Log4JLogger)this.wrappedLog()).getLogger().log(GrouperLogger.class.getName(), Level.ERROR, wrapLogMessage, t);
    } else {
      this.wrappedLog().debug(wrapLogMessage);
    }
  }

  @Override
  public void fatal(Object message) {
    this.fatal(message, null);
  }

  @Override
  public void fatal(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.wrappedLog() instanceof Log4JLogger ) {
      ((Log4JLogger)this.wrappedLog()).getLogger().log(GrouperLogger.class.getName(), Level.FATAL, wrapLogMessage, t);
    } else {
      this.wrappedLog().debug(wrapLogMessage);
    }
  }

  @Override
  public void info(Object message) {
    this.info(message, null);
  }

  @Override
  public void info(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.wrappedLog() instanceof Log4JLogger ) {
      ((Log4JLogger)this.wrappedLog()).getLogger().log(GrouperLogger.class.getName(), Level.INFO, wrapLogMessage, t);
    } else {
      this.wrappedLog().debug(wrapLogMessage);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return this.wrappedLog().isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return this.wrappedLog().isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return this.wrappedLog().isFatalEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return this.wrappedLog().isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return this.wrappedLog().isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return this.wrappedLog().isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    this.trace(message, null);
  }

  @Override
  public void trace(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.wrappedLog() instanceof Log4JLogger ) {
      ((Log4JLogger)this.wrappedLog()).getLogger().log(GrouperLogger.class.getName(), Level.TRACE, wrapLogMessage, t);
    } else {
      this.wrappedLog().debug(wrapLogMessage);
    }
  }

  @Override
  public void warn(Object message) {
    this.warn(message, null);
  }

  @Override
  public void warn(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.wrappedLog() instanceof Log4JLogger ) {
      ((Log4JLogger)this.wrappedLog()).getLogger().log(GrouperLogger.class.getName(), Level.WARN, wrapLogMessage, t);
    } else {
      this.wrappedLog().debug(wrapLogMessage);
    }
  }

}

package edu.internet2.middleware.grouper.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.ExtendedLogger;


public class GrouperLogger implements Log {


  private Log wrappedLog = null;

  /**
   * log4j2 extended logger, used to log with correct caller class name
   */
  private ExtendedLogger extendedLogger = null;


  public GrouperLogger(Log wrappedLog) {
    this.wrappedLog = wrappedLog;
  }

  /**
   * constructor with log4j2 extended logger for caller-aware logging
   * @param wrappedLog
   * @param loggerName
   */
  public GrouperLogger(Log wrappedLog, String loggerName) {
    this.wrappedLog = wrappedLog;
    org.apache.logging.log4j.Logger log4j2Logger = LogManager.getLogger(loggerName);
    if (log4j2Logger instanceof ExtendedLogger) {
      this.extendedLogger = (ExtendedLogger) log4j2Logger;
    }
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
    if (this.extendedLogger != null) {
      this.extendedLogger.logIfEnabled(GrouperLogger.class.getName(), Level.DEBUG, null, wrapLogMessage, t);
    } else {
      this.wrappedLog.debug(wrapLogMessage, t);
    }
  }

  @Override
  public void error(Object message) {
    this.error(message, null);
  }

  @Override
  public void error(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.extendedLogger != null) {
      this.extendedLogger.logIfEnabled(GrouperLogger.class.getName(), Level.ERROR, null, wrapLogMessage, t);
    } else {
      this.wrappedLog.error(wrapLogMessage, t);
    }
  }

  @Override
  public void fatal(Object message) {
    this.fatal(message, null);
  }

  @Override
  public void fatal(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.extendedLogger != null) {
      this.extendedLogger.logIfEnabled(GrouperLogger.class.getName(), Level.FATAL, null, wrapLogMessage, t);
    } else {
      this.wrappedLog.fatal(wrapLogMessage, t);
    }
  }

  @Override
  public void info(Object message) {
    this.info(message, null);
  }

  @Override
  public void info(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.extendedLogger != null) {
      this.extendedLogger.logIfEnabled(GrouperLogger.class.getName(), Level.INFO, null, wrapLogMessage, t);
    } else {
      this.wrappedLog.info(wrapLogMessage, t);
    }
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
    this.trace(message, null);
  }

  @Override
  public void trace(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.extendedLogger != null) {
      this.extendedLogger.logIfEnabled(GrouperLogger.class.getName(), Level.TRACE, null, wrapLogMessage, t);
    } else {
      this.wrappedLog.trace(wrapLogMessage, t);
    }
  }

  @Override
  public void warn(Object message) {
    this.warn(message, null);
  }

  @Override
  public void warn(Object message, Throwable t) {
    Object wrapLogMessage = wrapLogMessage(message);
    if (this.extendedLogger != null) {
      this.extendedLogger.logIfEnabled(GrouperLogger.class.getName(), Level.WARN, null, wrapLogMessage, t);
    } else {
      this.wrappedLog.warn(wrapLogMessage, t);
    }
  }

}

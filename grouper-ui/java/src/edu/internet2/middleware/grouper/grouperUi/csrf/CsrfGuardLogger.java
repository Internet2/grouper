/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.csrf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.csrfguard.log.ILogger;
import org.owasp.csrfguard.log.LogLevel;


/**
 *
 */
public class CsrfGuardLogger implements ILogger {

  /** logger */
  private static final Log LOG = LogFactory.getLog(CsrfGuardLogger.class);

  /**
   * @see org.owasp.csrfguard.log.ILogger#log(java.lang.String)
   */
  @Override
  public void log(String msg) {
    LOG.info(msg);
  }

  /**
   * @see org.owasp.csrfguard.log.ILogger#log(java.lang.Exception)
   */
  @Override
  public void log(Exception exception) {
    LOG.info("csrf", exception);
  }

  /**
   * @see org.owasp.csrfguard.log.ILogger#log(org.owasp.csrfguard.log.LogLevel, java.lang.String)
   */
  @Override
  public void log(LogLevel level, String msg) {
    switch(level) {
      case Debug:
        LOG.debug(msg);
        break;
      case Error:
        LOG.error(msg);
        break;
      case Fatal:
        LOG.fatal(msg);
        break;
      case Info:
        LOG.info(msg);
        break;
      case Trace:
        LOG.trace(msg);
        break;
      case Warning:
        LOG.warn(msg);
        break;
      default:
        throw new RuntimeException("Not expecting level: " + level);
    }
  }

  /**
   * @see org.owasp.csrfguard.log.ILogger#log(org.owasp.csrfguard.log.LogLevel, java.lang.Exception)
   */
  @Override
  public void log(LogLevel level, Exception exception) {
    switch(level) {
      case Debug:
        LOG.debug("csrf", exception);
        break;
      case Error:
        LOG.error("csrf", exception);
        break;
      case Fatal:
        LOG.fatal("csrf", exception);
        break;
      case Info:
        LOG.info("csrf", exception);
        break;
      case Trace:
        LOG.trace("csrf", exception);
        break;
      case Warning:
        LOG.warn("csrf", exception);
        break;
      default:
        throw new RuntimeException("Not expecting level: " + level);
    }
  }

}

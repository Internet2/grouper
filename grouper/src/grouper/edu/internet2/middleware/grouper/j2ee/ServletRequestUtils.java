/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.j2ee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperThreadLocalState;


/**
 *
 */
public class ServletRequestUtils {

  /** logger */
  private static Log LOG = LogFactory.getLog(ServletRequestUtils.class);

  /**
   * 
   */
  public ServletRequestUtils() {
  }

  /**
   * end of request in j2ee call this method, this is a failsafe method
   */
  public static void requestEnd() {
    try {
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.j2eeRequestEndRemoveThreadlocals", true)) {
        GrouperThreadLocalState.removeCurrentThreadLocals();
      }
    } catch (Exception e) {
      LOG.error("Error ending request", e);
    }

  }

}

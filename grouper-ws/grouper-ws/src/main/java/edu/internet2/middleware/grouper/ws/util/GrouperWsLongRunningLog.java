/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.util;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * logger for ws events
 */
public class GrouperWsLongRunningLog {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperWsLongRunningLog.class);

  /**
   * 
   * @return true if debug enabled
   */
  static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }
  
  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  @SuppressWarnings("static-access")
  public static void wsLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      try {
        int logOverMillis = GrouperWsConfig.retrieveConfig().propertyValueInt("ws.longRunningRequestLogMillis", 30000);
        long elapsedMillis = (System.nanoTime() - startTimeNanos) / 1000000;
        if (logOverMillis >= 0 && elapsedMillis >= logOverMillis) {
  
          if (messageMap != null && startTimeNanos != null && !messageMap.containsKey("elapsedMillis")) {
            messageMap.put("elapsedMillis", elapsedMillis);
          }
          LOG.debug(GrouperClientUtils.mapToString(messageMap));
        }
      } catch (RuntimeException re) {
        LOG.error("error", re);
      }
    }
  }

}

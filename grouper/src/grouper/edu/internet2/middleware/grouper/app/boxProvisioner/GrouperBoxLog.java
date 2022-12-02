package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * logger to log the traffic of box
 */
public class GrouperBoxLog {
  
  /** logger */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GrouperBoxLog.class);
 
  /**
   * log something to the log file
   * @param message
   */
  public static void boxLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  public static void boxLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

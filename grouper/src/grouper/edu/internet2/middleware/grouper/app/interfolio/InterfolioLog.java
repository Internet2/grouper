package edu.internet2.middleware.grouper.app.interfolio;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Simple logging utility for the Interfolio provisioner.
 */
public class InterfolioLog {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(InterfolioLog.class);

  /**
   * log a message to the log file
   * @param message the message
   */
  public static void interfolioLog(String message) {
    LOG.debug(message);
  }

  /**
   * log a debug map (with elapsed time) to the log file
   * @param messageMap the debug map
   * @param startTimeNanos nanos when the request started
   */
  public static void interfolioLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

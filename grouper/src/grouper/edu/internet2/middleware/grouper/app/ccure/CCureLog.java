package edu.internet2.middleware.grouper.app.ccure;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.logging.Log;

import java.util.Map;

public class CCureLog {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(CCureLog.class);

  /**
   * log something to the log file
   * @param message
   */
  public static void log(String message) {
    LOG.debug(message);
  }

  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  public static void log(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

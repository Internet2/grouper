package edu.internet2.middleware.grouper.app.datadog;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class DatadogLog {

  /** Logger  */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(DatadogLog.class);

  /**
   * log something to the log file
   * @param message the message to log
   */
  public static void datadogLog(String message) {
    LOG.debug(message);
  }

  /**
   * log something to the log file
   * @param messageMap a map of items to log
   * @param startTimeNanos to calculate elapsed time
   */
  public static void datadogLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }

  }

}

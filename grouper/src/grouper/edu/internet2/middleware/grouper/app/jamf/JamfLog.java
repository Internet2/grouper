package edu.internet2.middleware.grouper.app.jamf;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Simple debug-logging helper for the Jamf provisioner. Mirrors the logging
 * utility used by the other provisioners so HTTP calls can be traced with
 * elapsed time when debug logging is on.
 */
public class JamfLog {

  /** Logger */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(JamfLog.class);

  /**
   * log something to the log file
   * @param message the message to log
   */
  public static void jamfLog(String message) {
    LOG.debug(message);
  }

  /**
   * log a map of items to the log file, appending elapsed milliseconds
   * @param messageMap a map of items to log
   * @param startTimeNanos to calculate elapsed time (from System.nanoTime())
   */
  public static void jamfLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

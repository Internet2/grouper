package edu.internet2.middleware.grouper.app.dropbox;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Simple static logging utility for the Dropbox provisioner.
 */
public class DropboxLog {

  /** Logger */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(DropboxLog.class);

  /**
   * log something to the log file
   * @param message the message to log
   */
  public static void dropboxLog(String message) {
    LOG.debug(message);
  }

  /**
   * log a map of items, computing elapsed time from a start timestamp
   * @param messageMap a map of items to log
   * @param startTimeNanos to calculate elapsed time
   */
  public static void dropboxLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

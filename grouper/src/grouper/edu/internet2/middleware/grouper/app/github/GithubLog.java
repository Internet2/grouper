package edu.internet2.middleware.grouper.app.github;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Debug logging helper for the GitHub provisioner. Mirrors the pattern used by
 * the other provisioners (e.g. DatadogLog): each API command builds a debugMap
 * of what it did and logs it (with elapsed time) at debug level.
 */
public class GithubLog {

  /** Logger */
  private static final Log LOG = GrouperUtil.getLog(GithubLog.class);

  /**
   * log a simple message to the log file
   * @param message the message to log
   */
  public static void githubLog(String message) {
    LOG.debug(message);
  }

  /**
   * log a debug map to the log file, stamping elapsed time from a nanosecond start.
   * @param messageMap a map of items to log
   * @param startTimeNanos to calculate elapsed time (nanoseconds), or null to skip timing
   */
  public static void githubLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

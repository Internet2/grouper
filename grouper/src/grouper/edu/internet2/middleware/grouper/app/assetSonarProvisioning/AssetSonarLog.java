package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Simple debug-logging helper for the AssetSonar provisioner, so HTTP calls can be traced with
 * elapsed time when debug logging is on.
 */
public class AssetSonarLog {

  /** Logger */
  private static final Log LOG = GrouperUtil.getLog(AssetSonarLog.class);

  /**
   * log a map of items to the log file, appending elapsed milliseconds
   * @param messageMap a map of items to log
   * @param startTimeNanos to calculate elapsed time (from System.nanoTime())
   */
  public static void assetSonarLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

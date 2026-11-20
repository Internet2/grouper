package edu.internet2.middleware.grouper.app.libreChat;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Debug logging for the LibreChat provisioner's HTTP calls, with elapsed time
 *
 * GRP-7594
 */
public class LibreChatLog {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(LibreChatLog.class);

  /**
   * log a map of items, appending elapsed milliseconds
   * @param messageMap items to log
   * @param startTimeNanos from System.nanoTime()
   */
  public static void libreChatLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

}

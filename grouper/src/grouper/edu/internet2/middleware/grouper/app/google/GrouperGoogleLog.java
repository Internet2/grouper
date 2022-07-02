package edu.internet2.middleware.grouper.app.google;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperGoogleLog {

  /** logger */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GrouperGoogleLog.class);
 
  /**
   * log something to the log file
   * @param message
   */
  public static void googleLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  public static void googleLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }
  
}

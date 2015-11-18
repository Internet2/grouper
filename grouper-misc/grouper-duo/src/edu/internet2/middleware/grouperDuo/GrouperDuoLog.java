/**
 * @author mchyzer
 * $Id: TfRestLogicTrafficLog.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouperDuo;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * logger to log the traffic of duo
 */
public class GrouperDuoLog {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDuoLog.class);
 
  /**
   * log something to the log file
   * @param message
   */
  public static void duoLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  public static void duoLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      LOG.debug(GrouperUtil.mapToString(messageMap));
    }
  }

  
}

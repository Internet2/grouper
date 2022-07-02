/**
 * @author mchyzer
 * $Id: TfRestLogicTrafficLog.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;



/**
 * logger to log the traffic of box
 */
public class GrouperDuoRoleLog {

  /** logger */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GrouperDuoRoleLog.class);
 
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
      LOG.debug(GrouperClientUtils.mapToString(messageMap));
    }
  }

  
}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.util;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.coresoap.WsResultMeta;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * logger for ws events
 */
public class GrouperWsLog {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperWsLog.class);

  /**
   * 
   * @return true if debug enabled
   */
  static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }
  
  /**
   * 
   * @param messageMap 
   * @param key 
   * @param value 
   */
  public static void addToLogIfNotBlank(Map<String, Object> messageMap, String key, Object value) {
    
    if (!LOG.isDebugEnabled() || messageMap == null) {
      return;
    }
    if (value == null) {
      return;
    }
    
    if (value instanceof Throwable) {
      messageMap.put(key, ExceptionUtils.getFullStackTrace((Throwable)value));
      return;
    }
    
    if (value instanceof String && StringUtils.isBlank((String)value)) {
      return;
    }
    if (value.getClass().isArray() || value instanceof Collection) {
      if (GrouperUtil.length(value) > 0) {
        messageMap.put(key, GrouperServiceUtils.toStringForLog(value, 200));
      }
      return;
    }
    messageMap.put(key, GrouperServiceUtils.toStringForWsLog(value));
  }
  
  /**
   * 
   * @param messageMap 
   * @param wsResponseBean
   */
  public static void addToLog(Map<String, Object> messageMap, WsResponseBean wsResponseBean) {
    if (!LOG.isDebugEnabled()) {
      return;
    }

    if (wsResponseBean == null) {
      messageMap.put("responseBean", "null");
      return;
    }
    WsResultMeta resultMeta = wsResponseBean.getResultMetadata();
    if (resultMeta != null) {
      messageMap.put("success", resultMeta.getSuccess());
      messageMap.put("resultCode", resultMeta.getResultCode());
      if (!StringUtils.isBlank(resultMeta.getResultCode2())) {
        messageMap.put("resultCode2", resultMeta.getResultCode2());
      }
      //only put message if not success...
      if (!StringUtils.equals("T", resultMeta.getSuccess())) {
        messageMap.put("resultMessage", resultMeta.getResultMessage());
      }
    }
    WsResponseMeta responseMetadata = wsResponseBean.getResponseMetadata();
    if (responseMetadata != null) {
      messageMap.put("serverVersion", responseMetadata.getServerVersion());
      
      if (!StringUtils.isBlank(responseMetadata.getResultWarnings())) {
        messageMap.put("resultWarnings", responseMetadata.getResultWarnings());
      }
    }
  }
  
  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  @SuppressWarnings("static-access")
  public static void wsLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      try {
        if (messageMap != null && startTimeNanos != null) {
          messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
        }
        LOG.debug(GrouperClientUtils.mapToString(messageMap));
      } catch (RuntimeException re) {
        LOG.error("error", re);
      }
    }
  }

}

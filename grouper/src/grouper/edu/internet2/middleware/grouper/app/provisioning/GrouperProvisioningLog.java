package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningLog {

  /**
   * debug log
   * @param debugMap
   */
  public static void debugLog(Map<String, Object> debugMap) {
    debugLog(GrouperUtil.mapToString(debugMap));
  }

  /**
   * debug log
   * @param string
   */
  public static void debugLog(String string) {
    if (!LOG.isDebugEnabled()) {
      return;
    }
  }

  /**
   * @return if debug enabled
   */
  public static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLog.class);
  
}

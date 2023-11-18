package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * ldap sync log
 */
public class LdapSyncLog {

  /**
   * log object
   */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(LdapSyncLog.class);
  
  /**
   * debug log
   * @param debugMap
   */
  public static void debugLog(Map<String, Object> debugMap) {
    debugLog(GrouperClientUtils.mapToString(debugMap));
  }

  /**
   * debug log
   * @param string
   */
  public static void debugLog(String string) {
    if (isDebugEnabled()) {
      LOG.debug(string);
    }
  }

  /**
   * @return if debug enabled
   */
  public static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }
  
}

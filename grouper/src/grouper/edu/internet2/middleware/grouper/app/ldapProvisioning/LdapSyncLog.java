package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

/**
 * ldap sync log
 */
public class LdapSyncLog {

  /**
   * log object
   */
  private static final Log LOG = LogFactory.getLog(LdapSyncLog.class);
  
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

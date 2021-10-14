package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningLogCommands {

  /**
   * debug log
   * @param string
   */
  public static void debugLog(String string) {
    LOG.debug(string);
  }

  /**
   * debug log
   * @param string
   */
  public static void infoLog(String string) {
    LOG.info(string);
  }

  /**
   * debug log
   * @param string
   */
  public static void errorLog(String string) {
    LOG.error(string);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLogCommands.class);
  
}

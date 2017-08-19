/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * logger for loader events
 */
public class GrouperLoaderLog {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderLog.class);

  /**
   * 
   * @return true if debug enabled
   */
  static boolean isDebugEnabled() {
    return LOG.isDebugEnabled();
  }
  
  /**
   * 
   * @param theString
   */
  static void logDebug(String theString) {
    LOG.debug(theString);
  }
  
}

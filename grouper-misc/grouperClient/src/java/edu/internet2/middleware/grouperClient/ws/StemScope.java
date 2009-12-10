/*
 * @author mchyzer $Id: StemScope.java,v 1.1 2009-12-10 08:54:32 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * scope of groups under a stem.  either all or just one level (immediate)
 */
public enum StemScope {

  /** just direct immediate chlidren */
  ONE_LEVEL,
  
  /** all groups in this folder or subfolders */
  ALL_IN_SUBTREE;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static StemScope valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(StemScope.class, string, false);
  }
}

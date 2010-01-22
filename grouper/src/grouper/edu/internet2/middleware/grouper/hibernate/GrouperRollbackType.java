/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Various types of rolling back
 * @author mchyzer
 *
 */
public enum GrouperRollbackType {
  /** always rollback right now */
  ROLLBACK_NOW,
  
  /** only rollback if this is a new transaction */
  ROLLBACK_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static GrouperRollbackType valueOfIgnoreCase(String string) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperRollbackType.class,string, false );
  }

}

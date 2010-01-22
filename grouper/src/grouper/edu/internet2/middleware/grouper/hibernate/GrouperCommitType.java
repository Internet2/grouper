/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Various types of committing
 * @author mchyzer
 *
 */
public enum GrouperCommitType {
  /** always commit right now */
  COMMIT_NOW,
  
  /** only commit if this is a new transaction */
  COMMIT_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static GrouperCommitType valueOfIgnoreCase(String string) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperCommitType.class,string, false );
  }

}

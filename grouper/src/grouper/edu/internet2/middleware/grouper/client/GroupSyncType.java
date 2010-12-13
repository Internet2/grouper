package edu.internet2.middleware.grouper.client;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * group sync type is for syncing groups to/from other groupers
 */
public enum GroupSyncType {
  
  /** periodically push group list to another grouper */
  push,
  
  /** periodically pull group list from another grouper */
  pull,
  
  /** push changes as they happen to the group list to the other grouper */
  incremental_push;

  /**
   * see if incremental
   * @return true if incremental
   */
  public boolean isIncremental() {
    return this == incremental_push;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GroupSyncType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GroupSyncType.class, 
        string, exceptionOnNull);
  
  }
}
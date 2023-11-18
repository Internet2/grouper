package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperLoaderDisplayNameSyncType {
  
  BASE_FOLDER_NAME, LEVELS;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperLoaderDisplayNameSyncType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperLoaderDisplayNameSyncType.class, string, exceptionOnNull);
  
  }

}

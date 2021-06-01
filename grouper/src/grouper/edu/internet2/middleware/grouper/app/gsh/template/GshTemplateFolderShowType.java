package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateFolderShowType {

  certainFolder, allFolders;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateFolderShowType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateFolderShowType.class, string, exceptionOnNotFound);
  }
  
}

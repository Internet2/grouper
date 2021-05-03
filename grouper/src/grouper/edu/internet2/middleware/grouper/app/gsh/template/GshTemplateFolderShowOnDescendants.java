package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateFolderShowOnDescendants {

  certainFolder, oneChildLevel, certainFolderAndOneChildLevel, descendants, certainFolderAndDescendants;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateFolderShowOnDescendants valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateFolderShowOnDescendants.class, string, exceptionOnNotFound);
  }
      
}

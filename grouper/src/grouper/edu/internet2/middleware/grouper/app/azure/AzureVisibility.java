package edu.internet2.middleware.grouper.app.azure;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum AzureVisibility {
  
  Public, 
  
  Private, 
  
  HiddenMembership;
  

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AzureVisibility valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AzureVisibility.class, 
        string, exceptionOnNull);
  }

}
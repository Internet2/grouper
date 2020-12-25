package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningObjectMetadataItemFormElementType {
  
  /**
   * Form element type textfield, drop down
  Drop down options List the options or get them programmatically
   */
  
  TEXT, TEXTAREA, DROPDOWN;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningObjectMetadataItemFormElementType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningObjectMetadataItemFormElementType.class, 
        string, exceptionOnNull);

  }

}

package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningObjectMetadataItemFormElementType {
  
  /**
   * Form element type textfield, drop down
  Drop down options List the options or get them programmatically
   */
  
  TEXT, TEXTAREA, DROPDOWN, RADIOBUTTON;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningObjectMetadataItemFormElementType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningObjectMetadataItemFormElementType.class, 
        string, exceptionOnNotFound);

  }

}

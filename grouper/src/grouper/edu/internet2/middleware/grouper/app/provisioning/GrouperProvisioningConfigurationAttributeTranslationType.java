package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningConfigurationAttributeTranslationType {

  grouperProvisioningGroupField,
  grouperProvisioningEntityField,
  staticValues,
  translationScript;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningConfigurationAttributeTranslationType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningConfigurationAttributeTranslationType.class, 
        string, exceptionOnNotFound);
  }

}

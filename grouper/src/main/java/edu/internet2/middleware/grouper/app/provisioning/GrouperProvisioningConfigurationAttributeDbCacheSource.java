package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.attestation.AttestationType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningConfigurationAttributeDbCacheSource {
  grouper, target;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnBlank will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningConfigurationAttributeDbCacheSource valueOfIgnoreCase(String string, boolean exceptionOnBlank) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningConfigurationAttributeDbCacheSource.class, 
        string, exceptionOnBlank);
  }

}

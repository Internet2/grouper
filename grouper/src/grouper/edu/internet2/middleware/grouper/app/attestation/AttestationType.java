package edu.internet2.middleware.grouper.app.attestation;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum AttestationType {
  
  group, report;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttestationType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(AttestationType.class, 
        string, exceptionOnNotFound);
  }

}

package edu.internet2.middleware.tierApiAuthzClient.corebeans;

import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;

/**
 * save mode for saving an object
 */
public enum AsacSaveMode {
  
  /** only insert this object */
  INSERT,
  
  /**
   * only update this object */
  UPDATE,
  
  /**
   * insert or update this object
   */
  INSERT_OR_UPDATE;
  
  /**
   * 
   * @param string
   * @param exceptionOnBlank
   * @return the asacSaveMode
   */
  public static AsacSaveMode valueOfIgnoreCase(String string, boolean exceptionOnBlank) {
    
    return StandardApiClientUtils.enumValueOfIgnoreCase(AsacSaveMode.class,string, exceptionOnBlank );
  }
  
}


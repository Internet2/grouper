package edu.internet2.middleware.authzStandardApiClient.corebeans;

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
}


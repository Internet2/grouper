package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans;

/**
 * save mode for saving an object
 */
public enum AsasSaveMode {
  
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


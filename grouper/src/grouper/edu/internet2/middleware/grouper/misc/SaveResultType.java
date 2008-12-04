package edu.internet2.middleware.grouper.misc;

/**
 * save type
 */
public enum SaveResultType {

  /** group was inserted */
  INSERT,
  
  /** group was updated */
  UPDATE,
  
  /** group didnt need an update */
  NO_CHANGE;
}
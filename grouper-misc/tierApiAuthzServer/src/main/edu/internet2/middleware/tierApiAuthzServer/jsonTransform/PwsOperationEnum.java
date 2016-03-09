/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;

/**
 * the operation that is being performed
 */
public enum PwsOperationEnum {

  /**
   * had problem parsing it, this is invalid
   */
  invalidOperation,
  
  /** null string in operation is just a noop */
  nullOperation,
  
  /** assigning data from one location to another */
  assign;
}
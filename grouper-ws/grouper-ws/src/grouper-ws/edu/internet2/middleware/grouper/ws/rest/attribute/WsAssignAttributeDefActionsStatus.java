/**
 * 
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;


/**
 * @author vsachdeva
 * 
 */
public enum WsAssignAttributeDefActionsStatus {
  
  /** Added action to attribute def**/
  ADDED, 
  
  /** Deleted action from attribute def **/
  DELETED,
  
  /** action not found in attribute def while deleting, replacing**/
  NOT_FOUND,
  
  /** action is already assigned **/
  ASSIGNED_ALREADY

}

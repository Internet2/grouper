/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;


/**
 *
 */
public enum GrouperActivemqPermissionAction {

  /** send a message */
  sendMessage,
  
  /** send a message on this destination or sub destinations */
  sendMessageInherit,
  
  /** receive a message */
  receiveMessage,
  
  /** receive a message on this destination or sub destinations */
  receiveMessageInherit,
  
  /** create a queue/topic in this destination or somewhere under this destination */
  createDestination,
  
  /** delete a queue/topic in this destinate or somewhere under this destination */
  deleteDestination;
  
}

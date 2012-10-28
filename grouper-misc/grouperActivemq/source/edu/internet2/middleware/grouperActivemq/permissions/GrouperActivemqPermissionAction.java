/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * action for a permission assignment
 */
public enum GrouperActivemqPermissionAction {

  /** send a message */
  sendMessage {

    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return grouperActivemqPermissionAction == sendMessageInherit;
    }
  },
  
  /** send a message on this destination or sub destinations */
  sendMessageInherit {

    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return false;
    }
  },
  
  /** receive a message */
  receiveMessage {

    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return grouperActivemqPermissionAction == receiveMessageInherit;
    }
  },
  
  /** receive a message on this destination or sub destinations */
  receiveMessageInherit {

    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return false;
    }
  },
  
  /** create a queue/topic in this destination or somewhere under this destination */
  createDestination {

    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return grouperActivemqPermissionAction == createDestinationInherit;
    }
  },
  
  /** delete a queue/topic in this destinate or somewhere under this destination */
  deleteDestination {

    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return grouperActivemqPermissionAction == deleteDestinationInherit;
    }
  }, 
  
  /** create a queue/topic in this destination or somewhere under this destination */
  createDestinationInherit {
  
    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return false;
    }
  }, 
  
  /** delete a queue/topic in this destinate or somewhere under this destination */
  deleteDestinationInherit {
  
    @Override
    public boolean actionAllowedIfArgumentAllowed(
        GrouperActivemqPermissionAction grouperActivemqPermissionAction) {
      return false;
    }
  };
 
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound
   * @return the enum or null or exception if not found
   */
  public static GrouperActivemqPermissionAction valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GrouperActivemqPermissionAction.class,string, exceptionOnNotFound );
  }

  /**
   * 
   * @param grouperActivemqPermissionAction
   * @return true if this action is allowed since argument is allowed
   */
  public abstract boolean actionAllowedIfArgumentAllowed(GrouperActivemqPermissionAction grouperActivemqPermissionAction);
  
}

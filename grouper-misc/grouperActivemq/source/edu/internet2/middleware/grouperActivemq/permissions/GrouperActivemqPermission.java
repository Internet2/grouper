/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;


/**
 * permission object assigned to a user
 */
public class GrouperActivemqPermission {

  /** action for this permission */
  private GrouperActivemqPermissionAction action;
  
  /** fully qualified grouper destination for this permission: could end in __queue or __topic to be specific.
   * could have a starting prefix to keep the destinations in a certain folder */
  private String destination;
  
  /**
   * action for this permission
   * @return the action
   */
  public GrouperActivemqPermissionAction getAction() {
    return this.action;
  }
  
  /**
   * action for this permission
   * @param action1 the action to set
   */
  public void setAction(GrouperActivemqPermissionAction action1) {
    this.action = action1;
  }
  
  /**
   * fully qualified grouper destination for this permission: could end in __queue or __topic to be specific.
   * could have a starting prefix to keep the destinations in a certain folder
   * @return the destination
   */
  public String getDestination() {
    return this.destination;
  }
  
  /**
   * fully qualified grouper destination for this permission: could end in __queue or __topic to be specific.
   * could have a starting prefix to keep the destinations in a certain folder
   * @param destination1 the destination to set
   */
  public void setDestination(String destination1) {
    this.destination = destination1;
  }
}

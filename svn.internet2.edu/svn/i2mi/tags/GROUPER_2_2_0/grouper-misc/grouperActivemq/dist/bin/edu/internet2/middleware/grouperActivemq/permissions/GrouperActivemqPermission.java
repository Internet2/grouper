/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;

import edu.internet2.middleware.grouperActivemq.utils.GrouperActivemqUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * permission object assigned to a user
 */
public class GrouperActivemqPermission implements Comparable<GrouperActivemqPermission> {

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "GAPermission [action=" + this.action + ", destination="
        + this.destination + "]";
  }

  /**
   * default constructor
   */
  public GrouperActivemqPermission() {
  }

  /**
   * @param action1
   * @param destination1
   */
  public GrouperActivemqPermission(GrouperActivemqPermissionAction action1,
      String destination1) {
    this.action = action1;
    this.destination = destination1;
  }

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

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(GrouperActivemqPermission grouperActivemqPermission) {
    if (grouperActivemqPermission == null) {
      return -1;
    }
    
    
    
    if (!GrouperClientUtils.equals(this.action, grouperActivemqPermission.action)) {
      return GrouperActivemqUtils.compareTo(this.action, grouperActivemqPermission.action);
    }
    return GrouperActivemqUtils.compareTo(this.destination, grouperActivemqPermission.destination);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
    result = prime * result
        + ((this.destination == null) ? 0 : this.destination.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GrouperActivemqPermission)) {
      return false;
    }
    GrouperActivemqPermission other = (GrouperActivemqPermission) obj;
    return GrouperClientUtils.equals(this.action, other.action) 
      && GrouperClientUtils.equals(this.destination, other.destination);
  }
  
  
}

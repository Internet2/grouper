/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import edu.internet2.middleware.grouper.Group;


/**
 * Result of one group being retrieved since a user is a member of it.  The number of
 * groups will equal the number of groups the user is a member of (provided the filter matches)
 * 
 * @author mchyzer
 */
public class GuiGroup {

  /** group */
  private Group group;
  
  /**
   * return the group
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * 
   */
  public GuiGroup() {
    
  }
  
  /**
   * 
   * @param theGroup
   */
  public GuiGroup(Group theGroup) {
    this.group = theGroup;
  }
}

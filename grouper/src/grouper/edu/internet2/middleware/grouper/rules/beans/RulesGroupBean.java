/**
 * 
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * @author mchyzer
 *
 */
public class RulesGroupBean extends RulesBean {

  /**
   * 
   */
  public RulesGroupBean() {
    
  }
  
  /**
   * 
   * @param group
   */
  public RulesGroupBean(Group group) {
    super();
    this.group = group;
  }


  /** group */
  private Group group;

  /**
   * group
   * @return group
   */
  @Override
  public Group getGroup() {
    return group;
  }

  /**
   * group
   * @param group1
   */
  public void setGroup(Group group1) {
    this.group = group1;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.group != null) {
      result.append("group: ").append(this.group.getName()).append(", ");
    }
    return result.toString();
  }
  
  
}

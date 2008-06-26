/*
 * @author mchyzer
 * $Id: HooksGroupPreInsertBean.java,v 1.4 2008-06-26 11:16:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * pre insert bean
 */
public class HooksGroupPreInsertBean extends HooksBean {

  /** object being inserted */
  private Group group = null;
  
  /**
   * @param theGroup
   */
  public HooksGroupPreInsertBean(Group theGroup) {
    this.group = theGroup;
  }
  
  /**
   * object being inserted
   * @return the Group
   */
  public Group getGroup() {
    return this.group;
  }
}

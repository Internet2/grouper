/*
 * @author mchyzer
 * $Id: HooksGroupBean.java,v 1.2 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * bean to hold objects for group low level hooks
 */
public class HooksGroupBean extends HooksBean {
  
  /** object being affected */
  private Group group = null;
  
  /**
   * @param theGroup
   */
  public HooksGroupBean(Group theGroup) {
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

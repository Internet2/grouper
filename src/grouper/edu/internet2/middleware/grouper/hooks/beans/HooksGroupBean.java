/*
 * @author mchyzer
 * $Id: HooksGroupBean.java,v 1.1 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 *
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

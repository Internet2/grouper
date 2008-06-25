/*
 * @author mchyzer
 * $Id: HooksGroupPreInsertBean.java,v 1.3 2008-06-25 05:46:06 mchyzer Exp $
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
   * @param theHooksContext
   * @param theGroup
   */
  public HooksGroupPreInsertBean(HooksContext theHooksContext, Group theGroup) {
    super(theHooksContext);
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

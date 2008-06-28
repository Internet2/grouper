/*
 * @author mchyzer
 * $Id: HooksGroupTypeTupleBean.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.GroupTypeTuple;


/**
 * bean to hold objects for group low level hooks
 */
public class HooksGroupTypeTupleBean extends HooksBean {
  
  /** object being affected */
  private GroupTypeTuple groupTypeTuple = null;
  
  /**
   * @param theGroupTypeTuple
   */
  public HooksGroupTypeTupleBean(GroupTypeTuple theGroupTypeTuple) {
    this.groupTypeTuple = theGroupTypeTuple;
  }
  
  /**
   * object being inserted
   * @return the Group
   */
  public GroupTypeTuple getGroupTypeTuple() {
    return this.groupTypeTuple;
  }

}

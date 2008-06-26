/*
 * @author mchyzer
 * $Id: HooksGroupPreUpdateBean.java,v 1.4 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * pre update bean
 */
public class HooksGroupPreUpdateBean extends HooksGroupBean {
  /**
   * @param theGroup
   */
  public HooksGroupPreUpdateBean(Group theGroup) {
    super(theGroup);
  }

  
}

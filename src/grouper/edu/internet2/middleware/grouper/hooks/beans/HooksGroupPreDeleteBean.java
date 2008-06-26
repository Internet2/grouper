/*
 * @author mchyzer
 * $Id: HooksGroupPreDeleteBean.java,v 1.4 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * pre delete bean
 */
public class HooksGroupPreDeleteBean extends HooksGroupBean {

  
  /**
   * @param theGroup
   */
  public HooksGroupPreDeleteBean(Group theGroup) {
    super(theGroup);
  }
}

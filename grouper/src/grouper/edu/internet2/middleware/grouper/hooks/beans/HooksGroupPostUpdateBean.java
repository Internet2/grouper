/*
 * @author mchyzer
 * $Id: HooksGroupPostUpdateBean.java,v 1.4 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * post update bean
 */
public class HooksGroupPostUpdateBean extends HooksGroupBean {

  /**
   * @param theGroup
   */
  public HooksGroupPostUpdateBean(Group theGroup) {
    super(theGroup);
  }

}

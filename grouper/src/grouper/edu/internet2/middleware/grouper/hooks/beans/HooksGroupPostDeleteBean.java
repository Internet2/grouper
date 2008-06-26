/*
 * @author mchyzer
 * $Id: HooksGroupPostDeleteBean.java,v 1.4 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * post delete bean
 */
public class HooksGroupPostDeleteBean extends HooksGroupBean {

  /**
   * @param theGroup
   */
  public HooksGroupPostDeleteBean(Group theGroup) {
    super(theGroup);
  }

}

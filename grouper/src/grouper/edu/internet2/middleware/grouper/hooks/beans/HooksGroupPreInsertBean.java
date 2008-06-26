/*
 * @author mchyzer
 * $Id: HooksGroupPreInsertBean.java,v 1.5 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * pre insert bean
 */
public class HooksGroupPreInsertBean extends HooksGroupBean {

  /**
   * @param theGroup
   */
  public HooksGroupPreInsertBean(Group theGroup) {
    super(theGroup);
  }

}

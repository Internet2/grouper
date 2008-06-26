/*
 * @author mchyzer
 * $Id: HooksGroupPostInsertBean.java,v 1.4 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Group;


/**
 * post insert bean
 */
public class HooksGroupPostInsertBean extends HooksGroupBean {

  /**
   * @param theGroup
   */
  public HooksGroupPostInsertBean(Group theGroup) {
    super(theGroup);
  }
  
  
}

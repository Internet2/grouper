/*
 * @author mchyzer
 * $Id: HooksGrouperSessionBean.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.GrouperSession;


/**
 * bean to hold objects for grouper session low level hooks
 */
public class HooksGrouperSessionBean extends HooksBean {
  
  /** object being affected */
  private GrouperSession grouperSession = null;
  
  /**
   * @param theGrouperSession
   */
  public HooksGrouperSessionBean(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
  }
  
  /**
   * object being inserted
   * @return the Group
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }

}

/*
 * @author mchyzer
 * $Id: HooksGroupPreInsertBean.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.internal.dao.GroupDAO;


/**
 * pre update bean
 */
public class HooksGroupPreInsertBean extends HooksBean {

  /** object being inserted */
  private GroupDAO groupDao = null;
  
  /**
   * @param theHooksContext
   * @param theGroupDao 
   */
  public HooksGroupPreInsertBean(HooksContext theHooksContext, GroupDAO theGroupDao) {
    super(theHooksContext);
    this.groupDao = theGroupDao;
  }
  
  /**
   * object being inserted
   * @return the hib3GroupDAO
   */
  public GroupDAO getGroupDao() {
    return this.groupDao;
  }

  
  
}

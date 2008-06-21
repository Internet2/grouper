/*
 * @author mchyzer
 * $Id: GroupHooks.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPostDeleteBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPostInsertBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPostUpdateBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreDeleteBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreUpdateBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * group related actions
 */
public abstract class GroupHooks {

  /**
   * called right before a group update
   * @param preUpdateBean
   */
  public void groupPreUpdate(HooksGroupPreUpdateBean preUpdateBean) {
    
  }
  
  /**
   * called right after a group update
   * @param postUpdateBean
   */
  public void groupPostUpdate(HooksGroupPostUpdateBean postUpdateBean) {
    
  }
  
  /**
   * called right before a group insert
   * @param preInsertBean
   */
  public void groupPreInsert(HooksGroupPreInsertBean preInsertBean) {
    
  }
  
  /**
   * called right after a group insert
   * @param postInsertBean
   */
  public void groupPostInsert(HooksGroupPostInsertBean postInsertBean) {
    
  }
  
  /**
   * called right before a group delete
   * @param preDeleteBean
   */
  public void groupPreDelete(HooksGroupPreDeleteBean preDeleteBean) {
    
  }
  
  /**
   * called right after a group insert
   * @param postDeleteBean
   */
  public void groupPostDelete(HooksGroupPostDeleteBean postDeleteBean) {
    
  }
  
}

/*
 * @author mchyzer
 * $Id: MembershipHooks.java,v 1.1.2.1 2008-06-09 19:26:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostDeleteBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostInsertBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostUpdateBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostUpdateHighLevelBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreDeleteBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreInsertBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreUpdateBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreUpdateHighLevelBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * membership related actions
 */
public abstract class MembershipHooks {

  /**
   * called right before a membership update
   * @param preUpdateBean
   */
  public void membershipPreUpdate(HooksMembershipPreUpdateBean preUpdateBean) {
    
  }
  
  /**
   * called right after a membership update
   * @param postUpdateBean
   */
  public void membershipPostUpdate(HooksMembershipPostUpdateBean postUpdateBean) {
    
  }
  
  /**
   * called right before a membership update (high level, not the side effects)
   * @param preUpdateHighLevelBean
   */
  public void membershipPreUpdateHighLevel(
      HooksMembershipPreUpdateHighLevelBean preUpdateHighLevelBean) {
    
  }
  
  /**
   * called right after a membership update (high level, not the side effects)
   * @param postUpdateHighLevelBean
   */
  public void membershipPostUpdateHighLevel(
      HooksMembershipPostUpdateHighLevelBean postUpdateHighLevelBean) {
    
  }
  
  /**
   * called right before a membership insert
   * @param preInsertBean
   */
  public void membershipPreInsert(HooksMembershipPreInsertBean preInsertBean) {
    
  }
  
  /**
   * called right after a membership insert
   * @param postInsertBean
   */
  public void membershipPostInsert(HooksMembershipPostInsertBean postInsertBean) {
    
  }
  
  /**
   * called right before a membership delete
   * @param preDeleteBean
   */
  public void membershipPreDelete(HooksMembershipPreDeleteBean preDeleteBean) {
    
  }
  
  /**
   * called right after a membership insert
   * @param postDeleteBean
   */
  public void membershipPostDelete(HooksMembershipPostDeleteBean postDeleteBean) {
    
  }
  
}

/*
 * @author mchyzer
 * $Id: LifecycleHooksImpl.java,v 1.1 2008-07-10 00:46:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleGrouperStartupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHibInitBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHooksInitBean;


/**
 *
 */
public class LifecycleHooksImpl extends LifecycleHooks {

  /** proof that we hit grouper startup */
  static boolean hitGrouperStartup = false;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.LifecycleHooks#grouperStartup(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleGrouperStartupBean)
   */
  @Override
  public void grouperStartup(HooksContext hooksContext,
      HooksLifecycleGrouperStartupBean hooksLifecycleStartupBean) {
    //this is registered via reflection in GrouperHookType
    hitGrouperStartup = true;
    //System.err.println("Grouper startup hook");
  }

  /** proof that we hit hibernate init */
  static boolean hitHibernateInit = false;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.LifecycleHooks#hibernateInit(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHibInitBean)
   */
  @Override
  public void hibernateInit(HooksContext hooksContext,
      HooksLifecycleHibInitBean hooksLifecycleHibInitBean) {
    //this is registered via reflection in GrouperHookType
    //System.err.println("Hibernate init hook");
    hitHibernateInit = true;
  }

  /** proof that we hit hooks init */
  static boolean hitHooksInit = false;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.LifecycleHooks#hooksInit(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHooksInitBean)
   */
  @Override
  public void hooksInit(HooksContext hooksContext,
      HooksLifecycleHooksInitBean hooLifecycleHooksInitBean) {
    //this is registered via reflection in GrouperHookType
    hitHooksInit=true;
    //System.err.println("Hooks init hook");
  }

}

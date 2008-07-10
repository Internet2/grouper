/*
 * @author mchyzer
 * $Id: LifecycleHooks.java,v 1.1 2008-07-10 00:46:54 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleGrouperStartupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHibInitBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHooksInitBean;


/**
 * hooks regarding general grouper lifecycle (e.g. startup, hooks init, hib registration, etc)
 */
public class LifecycleHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: grouperStartup */
  public static final String METHOD_GROUPER_STARTUP = "grouperStartup";

  /** constant for method name for: hibernateInit */
  public static final String METHOD_HIBERNATE_INIT = "hibernateInit";

  /** constant for method name for: hooksInit */
  public static final String METHOD_HOOKS_INIT = "hooksInit";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//
  
  /**
   * called when grouper starts up (note, this might be too early to do anything complex)
   * @param hooksContext
   * @param hooksLifecycleStartupBean
   */
  public void grouperStartup(HooksContext hooksContext, HooksLifecycleGrouperStartupBean hooksLifecycleStartupBean) {
    
  }

  /**
   * called when grouper starts up
   * @param hooksContext
   * @param hooLifecycleHooksInitBean
   */
  public void hooksInit(HooksContext hooksContext, HooksLifecycleHooksInitBean hooLifecycleHooksInitBean) {
    
  }

  /**
   * called when hibernate is registering objects (you can add your own hibernate mapped objects here)
   * @param hooksContext
   * @param hooksLifecycleHibInitBean
   */
  public void hibernateInit(HooksContext hooksContext, HooksLifecycleHibInitBean hooksLifecycleHibInitBean) {
    
  }

}

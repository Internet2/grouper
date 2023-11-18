/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: LifecycleHooks.java,v 1.2 2008-07-27 07:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleDdlInitBean;
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

  /** constant for method name for: ddlInit */
  public static final String METHOD_DDL_INIT = "ddlInit";

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

  /**
   * called when ddl is checking objects (you can add your own ddl object types here)
   * @param hooksContext
   * @param hooksLifecycleDdlInitBean
   */
  public void ddlInit(HooksContext hooksContext, HooksLifecycleDdlInitBean hooksLifecycleDdlInitBean) {
    
  }
}

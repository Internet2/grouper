/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.util;

import org.apache.commons.logging.Log;
import org.apache.log4j.NDC;

import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderLogger;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader.GrouperLoaderDryRunBean;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextType;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.morphString.Crypto;


/**
 * keep state of thread local to propagate to worker threads
 */
public class GrouperThreadLocalState {

  /**
   * 
   */
  private GrouperContext grouperContextCurrentInnerContext = null;
  
  /**
   * context around a web request (e.g. UI or WS)
   */
  private GrouperContext grouperContextCurrentOuterContext = null;
  
  /**
   * default settings if not another set
   */
  private GrouperContext grouperContextDefaultContext = null;
  
  /**
   * if a dry run
   */
  private GrouperLoaderDryRunBean grouperLoaderDryRunBean = null;
  
  /**
   * if readonly mode
   */
  private Boolean hibernateSessionReadonlyMode = null;

  /**
   * context type
   */
  private GrouperContextType grouperContextType = null;
  
  /**
   * grouper source adapter
   */
  private boolean grouperSourceAdapterSearchWithReadPrivilege = false;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperThreadLocalState.class);

  /**
   * store current thread locals here
   */
  public void storeCurrentThreadLocals() {
    this.grouperContextCurrentInnerContext = GrouperContext.internal_retrieveCurrentInnerContext();
    this.grouperContextCurrentOuterContext = GrouperContext.internal_retrieveCurrentOuterContext();
    this.grouperContextDefaultContext = GrouperContext.internal_retrieveDefaultContext();
    this.grouperLoaderDryRunBean = GrouperLoader.internal_retrieveThreadLocalGrouperLoaderDryRun();
    this.hibernateSessionReadonlyMode = HibernateSession.internal_retrieveThreadlocalReadonly();
    this.grouperContextType = GrouperContextTypeBuiltIn._internal_getThreadLocalGrouperContextType();
    this.grouperSourceAdapterSearchWithReadPrivilege = GrouperSourceAdapter.searchForGroupsWithReadPrivilege();
  }
  
  /**
   * assign current thread locals here
   */
  public void assignCurrentThreadLocals() {
    GrouperContext.internal_assignCurrentInnerContext(this.grouperContextCurrentInnerContext);
    GrouperContext.internal_assignCurrentOuterContext(this.grouperContextCurrentOuterContext);
    GrouperContext.internal_assignDefaultContext(this.grouperContextDefaultContext);
    GrouperLoader.internal_assignThreadLocalGrouperLoaderDryRun(this.grouperLoaderDryRunBean);
    HibernateSession.internal_assignThreadlocalReadonly(this.hibernateSessionReadonlyMode);
    GrouperContextTypeBuiltIn.setThreadLocalContext(this.grouperContextType);
    GrouperSourceAdapter.searchForGroupsWithReadPrivilege(this.grouperSourceAdapterSearchWithReadPrivilege);
  }
  
  /**
   * remove current thread locals (e.g. at end of request)
   */
  @SuppressWarnings("rawtypes")
  public static void removeCurrentThreadLocals() {
    GrouperContext.internal_assignCurrentInnerContext(null);
    GrouperContext.internal_assignCurrentOuterContext(null);
    GrouperContext.internal_assignDefaultContext(null);
    GrouperLoader.internal_assignThreadLocalGrouperLoaderDryRun(null);
    HibernateSession.internal_assignThreadlocalReadonly(null);
    GrouperContextTypeBuiltIn.setThreadLocalContext(null);
    GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
    GrouperLoaderLogger.removeThreadLocalMaps();
    NDC.remove();

    for (Class theClass : new Class[]{edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto.class, Crypto.class}) {

      //try to get the Crypto one
      try {
        ThreadLocal threadLocalCrypto = (ThreadLocal)GrouperUtil.fieldValue(theClass, null, "threadLocalCrypto", false, true, false);
        if (threadLocalCrypto != null) {
          threadLocalCrypto.remove();
        }
      } catch (Exception e) {
        LOG.error("cant clear Crypto threadlocal: " + theClass.getName(), e);
      }
      

    }
        
  }
  
  
  /**
   * 
   */
  public GrouperThreadLocalState() {
  }

}

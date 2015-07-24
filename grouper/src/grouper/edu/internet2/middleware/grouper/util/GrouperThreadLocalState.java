/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader.GrouperLoaderDryRunBean;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextType;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;


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
   * 
   */
  public GrouperThreadLocalState() {
  }

}

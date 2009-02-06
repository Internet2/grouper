/*
 * @author mchyzer
 * $Id: GrouperContext.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * holds threadlocal information about the current context of the database transactions
 */
public class GrouperContext {
  
  /** ip address of caller */
  private String callerIpAddress;
  
  /**
   * private data in context
   */
  private static class GrouperContextPrivate {

    /** context is audited */
    private boolean contextAudited;

    /** id of the current context, this is lazy loaded */
    private String contextId;
    
    /**
     * if context is audited
     * @return true if context is audited
     */
    public boolean isContextAudited() {
      return this.contextAudited;
    }
    
    /**
     * if context is audited
     * @param contextIsAudited
     */
    public void setContextAudited(boolean contextIsAudited) {
      this.contextAudited = contextIsAudited;
    }

    /**
     * context id
     * @return context id
     */
    public String getContextId() {
      if (this.contextId == null) {
        this.contextId = GrouperUuid.getUuid();
      }
      return this.contextId;
    }

  }
  
  
  
  /**
   * 
   */
  private static ThreadLocal<GrouperContext> currentContext = 
    new ThreadLocal<GrouperContext>();
  
  /**
   * 
   */
  private static ThreadLocal<GrouperContextPrivate> currentPrivateContext = 
    new ThreadLocal<GrouperContextPrivate>();
  
  /**
   * 
   * @param contextAudited1
   */
  static void setContextAudited(boolean contextAudited1) {
    currentPrivateContext.get().setContextAudited(contextAudited1);
  }
  
  /**
   * 
   * @return contextIsAudited1
   */
  public static boolean contextIsAudited() {
    return currentPrivateContext.get().isContextAudited();
  }

  /**
   * retrieve current context id
   * @param requireContext true to require context (if required in grouper.properties)
   * @return context id
   */
  public static String retrieveContextId(boolean requireContext) {
    GrouperContextPrivate grouperContextPrivate = currentPrivateContext.get();
    if (grouperContextPrivate == null) {
      if (requireContext) {
        //TODO throw exception if configured in grouper.properties
      }
      return null;
    }
    return grouperContextPrivate.getContextId();
  }
  
  /**
   * create a new context if one doesnt already exist
   * @return true if created one, false if already existed
   */
  static boolean createNewPrivateContextIfNotExist() {
    if (currentPrivateContext.get() != null) {
      return false;
    }
    GrouperContextPrivate grouperContextPrivate = new GrouperContextPrivate();
    currentPrivateContext.set(grouperContextPrivate);
    return true;
  }

  /**
   * delete the private context if just created
   */
  static void deletePrivateContext() {
    currentPrivateContext.remove();
  }
}

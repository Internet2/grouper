/*
 * @author mchyzer
 * $Id: GrouperContext.java,v 1.2 2009-02-07 20:16:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 * <pre>
 * holds threadlocal information about the current context of the database transactions.
 * 
 * The inner context is for operations like addGroup.
 * The outer context is for e.g. web requests like UI or WS
 * </pre>
 */
public class GrouperContext {
  
  /**
   * see if there is an inner context
   * @return true if inner context
   */
  public static boolean contextExistsInner() {
    return currentInnerContext.get() != null;
  }
  
  /** ip address of caller */
  private String callerIpAddress;

  /**
   * id of the current context, this is lazy loaded 
   */
  private String contextId;

  /**
   * count the number of queries that an audit requires 
   */
  private int queryCount;

  /**
   * this is not a timestamp, but rather is the nanos of when it started 
   */
  private long startedNanos = System.nanoTime();
  
  /**
   * assign fields in audit entry from outer audit (note, this might not exist)
   * @param auditEntry
   */
  public static void assignAuditEntryFieldsOuter(AuditEntry auditEntry) {
    
    GrouperContext grouperOuterContext = currentOuterContext.get();
    if (grouperOuterContext != null) {
      
      auditEntry.setUserIpAddress(grouperOuterContext.callerIpAddress);
      
    }
  }
  
  /**
   * assign fields in audit entry
   * @param auditEntry
   */
  public static void assignAuditEntryFields(AuditEntry auditEntry) {

    assignAuditEntryFieldsOuter(auditEntry);
    
    GrouperContext grouperInnerContext = currentInnerContext.get();
    if (grouperInnerContext == null) {
      throw new NullPointerException("grouperInnerContext is null, was it not started?");
    }
    auditEntry.setContextId(grouperInnerContext.getContextId());
    //divide nanos by 1000 to get micros
    auditEntry.setDurationMicroseconds((System.nanoTime() - grouperInnerContext.startedNanos)/1000);
    auditEntry.setQueryCount(grouperInnerContext.queryCount);
  }
  
  /**
   * context around a web request (e.g. UI or WS)
   */
  private static ThreadLocal<GrouperContext> currentOuterContext = 
    new ThreadLocal<GrouperContext>();
  
  /**
   * 
   */
  private static ThreadLocal<GrouperContext> currentInnerContext = 
    new ThreadLocal<GrouperContext>();
  
  /**
   * retrieve current context id
   * @param requireContext true to require context (if required in grouper.properties)
   * @return context id
   */
  public static String retrieveContextId(boolean requireContext) {
    GrouperContext grouperContextInner = currentInnerContext.get();
    if (grouperContextInner == null) {
      if (requireContext) {
        throw new RuntimeException("No context found");
      }
      return null;
    }
    return grouperContextInner.getContextId();
  }
  
  /**
   * create a new context if one doesnt already exist
   * @return true if created one, false if already existed
   */
  static boolean createNewInnerContextIfNotExist() {
    if (currentInnerContext.get() != null) {
      return false;
    }
    GrouperContext grouperContextPrivate = new GrouperContext();
    currentInnerContext.set(grouperContextPrivate);
    return true;
  }
  
  /**
   * tell the context another query occurred
   */
  public static void incrementQueryCount() {
    GrouperContext grouperContextInner = currentInnerContext.get();
    if (grouperContextInner != null) {
      grouperContextInner.queryCount++;
    }
    
    GrouperContext grouperContextOuter = currentOuterContext.get();
    if (grouperContextOuter != null) {
      grouperContextOuter.queryCount++;
    }
  }

  /**
   * delete the private context if just created
   */
  static void deleteInnerContext() {
    currentInnerContext.remove();
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

/*
 * @author mchyzer
 * $Id: HibernateHandlerBean.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;



/**
 *
 */
public class HibernateHandlerBean {
  
  /** if this is a new context, or using one that already existed */
  private boolean newContext = false;
  
  /** if the caller will audit the transaction, if not, then you might want to */
  private boolean callerWillCreateAudit = false;
  
  /** grouper session */
  private HibernateSession hibernateSession;

  /**
   * get grouper session
   * @return grouper session
   */
  public HibernateSession getHibernateSession() {
    return this.hibernateSession;
  }

  /**
   * 
   * @param hibernateSession1
   */
  public void setHibernateSession(HibernateSession hibernateSession1) {
    this.hibernateSession = hibernateSession1;
  }

  /**
   * if this is a new context, or using one that already existed
   * @return if new context
   */
  public boolean isNewContext() {
    return this.newContext;
  }

  /**
   * if this is a new context, or using one that already existed
   * @param newContext1
   */
  public void setNewContext(boolean newContext1) {
    this.newContext = newContext1;
  }

  /**
   * if this is a new context, or using one that already existed
   * @return true if caller will audit transaction
   */
  public boolean isCallerWillCreateAudit() {
    return this.callerWillCreateAudit;
  }

  /**
   * if this is a new context, or using one that already existed
   * @param callerWillAuditTransaction1
   */
  public void setCallerWillCreateAudit(boolean callerWillAuditTransaction1) {
    this.callerWillCreateAudit = callerWillAuditTransaction1;
  } 


}

package edu.internet2.middleware.grouper.hibernate;






/**
 * Superclass of query types, holds common fields
 * @version $Id: ByQueryBase.java,v 1.3 2008-06-29 17:42:41 mchyzer Exp $
 * @author mchyzer
 */
abstract class ByQueryBase {
  
  /** if we should ignore hooks */
  private boolean ignoreHooks = false;
  
  /**
   * if we should ignore hooks
   * @param theIgnoreHooks
   * @return if we should ignore hooks
   */
  public ByQueryBase setIgnoreHooks(boolean theIgnoreHooks) {
    this.ignoreHooks = theIgnoreHooks;
    return this;
  }
  
  /**
   * if we should ignore hooks
   * @return if we should ignore hooks
   */
  public boolean isIgnoreHooks() {
    return this.ignoreHooks;
  }
  
  /**
   * copy fields from this to the argument
   * @param byQueryBase
   */
  protected void copyFieldsTo(ByQueryBase byQueryBase) {
    byQueryBase.setIgnoreHooks(this.isIgnoreHooks());
  }

  /**
   * 
   */
  private HibernateSession hibernateSession;

  /**
   * @return Returns the hibernateSession.
   */
  protected HibernateSession getHibernateSession() {
    return this.hibernateSession;
  }


  
  /**
   * set the hibernate session to re-use, or null for a new one
   * byCriteriaStatic().set(hibernateSession2).select(...)
   * @param theHibernateSession2 is the session to reuse
   * @return this for chaining
   */
  protected ByQueryBase set(HibernateSession theHibernateSession2) {
    this.hibernateSession = theHibernateSession2;
    return this;
  }
  
  /**
   * 
   */
  public ByQueryBase() {
    super();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }
}

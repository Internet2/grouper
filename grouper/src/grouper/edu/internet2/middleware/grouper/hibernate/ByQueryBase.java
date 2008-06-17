package edu.internet2.middleware.grouper.hibernate;





/**
 * Superclass of query types, holds common fields
 * @version $Id: ByQueryBase.java,v 1.1.2.1 2008-06-07 19:28:22 mchyzer Exp $
 * @author mchyzer
 */
abstract class ByQueryBase {
  
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

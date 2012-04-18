package edu.internet2.middleware.grouper.hibernate;


/**
 * @version $Id: HibernateDelegate.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 * @author mchyzer
 */
class HibernateDelegate extends ByQueryBase {


  /**
   * 
   * @param theHibernateSession
   */
  public HibernateDelegate(HibernateSession theHibernateSession){
    this.set(theHibernateSession);
  }

}

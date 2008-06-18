package edu.internet2.middleware.grouper.hibernate;


/**
 * @version $Id: HibernateDelegate.java,v 1.1.2.1 2008-06-07 19:28:22 mchyzer Exp $
 * @author harveycg
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

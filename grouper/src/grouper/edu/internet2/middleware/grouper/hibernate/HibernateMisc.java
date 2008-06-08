package edu.internet2.middleware.grouper.hibernate;



/**
 * @version $Id: HibernateMisc.java,v 1.1.2.1 2008-06-08 07:21:24 mchyzer Exp $
 * @author harveycg
 */
public class HibernateMisc extends HibernateDelegate {

  /**
   * @param theHibernateSession
   */
  public HibernateMisc(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }

  /**
   * Flush the underlying hibernate session (sync the object model with the DB).
   * This doesnt commit or anything, it just sends the bySql across
   */
  public void flush() {
    this.getHibernateSession().getSession().flush();
  }

}

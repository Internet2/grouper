package edu.internet2.middleware.grouper.hibernate;




/**
 * @version $Id: HibernateMisc.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 * @author mchyzer
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
    
    HibernateSession.assertNotGrouperReadonly();
    
    this.getHibernateSession().getSession().flush();
  }

}

/*
 * @author mchyzer
 * $Id: HibGrouperLifecycle.java,v 1.1.2.1 2008-06-07 19:28:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;


/**
 * callbacks for hib grouper lifecycle events
 */
public interface HibGrouperLifecycle {

  /**
   * after an update occurs
   * @param hibernateSession 
   */
  public void onPostUpdate(HibernateSession hibernateSession);
  
  /**
   * after an sav (insert) occurs
   * @param hibernateSession 
   */
  public void onPostSave(HibernateSession hibernateSession);
  
}

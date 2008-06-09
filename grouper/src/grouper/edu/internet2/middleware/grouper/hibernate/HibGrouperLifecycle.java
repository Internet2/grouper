/*
 * @author mchyzer
 * $Id: HibGrouperLifecycle.java,v 1.1.2.2 2008-06-09 05:52:52 mchyzer Exp $
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
   * after a save (insert) occurs
   * @param hibernateSession 
   */
  public void onPostSave(HibernateSession hibernateSession);
  
  /**
   * before an update occurs
   * @param hibernateSession 
   */
  public void onPreUpdate(HibernateSession hibernateSession);
  
  /**
   * before a save (insert) occurs
   * @param hibernateSession 
   */
  public void onPreSave(HibernateSession hibernateSession);

  /**
   * after a delete occurs
   * @param hibernateSession 
   */
  public void onPostDelete(HibernateSession hibernateSession);

  /**
   * before a delete (insert) occurs
   * @param hibernateSession 
   */
  public void onPreDelete(HibernateSession hibernateSession);
  
}

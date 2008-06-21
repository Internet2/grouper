/*
 * @author mchyzer
 * $Id: GrouperDefaultDTO.java,v 1.2 2008-06-21 04:16:12 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dto;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 *
 */
public abstract class GrouperDefaultDTO implements GrouperDTO {

  /**
   * take a snapshot of the data since this is what is in the db
   */
  void dbVersionReset() {
  }
  
  /**
   * see if the state of this object has changed compared to the DB state (last known)
   * @return true if changed, false if not
   */
  boolean dbVersionDifferent() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * see which fields have changed compared to the DB state (last known)
   * note that attributes will print out: attribute__attributeName
   * @return a set of attributes changed, or empty set if none
   */
  Set<String> dbVersionDifferentFields() {
    throw new RuntimeException("Not implemented");
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onDelete(org.hibernate.Session)
   */
  public boolean onDelete(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onLoad(org.hibernate.Session, java.io.Serializable)
   */
  public void onLoad(Session s, Serializable id) {
    this.dbVersionReset();
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onSave(org.hibernate.Session)
   */
  public boolean onSave(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onUpdate(org.hibernate.Session)
   */
  public boolean onUpdate(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }
  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostDelete(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostSave(HibernateSession hibernateSession) {
    this.dbVersionReset();
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    this.dbVersionReset();
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreDelete(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreSave(HibernateSession hibernateSession) {
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreUpdate(HibernateSession hibernateSession) {
  }

  
}

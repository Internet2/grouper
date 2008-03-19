/*
 * @author mchyzer
 * $Id: Hib3HibernateVersioned.java,v 1.1.2.1 2008-03-19 18:46:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;


/**
 * Hibernate should be able to figure out if insert/update for 
 * assigned id's and version, but it isnt, so tell it.
 */
public abstract class Hib3HibernateVersioned extends Hib3DAO {

  /**
   * hibernate increments with each insert/update (-1 means insert, 0+ means update)
   */
  private long hibernateVersion = -1;

  /** constant name of field (and javabean property) for hibernateVersion */
  public static final String FIELD_HIBERNATE_VERSION = "hibernateVersion";
  
  /**
   * hibernate increments with each insert/update (-1 means insert, 0+ means update)
   * @return the hibernateVersion
   */
  public long getHibernateVersion() {
    return this.hibernateVersion;
  }

  /**
   * hibernate increments with each insert/update
   * @param hibernateVersion the hibernateVersion to set
   * @return this (will need to typecast)
   */
  public Hib3HibernateVersioned setHibernateVersion(long hibernateVersion) {
    this.hibernateVersion = hibernateVersion;
    return this;
  }
}

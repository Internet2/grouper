/*
 * @author mchyzer
 * $Id: ChangeLogConsumerDAO.java,v 1.1 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;

/**
 * change log consumer data access methods
 */
public interface ChangeLogConsumerDAO extends GrouperDAO {
  
  /**
   * find all change log consumers
   * @return all consumers
   */
  public Set<ChangeLogConsumer> findAll();
  
  /**
   * find a change log consumer by name
   * @param name is the consumer name
   * @param exceptionIfNotFound true if exception should be thrown if not found
   * @return the consumer or null or exception
   */
  public ChangeLogConsumer findByName(String name, boolean exceptionIfNotFound);
  
  /** 
   * insert or update an change log entry object 
   * @param changeLogConsumer 
   */
  public void saveOrUpdate(ChangeLogConsumer changeLogConsumer);
  
}

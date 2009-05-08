/*
 * @author mchyzer
 * $Id: ChangeLogTypeDAO.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogType;


/**
 * audit type data access methods
 */
public interface ChangeLogTypeDAO extends GrouperDAO {
  
  /**
   * find all audit types
   * @return all audit types
   */
  public Set<ChangeLogType> findAll();
  
  /** 
   * insert or update an change log entry object 
   * @param changeLogType 
   */
  public void saveOrUpdate(ChangeLogType changeLogType);
  
  /**
   * delete entries and types by category and action
   * @param category
   * @param action
   */
  public void deleteEntriesAndTypesByCategoryAndAction(String category, String action);
  
}

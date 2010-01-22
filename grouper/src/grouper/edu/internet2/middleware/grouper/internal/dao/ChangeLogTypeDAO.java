/*
 * @author mchyzer
 * $Id: ChangeLogTypeDAO.java,v 1.2 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogType;


/**
 * change log type data access methods
 */
public interface ChangeLogTypeDAO extends GrouperDAO {
  
  /**
   * find all change log types
   * @return all change log types
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

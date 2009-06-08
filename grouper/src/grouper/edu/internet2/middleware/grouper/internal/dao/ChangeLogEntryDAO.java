/*
 * @author mchyzer
 * $Id: ChangeLogEntryDAO.java,v 1.3 2009-06-08 12:16:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;


/**
 * change log entry data access methods
 */
public interface ChangeLogEntryDAO extends GrouperDAO {
  
  /** 
   * insert or update a change log entry object 
   * @param changeLogEntry 
   */
  public void save(ChangeLogEntry changeLogEntry);
  
  /** 
   * delete change log entry object 
   * @param changeLogEntry 
   */
  public void delete(ChangeLogEntry changeLogEntry);
  
}

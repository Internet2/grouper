/*
 * @author mchyzer
 * $Id: ChangeLogEntryDAO.java,v 1.2 2009-05-12 06:35:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;


/**
 * change log entry data access methods
 */
public interface ChangeLogEntryDAO extends GrouperDAO {
  
  /** 
   * insert or update an change log entry object 
   * @param changeLogEntry 
   */
  public void save(ChangeLogEntry changeLogEntry);
  
}

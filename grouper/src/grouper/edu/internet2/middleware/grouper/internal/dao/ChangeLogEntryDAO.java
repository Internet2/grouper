/*
 * @author mchyzer
 * $Id: ChangeLogEntryDAO.java,v 1.4 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.List;

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
  
  /**
   * get the change log records after a sequence number, not including the sequence number
   * @param afterSequenceNumber
   * @param batchSize is the size of the batch
   * @return the records
   */
  public List<ChangeLogEntry> retrieveBatch(long afterSequenceNumber, int batchSize);
}

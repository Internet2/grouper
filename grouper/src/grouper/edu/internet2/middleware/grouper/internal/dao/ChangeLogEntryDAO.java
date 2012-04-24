/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: ChangeLogEntryDAO.java,v 1.5 2009-06-10 05:31:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;


/**
 * change log entry data access methods
 */
public interface ChangeLogEntryDAO extends GrouperDAO {
  
  /** 
   * insert a change log entry object 
   * @param changeLogEntry 
   */
  public void save(ChangeLogEntry changeLogEntry);
  
  /**
   * insert a batch of change log entry objects
   * @param changeLogEntries
   * @param isTempBatch
   */
  public void saveBatch(Set<ChangeLogEntry> changeLogEntries, boolean isTempBatch);
  
  /** 
   * update a change log entry object 
   * @param changeLogEntry 
   */
  public void update(ChangeLogEntry changeLogEntry);
  
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

  /**
   * find by sequence number.  This is NOT a secure method, a grouperSession does not need to be open
   * @param sequenceNumber uniquely identifies rows in change log, newer records have greater than older
   * @param exceptionIfNotFound
   * @return the change log entry or null if not there
   */
  public ChangeLogEntry findBySequenceNumber(long sequenceNumber, boolean exceptionIfNotFound);
}

/**
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
 */
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.GrouperAPI;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public abstract class GrouperPIT extends GrouperAPI {

  /** active */
  public static final String COLUMN_ACTIVE = "active";
  
  /** start_time */
  public static final String COLUMN_START_TIME = "start_time";
  
  /** end_time */
  public static final String COLUMN_END_TIME = "end_time";
  
  /** constant for field name for: activeDb */
  public static final String FIELD_ACTIVE_DB = "activeDb";
  
  /** constant for field name for: startTimeDb */
  public static final String FIELD_START_TIME_DB = "startTimeDb";
  
  /** constant for field name for: endTimeDb */
  public static final String FIELD_END_TIME_DB = "endTimeDb";
  
  /** activeDb */
  private String activeDb;
  
  /** startTimeDb */
  private Long startTimeDb;
  
  /** endTimeDb */
  private Long endTimeDb;
  
  /**
   * @return activeDb
   */
  public String getActiveDb() {
    return activeDb;
  }

  /**
   * @param activeDb
   */
  public void setActiveDb(String activeDb) {
    this.activeDb = activeDb;
  }

  /**
   * @return startTimeDb
   */
  public Long getStartTimeDb() {
    return startTimeDb;
  }

  /**
   * @param startTimeDb
   */
  public void setStartTimeDb(Long startTimeDb) {
    this.startTimeDb = startTimeDb;
  }

  /**
   * @return endTimeDb
   */
  public Long getEndTimeDb() {
    return endTimeDb;
  }

  /**
   * @param endTimeDb
   */
  public void setEndTimeDb(Long endTimeDb) {
    this.endTimeDb = endTimeDb;
  }

  /**
   * @return true if active
   */
  public boolean isActive() {
    if (activeDb == null) {
      throw new RuntimeException("activeDb should not be null.");
    }
    
    if (activeDb.equals("T")) {
      return true;
    }
    
    return false;
  }
  
  /**
   * @return start time
   */
  public Timestamp getStartTime() {
    if (startTimeDb == null) {
      throw new RuntimeException("startTimeDb should not be null.");
    }
    
    return new Timestamp(startTimeDb / 1000);
  }
  
  /**
   * @return end time
   */
  public Timestamp getEndTime() {
    if (endTimeDb != null) {
      return new Timestamp(endTimeDb / 1000);
    }
    
    return null;
  }
}

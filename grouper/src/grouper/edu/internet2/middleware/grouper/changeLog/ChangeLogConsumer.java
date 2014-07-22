/**
 * Copyright 2014 Internet2
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
/*
 * @author mchyzer
 * $Id: ChangeLogConsumer.java,v 1.1 2009-06-09 17:24:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * changeLog consumer
 */
@SuppressWarnings("serial")
public class ChangeLogConsumer extends GrouperAPI implements Hib3GrouperVersioned {

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  /** column */
  public static final String COLUMN_LAST_SEQUENCE_PROCESSED = "last_sequence_processed";

  /** column */
  public static final String COLUMN_NAME = "name";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";


  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastSequenceProcessed */
  public static final String FIELD_LAST_SEQUENCE_PROCESSED = "lastSequenceProcessed";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * fields in to string deep method
   */
  private static final Set<String> TO_STRING_DEEP_FIELDS = GrouperUtil.toSet(
      FIELD_CREATED_ON_DB, 
      FIELD_ID, FIELD_LAST_SEQUENCE_PROCESSED, FIELD_LAST_UPDATED_DB, FIELD_NAME);


  
  /**
   * empty constructor
   */
  public ChangeLogConsumer() {
    
  }
  
  /**
   * see if one changeLog type is the same as another (not looking at last update, id, etc)
   * @param changeLogType
   * @return true if equals, false if not
   */
  public boolean equalsDeep(ChangeLogConsumer changeLogType) {
    
    return new EqualsBuilder().append(this.name, changeLogType.name)
      .isEquals();
  }
  
  /**
   * the string repre
   * @return string 
   */
  public String toStringDeep() {
    return GrouperUtil.toStringFields(this, TO_STRING_DEEP_FIELDS);
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "ChangeLogConsumer name: " + this.name;
  }
  
  /** name of the grouper changeLog consumer table in the db */
  public static final String TABLE_GROUPER_CHANGE_LOG_CONSUMER = "grouper_change_log_consumer";
  
  /** id of this consumer */
  private String id;

  /** name of this consumer */
  private String name;
  
  /** what is the number of the last sequence processed */
  private Long lastSequenceProcessed;
  
  /** when this record was last updated */
  private Long lastUpdatedDb;
  
  /** when this record was created */
  private Long createdOnDb;
  
  /**
   * uuid of row
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of row
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    if (this.lastUpdatedDb == null) {
      this.lastUpdatedDb = System.currentTimeMillis();
    }
    //assign id if not there
    if (StringUtils.isBlank(this.getId())) {
      this.setId(GrouperUuid.getUuid());
    }

    this.truncate();

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
    this.truncate();
  }

  /**
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.name = GrouperUtil.truncateAscii(this.name, 100);
    this.id = GrouperUtil.truncateAscii(this.id, 128);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("not implemented");
  }

  /**
   * name of this consumer
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * name of this consumer
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * what is the number of the last sequence processed
   * @return the number
   */
  public Long getLastSequenceProcessed() {
    return lastSequenceProcessed;
  }

  /**
   * what is the number of the last sequence processed
   * @param lastSequenceProcessed1
   */
  public void setLastSequenceProcessed(Long lastSequenceProcessed1) {
    this.lastSequenceProcessed = lastSequenceProcessed1;
  }

}

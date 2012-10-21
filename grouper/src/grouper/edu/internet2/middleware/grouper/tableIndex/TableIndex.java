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
package edu.internet2.middleware.grouper.tableIndex;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Keep track of last index for groups, stems, attribute definitions, and attribute names
 * grab a certain number of indices, and update the table in an autonomous transaction.
 * If it doesnt work, try again some more (20 times?).  This holds the type and
 * last index.  The static methods in this class will allow safe access to an index
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class TableIndex extends GrouperAPI implements Hib3GrouperVersioned {
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "GrouperTableIndex: " + this.id;
  }

  /** name of the grouper table index table */
  public static final String TABLE_GROUPER_TABLE_INDEX = "grouper_table_index";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_TYPE = "type";

  /** column */
  public static final String COLUMN_LAST_INDEX_RESERVED = "last_index_reserved";



  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** column */
  public static final String FIELD_LAST_INDEX_RESERVED = "lastIndexReserved";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATED_ON_DB,  
      FIELD_ID, FIELD_LAST_UPDATED_DB, 
      FIELD_TYPE, FIELD_LAST_INDEX_RESERVED);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATED_ON_DB, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_LAST_INDEX_RESERVED,
      FIELD_LAST_UPDATED_DB, FIELD_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this type */
  private String id;
  
  /** type of index, group, stem, attributeDef, attributeDefName, etc */
  private TableIndexType type = null;

  /**
   * time in millis when this stem set was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this stem set was last modified
   */
  private Long lastUpdatedDb;

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!(other instanceof TableIndex)) {
      return false;
    }
    TableIndex otherTableIndex = (TableIndex)other;
    return StringUtils.equals(this.getId(), otherTableIndex.getId());
  }
  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.id)
      .toHashCode();
  }
  
  /**
   * @return id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * type of index, group, stem, attributeDef, attributeDefName, etc
   * @return type
   */
  public TableIndexType getType() {
    return this.type;
  }

  /**
   * get string value of type for hibernate
   * @return type
   */
  public String getTypeDb() {
    return this.type == null ? null : this.type.name();
  }
  
  /**
   * set stem set assignment type
   * @param type1
   */
  public void setType(TableIndexType type1) {
    this.type = type1;
  }

  /**
   * type of index, group, stem, attributeDef, attributeDefName, etc
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = TableIndexType.valueOfIgnoreCase(type1, false);
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
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getTableIndex().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getTableIndex().delete(this);
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
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
   * @see GrouperAPI#onPreSave(HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    
    this.lastUpdatedDb = System.currentTimeMillis();
  }

  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
    
    if (this.dbVersionDifferentFields().contains(FIELD_TYPE)) {
      throw new RuntimeException("cannot update type");
    }
  }

  /** last index reserved, stored in JVM */
  private long lastIndexReserved = 0;
  
  /**
   * last index reserved, stored in JVM
   * @return
   */
  public long getLastIndexReserved() {
    return lastIndexReserved;
  }
  
  /**
   * last index reserved, stored in JVM
   * @param lastIndexReserved1
   */
  public void setLastIndexReserved(long lastIndexReserved1) {
    this.lastIndexReserved = lastIndexReserved1;
  }

  /**
   * save the statGrouperTableIndexretrieving from DB
   * @reGrouperTableIndexe dbVersion
   */
  @Override
  public TableIndex dbVersion() {
    return (TableIndex)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }
  @Override
  public GrouperAPI clone() {
    // TODO Auto-generated method stub
    return null;
  }
}

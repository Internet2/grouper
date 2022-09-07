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
package edu.internet2.middleware.grouper.tableIndex;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
   * make sure the current user can assign id index
   */
  public static void assertCanAssignIdIndex() {
    String allowedGroupName = GrouperConfig.retrieveConfig().propertyValueString("grouper.tableIndex.groupWhoCanAssignIdIndex");
    if (StringUtils.isNotBlank(allowedGroupName)) {
      
      Subject subject = GrouperSession.staticGrouperSession().getSubject();
      
      
      if (!PrivilegeHelper.isWheelOrRoot(subject)) {

        Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
            .internal_getRootSession(), allowedGroupName, false);
        if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
          throw new RuntimeException("Cannot assign ID index! " + GrouperUtil.subjectToString(subject) + " since not in group: " + allowedGroupName);
        }
      }
    }
  }
  
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

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TableIndex.class);
  
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
  
  /**
   * 
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * map of types to the list of ids which are available
   */
  private static Map<TableIndexType, List<Long>> reservedIds = new HashMap<TableIndexType, List<Long>>();

  /**
   * clear reserved id in case an existing one was used
   * @param tableIndexType
   */
  public static void clearReservedId(TableIndexType tableIndexType, long id) {
    synchronized(tableIndexType) {
      List<Long> longList = reservedIds.get(tableIndexType);
      if (longList != null) {
        for (int i=0;i<longList.size();i++) {
          Long theLong = longList.get(i);
          if (GrouperUtil.equals(theLong, id)) {
            longList.set(i, null);
          }
        }
      }
    }
  }


  /**
   * clear all ids
   * @param tableIndexType
   */
  public static void clearReservedIds(TableIndexType tableIndexType) {
    synchronized(tableIndexType) {
      List<Long> longList = reservedIds.get(tableIndexType);
      if (longList != null) {
        for (int i=0;i<longList.size();i++) {
          longList.set(i, null);
        }
      }
    }
  }

  /**
   * get ids for this type of object, if needed, increment the index in the database
   * @param tableIndexType
   * @param count
   * @return the id that can be used for the type of object
   */
  public static List<Long> reserveIds(TableIndexType tableIndexType, int count) {
    
    List<Long> ids = reserveIdHelper(tableIndexType, count);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Reserved " + count + " idIndexes: " + ids.get(0) + ", for type: " + tableIndexType);
    }

    return ids;
    
  }

  /**
   * get an id for this type of object, if needed, increment the index in the database
   * @param tableIndexType
   * @return the id that can be used for the type of object
   */
  public static long reserveId(TableIndexType tableIndexType) {
    
    List<Long> ids = reserveIdHelper(tableIndexType, 1);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Reserved idIndex: " + ids.get(0) + ", for type: " + tableIndexType);
    }

    return ids.get(0);
  }

  /**
   * get an id for this type of object, if needed, increment the index in the database
   * @param tableIndexType
   * @return the id that can be used for the type of object
   */
  private static List<Long> reserveIdHelper(TableIndexType tableIndexType, Integer count) {
    
    synchronized (tableIndexType) {
      
      List<Long> result = new ArrayList<Long>();
      
      List<Long> longList = reservedIds.get(tableIndexType);
      
      if (longList == null) {
        longList = new ArrayList<Long>();
        reservedIds.put(tableIndexType, longList);
      }
      
      while (result.size() < count) {
        Long nextOne = reserveOneFromList(longList);
        if (nextOne == null) {
          break;
        }
        result.add(nextOne);
      }
      
      if (result.size() == count) {
        return result;
      }
      
      //ok, we need to reserve some more...
      int defaultCount = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.reserveIdsDefault", 10);
      GrouperContext grouperContext = GrouperContext.retrieveDefaultContext();
      
      GrouperEngineBuiltin grouperEngineBuiltin = grouperContext == null ? null : grouperContext.getGrouperEngine();
      
      if (grouperEngineBuiltin != null) {
        switch(grouperEngineBuiltin) {
          case GSH:
            defaultCount = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.reserveIdsGsh", 1);
            break;
          case LOADER:
            defaultCount = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.reserveIdsLoader", 10);
            break;
          case WS:
            defaultCount = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.reserveIdsWs", 10);
            break;
          case UI:
            defaultCount = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.reserveIdsUi", 10);
            break;
          default:
            //nothing
        }
      }
          
      int idsToReserve = defaultCount + (count - result.size());

      TableIndex tableIndex = GrouperDAOFactory.getFactory().getTableIndex().reserveIds(tableIndexType,idsToReserve);
      
      //last index reserved
      long lastIndexReserved = tableIndex.getLastIndexReserved();
      
      // note it should be empty but just in case
      longList.clear();
      for (long currentIdIndex=(lastIndexReserved-idsToReserve) + 1; currentIdIndex<=lastIndexReserved; currentIdIndex++) {
        longList.add(currentIdIndex);
      }
      
      while (result.size() < count) {
        Long nextOne = reserveOneFromList(longList);
        result.add(nextOne);
      }
      
      return result;
    }
    
  }
  private static Long reserveOneFromList(List<Long> longList) {
    if (longList.size() > 0) {
      return longList.remove(longList.size()-1);
    }
    return null;
  }
  
}

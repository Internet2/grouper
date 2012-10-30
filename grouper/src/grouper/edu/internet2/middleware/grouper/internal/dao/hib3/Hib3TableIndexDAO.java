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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.TableIndexDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Data Access Object for table index
 * @author  mchyzer
 * @version $Id$
 */
public class Hib3TableIndexDAO extends Hib3DAO implements TableIndexDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3TableIndexDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    hibernateSession.byHql().createQuery("delete from TableIndex").executeUpdate();
    
    for (TableIndexType tableIndexType : TableIndexType.values()) {
      TableIndex.clearReservedIds(tableIndexType);
    }
    
    clearCheckedTableIndexOnce();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.TableIndexDAO#findById(java.lang.String, boolean)
   */
  public TableIndex findById(String id, boolean exceptionIfNotFound) {
    TableIndex tableIndex = HibernateSession.byHqlStatic()
      .createQuery("from TableIndex where id = :theId")
      .setString("theId", id).uniqueResult(TableIndex.class);
    
    if (tableIndex == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find table index by id: " + id);
    }
    
    return tableIndex;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.TableIndexDAO#saveOrUpdate(TableIndex)
   */
  public void saveOrUpdate(TableIndex tableIndex) {
    HibernateSession.byObjectStatic().saveOrUpdate(tableIndex);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.TableIndexDAO#delete(TableIndex)
   */
  public void delete(final TableIndex tableIndex) {
    HibernateSession.byObjectStatic().delete(tableIndex);
  }

  /**
   * @see TableIndexDAO#findByType(String)
   */
  @Override
  public TableIndex findByType(TableIndexType type) {
    TableIndex tableIndex = HibernateSession.byHqlStatic()
        .createQuery("from TableIndex where typeDb = :theType")
        .setString("theType", type.name()).uniqueResult(TableIndex.class);
    return tableIndex;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3TableIndexDAO.class);

  public static long testingNumberOfTimesReservedIndexes = 0;
  
  /**
   * @see TableIndexDAO#reserveIds(TableIndexType, int)
   */
  @Override
  public TableIndex reserveIds(final TableIndexType tableIndexType, final int numberOfIndicesToReserve) {
    TableIndex tableIndex = reserveIdsHelper(tableIndexType, numberOfIndicesToReserve);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Reserved from DB: " + numberOfIndicesToReserve + " indexes, for type: " 
          + tableIndexType + ", lastIndexReserved: " + tableIndex.getLastIndexReserved());
    }
    return tableIndex;
  }
  
  /** if we checked table index on startup once */
  private static Map<TableIndexType, Boolean> checkedTableIndexOnce = new HashMap<TableIndexType, Boolean>();
  
  /**
   * clear that we have checked the table
   */
  private static void clearCheckedTableIndexOnce() {
    checkedTableIndexOnce.clear();
  }
  
  /**
   * @see TableIndexDAO#reserveIds(TableIndexType, int)
   * @param tableIndexType
   * @param numberOfIndicesToReserve
   */
  private TableIndex reserveIdsHelper(final TableIndexType tableIndexType, final int numberOfIndicesToReserve) {
    
    //try 50 times, if not successful keep trying
    int numberOfTries = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.numberOfTries", 50);

    Exception lastException = null;
    
    for (int i=0;i<numberOfTries;i++) {
      try {
        
        testingNumberOfTimesReservedIndexes++;
        
        //lets do an autonomous transaction
        TableIndex tableIndex = (TableIndex)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          /**
           * callback
           */
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            //lets get the table index... see if it exists
            TableIndex localTableIndex = findByType(tableIndexType);
            int minIndex = GrouperConfig.retrieveConfig().propertyValueInt("idIndex." + tableIndexType + ".minIndex", 10000);

            if (localTableIndex == null) {
              localTableIndex = new TableIndex();
                            
              
              int lastReservedIndex = (minIndex + numberOfIndicesToReserve) -1;
              
              //max in table
              int rowCount = HibernateSession.bySqlStatic().select(Integer.class, "select count(*) from " + tableIndexType.tableName());
              if (rowCount > 0) {
                int maxInTable = numberOfIndicesToReserve + GrouperUtil.defaultIfNull(
                    HibernateSession.bySqlStatic().select(Integer.class, "select max(id_index) from " + tableIndexType.tableName()), 0);
                if (maxInTable > lastReservedIndex) {
                  lastReservedIndex = maxInTable;
                  if (LOG.isDebugEnabled()) {
                    LOG.debug("Table has rows, but and max is " + (maxInTable-numberOfIndicesToReserve) + " so last reserved will be that instead of: " + lastReservedIndex);
                  }
                } else {
                  if (LOG.isDebugEnabled()) {
                    LOG.debug("Table has rows, but its max is " + (maxInTable-numberOfIndicesToReserve) + " and reserved will be: " + lastReservedIndex);
                  }
                }
              }
              localTableIndex.setLastIndexReserved(lastReservedIndex);
              localTableIndex.setId(GrouperUuid.getUuid());
              localTableIndex.setType(tableIndexType);
              
              saveOrUpdate(localTableIndex);
              hibernateHandlerBean.getHibernateSession().commit(GrouperCommitType.COMMIT_NOW);
              return localTableIndex;
            }
            
            long nextIndex = localTableIndex.getLastIndexReserved();
            if (nextIndex < minIndex) {
              nextIndex = minIndex;
            }
            
            //check once, make sure the index is at least the max in the table
            Boolean checkedOnce = checkedTableIndexOnce.get(tableIndexType);
            if (checkedOnce == null || !checkedOnce) {
              if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.tableIndex.verifyOnStartup", true)) {
              
                
                //max in table
                int rowCount = HibernateSession.bySqlStatic().select(Integer.class, "select count(*) from " + tableIndexType.tableName());
                if (rowCount > 0) {
                  int maxInTable = numberOfIndicesToReserve + GrouperUtil.defaultIfNull(HibernateSession.bySqlStatic().select(Integer.class, 
                      "select max(id_index) from " + tableIndexType.tableName()), 0);
                  if (maxInTable > nextIndex) {
                    nextIndex = maxInTable;
                  }
                }
                
              }

              checkedTableIndexOnce.put(tableIndexType, true);
              
            }
            
            localTableIndex.setLastIndexReserved(nextIndex + numberOfIndicesToReserve);
            saveOrUpdate(localTableIndex);
            hibernateHandlerBean.getHibernateSession().commit(GrouperCommitType.COMMIT_NOW);
            return localTableIndex;
          }
        });

        return tableIndex;
      } catch (Exception e) {
        lastException = e;
        if (LOG.isDebugEnabled()) {
          LOG.debug("Problem finiding table index for " + tableIndexType.name(), e);
        }
        //its ok, its probably that another process is reserving the objects... wait a random number of millis between 0 and 1000 millis...
        GrouperUtil.sleep(new Random().nextInt(1000));
      }
    }
    throw new RuntimeException("Cant find next available index for " + tableIndexType + "...", lastException);
  }

}

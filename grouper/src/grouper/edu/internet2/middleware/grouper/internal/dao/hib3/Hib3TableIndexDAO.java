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

import java.util.Random;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.TableIndexDAO;
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
  public TableIndex findByType(String type) {
    TableIndex tableIndex = HibernateSession.byHqlStatic()
        .createQuery("from TableIndex where typeDb = :theType")
        .setString("theType", type).uniqueResult(TableIndex.class);
    return tableIndex;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3TableIndexDAO.class);

  /**
   * @see TableIndexDAO#reserveIds(TableIndexType, int)
   */
  @Override
  public TableIndex reserveIds(final TableIndexType tableIndexType, final int numberOfIndicesToReserve) {
    
    //try 20 times, if not successful keep trying
    int numberOfTries = GrouperConfig.retrieveConfig().propertyValueInt("grouper.tableIndex.numberOfTries", 20);

    for (int i=0;i<numberOfTries;i++) {
      try {
        
        //lets do an autonomous transaction
        TableIndex tableIndex = (TableIndex)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          /**
           * callback
           */
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            //lets get the table index... see if it exists
            TableIndex localTableIndex = findByType(tableIndexType.name());
            if (localTableIndex == null) {
              localTableIndex = new TableIndex();
              
              //TODO get the largest from the table
              
              localTableIndex.setLastIndexReserved(numberOfIndicesToReserve);
              saveOrUpdate(localTableIndex);
              return localTableIndex;
            }
            localTableIndex.setLastIndexReserved(localTableIndex.getLastIndexReserved() + numberOfIndicesToReserve);
            saveOrUpdate(localTableIndex);
            return localTableIndex;
          }
        });

        return tableIndex;
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Problem finiding table index for " + tableIndexType.name(), e);
        }
        //its ok, its probably that another process is reserving the objects... wait a random number of millis between 0 and 1000 millis...
        GrouperUtil.sleep(new Random().nextInt(1000));
      }
    }
    throw new RuntimeException("Cant find next available index for " + tableIndexType + "...");
  }

}

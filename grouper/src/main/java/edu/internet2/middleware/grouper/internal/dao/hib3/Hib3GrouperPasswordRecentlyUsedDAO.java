/**
 * Copyright 2018 Internet2
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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperPasswordRecentlyUsedDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 * Data Access Object for grouper password recently used
 * @version $Id$
 */
public class Hib3GrouperPasswordRecentlyUsedDAO extends Hib3DAO implements GrouperPasswordRecentlyUsedDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3GrouperPasswordRecentlyUsedDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    hibernateSession.byHql().createQuery("delete from GrouperPasswordRecentlyUsed").executeUpdate();
    
  }

  /**
   * 
   * @param id
   * @param exceptionIfNotFound
   * @return
   */
  public GrouperPasswordRecentlyUsed findById(String id, boolean exceptionIfNotFound) {
    GrouperPasswordRecentlyUsed grouperPasswordRecentlyUsed = HibernateSession.byHqlStatic()
      .createQuery("from GrouperPasswordRecentlyUsed where id = :theId")
      .setString("theId", id).uniqueResult(GrouperPasswordRecentlyUsed.class);
    
    if (grouperPasswordRecentlyUsed == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find grouper password recently used by id: " + id);
    }
    
    return grouperPasswordRecentlyUsed;
  }


 /**
  * 
  * @param grouperPasswordRecentlyUsed
  */
  public void saveOrUpdate(GrouperPasswordRecentlyUsed grouperPasswordRecentlyUsed) {
    HibernateSession.byObjectStatic().saveOrUpdate(grouperPasswordRecentlyUsed);
  }
  
  /**
   */
  public void delete(final GrouperPasswordRecentlyUsed grouperPasswordRecentlyUsed) {
    HibernateSession.byObjectStatic().delete(grouperPasswordRecentlyUsed);
  }
  
  public static void main(String[] args) {
    
    List<Long> list = new ArrayList<Long>();
    list.add(1L);
    list.add(3L);
    list.add(2L);
    
    System.out.println(list);
    Collections.sort(list);
    System.out.println(list);
    
  }
  
  /**
   * find GrouperPasswordRecentlyUsed 
   */
  @Override
  public Set<GrouperPasswordRecentlyUsed> findByGrouperPasswordIdAndStatus(String grouperPasswordId, Set<Character> statuses, QueryOptions queryOptions) {

    if (StringUtils.isBlank(grouperPasswordId)) {
      throw new RuntimeException("grouperPasswordId cannot be null");
    }
    
    StringBuilder sql = new StringBuilder("select theGrouperPasswordRecentlyUsed "
        + " from GrouperPasswordRecentlyUsed theGrouperPasswordRecentlyUsed where "
        + " theGrouperPasswordRecentlyUsed.grouperPasswordId = :grouperPasswordId ");

    ByHqlStatic hqlStatic = HibernateSession.byHqlStatic().options(queryOptions);
        
    if (statuses != null && statuses.size() > 0) {
      
      String[] statusesArray = new String[statuses.size()];
      
      int i=0;
      for (Character status: statuses) {
        String bindVarName = "status" + i;
        statusesArray[i] = ":"+bindVarName;
        hqlStatic.setString(bindVarName, String.valueOf(status));
        i++;
      }
      
      String statusesCommaSeparated = StringUtils.join(statusesArray, ',');
      
      sql.append(" and theGrouperPasswordRecentlyUsed.status in (");
      sql.append(statusesCommaSeparated);
      sql.append(") ");
      
    }
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQueryPaging() == null) {
      int defaultSize = 20;
      queryOptions.paging(QueryPaging.page(defaultSize, 1, false));
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sort(QuerySort.desc("attemptMillis"));
    }
     
    hqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setString( "grouperPasswordId", grouperPasswordId );
     
     return hqlStatic.listSet(GrouperPasswordRecentlyUsed.class);
  }
  
  @Override
  public int cleanupOldEntriesFromGrouperPasswordRecentlyUsedTable() {
    
    String sql = " select gpru2.id, gpru2.grouperPasswordId, gpru2.attemptMillis from GrouperPasswordRecentlyUsed gpru2 where "
        + " gpru2.grouperPasswordId in ( select gpru.grouperPasswordId from GrouperPasswordRecentlyUsed gpru group by gpru.grouperPasswordId having count(*) > 20) ";
    
    List<Object[]> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .list(Object[].class);
    
    
    if (results.size() == 0) {
      return 0;
    }
    
    Map<String, List<Long>> grouperPasswordIdToAttemptMillis = new HashMap<String, List<Long>>();
    
    for (Object[] result: results) {
      
      String grouperPasswordId = (String)result[1];
      Long attemptMillis = (Long)result[2];
      
      List<Long> attemptMillisForOneGrouperPasswordId = grouperPasswordIdToAttemptMillis.getOrDefault(grouperPasswordId, new ArrayList<Long>());
      attemptMillisForOneGrouperPasswordId.add(attemptMillis);
      
      grouperPasswordIdToAttemptMillis.put(grouperPasswordId, attemptMillisForOneGrouperPasswordId);
      
    }
    
    int totalDeleted = 0;
    
    int entriesToKeep = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.grouperPasswordRecentlyUsedCleanupDaemon.entriesToKeep", 20);
    
    for (String grouperPasswordId: grouperPasswordIdToAttemptMillis.keySet()) {
      
      List<Long> attemptMillis = grouperPasswordIdToAttemptMillis.get(grouperPasswordId);
      
      List<Long> subList = attemptMillis.subList(0, attemptMillis.size() - entriesToKeep);
      
      grouperPasswordIdToAttemptMillis.put(grouperPasswordId, subList);
      
      totalDeleted = totalDeleted + deleteGrouperPasswordRecentlyUsedEntries(grouperPasswordId, subList);
      
    }
   
    return totalDeleted;
    
  }
  
  
  private int deleteGrouperPasswordRecentlyUsedEntries(String grouperPasswordId, List<Long> attemptMillis) {
    
    int batchSize = 5000;
    
    Collections.sort(attemptMillis);
    
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(attemptMillis, batchSize);
    
    int totalDeleted = 0;
    
    for (int batchIndex=0; batchIndex<numberOfBatches; batchIndex++) {
      List<Long> theBatch = GrouperClientUtils.batchList(attemptMillis, batchSize, batchIndex);
      
      // the last one is the latest
      Long earliestAttemptMillis = theBatch.get(theBatch.size()-1);
      
      int countDeleted = HibernateSession.byHqlStatic()
      .createQuery("delete from GrouperPasswordRecentlyUsed gpru where gpru.grouperPasswordId = :grouperPasswordId and gpru.attemptMillis <= :attemptMillis")
      .setLong("attemptMillis", earliestAttemptMillis)
      .setString("grouperPasswordId", grouperPasswordId)
      .executeUpdateInt();
      
      totalDeleted = totalDeleted + countDeleted;
      
    }
    
    return totalDeleted;
    
  }
  
  public int deleteGrouperPasswordRecentlyUsedEntries(String grouperPasswordId) {
    
    String sql = " select gpru.attemptMillis from GrouperPasswordRecentlyUsed gpru where "
        + " gpru.grouperPasswordId = :grouperPasswordId ";
    
    List<Long> attemptMillis = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setString("grouperPasswordId", grouperPasswordId)
      .setCacheable(false)
      .list(Long.class);
    
    
    if (attemptMillis.size() == 0) {
      return 0;
    }
    
    return deleteGrouperPasswordRecentlyUsedEntries(grouperPasswordId, attemptMillis);
    
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3GrouperPasswordRecentlyUsedDAO.class);

}

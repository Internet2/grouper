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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperPasswordRecentlyUsedDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3GrouperPasswordRecentlyUsedDAO.class);

}

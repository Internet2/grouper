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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ConfigDAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * Data Access Object for config
 * @author  mchyzer
 * @version $Id$
 */
public class Hib3ConfigDAO extends Hib3DAO implements ConfigDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3ConfigDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    hibernateSession.byHql().createQuery("delete from GrouperConfigHibernate").executeUpdate();
    
  }

  
  
  
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ConfigDAO#findById(java.lang.String, boolean)
   */
  public GrouperConfigHibernate findById(String id, boolean exceptionIfNotFound) {
    GrouperConfigHibernate config = HibernateSession.byHqlStatic()
      .createQuery("from GrouperConfigHibernate where id = :theId")
      .setString("theId", id).uniqueResult(GrouperConfigHibernate.class);
    
    if (config == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find config by id: " + id);
    }
    
    return config;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ConfigDAO#saveOrUpdate(GrouperConfigHibernate)
   */
  public void saveOrUpdate(GrouperConfigHibernate grouperConfigHibernate) {
    HibernateSession.byObjectStatic().saveOrUpdate(grouperConfigHibernate);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ConfigDAO#delete(GrouperConfigHibernate)
   */
  public void delete(final GrouperConfigHibernate grouperConfigHibernate) {
    HibernateSession.byObjectStatic().delete(grouperConfigHibernate);
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3ConfigDAO.class);

  private static long countFindAll = 0;
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.ConfigDAO#findAll(ConfigFileName, java.sql.Timestamp, java.lang.String)
   */
  public Set<GrouperConfigHibernate> findAll(ConfigFileName configFileName,
      Timestamp changedAfterDate, String configKey) {
    StringBuilder query = new StringBuilder();
    query.append("from GrouperConfigHibernate gch");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder whereClause = new StringBuilder();
    
    if (configFileName != null) {
      if (whereClause.length() > 0) {
        whereClause.append(" and ");
      }
      whereClause.append(" configFileNameDb = :theConfigFileName  ");
      byHqlStatic.setString("theConfigFileName", configFileName.getConfigFileName());
    }
    
    if (changedAfterDate != null) {
      if (whereClause.length() > 0) {
        whereClause.append(" and ");
      }
      whereClause.append(" lastUpdatedDb > :theLastUpdated ");
      byHqlStatic.setLong("theLastUpdated", changedAfterDate.getTime());
    }

    if (!StringUtils.isBlank(configKey)) {
      if (whereClause.length() > 0) {
        whereClause.append(" and ");
      }
      whereClause.append(" configKey = :theConfigKey  ");
      byHqlStatic.setString("theConfigKey", configKey);
    }

    if (whereClause.length() > 0) {
      query.append(" where ").append(whereClause);
    }
    
    Set<GrouperConfigHibernate> configs = byHqlStatic
        .createQuery(query.toString())
        .listSet(GrouperConfigHibernate.class);
    return new TreeSet<GrouperConfigHibernate>(configs);
  }


  @Override
  public Map<String, Set<GrouperConfigHibernate>> findByFileAndKey(Collection<MultiKey> configFileNameAndKeys) {
    Map<String, Set<GrouperConfigHibernate>> result = new HashMap<String, Set<GrouperConfigHibernate>>();
    
    if (GrouperUtil.length(configFileNameAndKeys) > 0) {
    
      List<MultiKey> fullList = new ArrayList<MultiKey>(configFileNameAndKeys);
      
      for (MultiKey entry : configFileNameAndKeys) {
        // get el and normal
        String fullPropertyName = (String)entry.getKey(1);
        String propertyNameString = GrouperUtil.stripSuffix(fullPropertyName, ".elConfig");
        String propertyNameStringEl = propertyNameString + ".elConfig";
        // add in the other one
        if (!StringUtils.equals(fullPropertyName, propertyNameString)) {
          fullList.add(new MultiKey(entry.getKey(0), propertyNameString));
        } else {
          fullList.add(new MultiKey(entry.getKey(0), propertyNameStringEl));
        }
      }
      
      
      int batchSize = 450;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(fullList, batchSize);
      for (int i=0; i<numberOfBatches; i++) {
        
        List<MultiKey> currentBatch = GrouperUtil.batchList(fullList, batchSize, i);
        
        StringBuilder query = new StringBuilder();
        query.append(" from GrouperConfigHibernate gch where ");
        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

        StringBuilder whereClause = new StringBuilder();
        
        for (int j=0;j<currentBatch.size();j++) {
          if (j > 0) {
            whereClause.append(" or ");
          }
          whereClause.append(" ( ").append(" configFileNameDb = :theConfigFileName").append(j).append(" and configKey = :configKey").append(j).append(" ) ");
          byHqlStatic.setString("theConfigFileName" + j, (String)currentBatch.get(j).getKey(0));
          byHqlStatic.setString("configKey" + j, (String)currentBatch.get(j).getKey(1));
          
        }
        
        if (whereClause.length() > 0) {
          query.append(whereClause);
        }
        
        Set<GrouperConfigHibernate> configs = byHqlStatic
            .createQuery(query.toString())
            .listSet(GrouperConfigHibernate.class);

        for (GrouperConfigHibernate grouperConfigHibernate : GrouperUtil.nonNull(configs)) {
          Set<GrouperConfigHibernate> localSet = result.get(grouperConfigHibernate.getConfigKey());
          if (localSet == null) {
            localSet = new HashSet<GrouperConfigHibernate>();
            result.put(grouperConfigHibernate.getConfigKey(), localSet);
          }
          localSet.add(grouperConfigHibernate);
        }
      }
    }
    
    return result;

  }


}

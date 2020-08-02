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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITConfigDAO;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class Hib3PITConfigDAO extends Hib3DAO implements PITConfigDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITConfigDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#saveOrUpdate(PITGrouperConfigHibernate)
   */
  public void saveOrUpdate(PITGrouperConfigHibernate pitGrouperConfigHibernate) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGrouperConfigHibernate);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGrouperConfigHibernates);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#delete(PITGrouperConfigHibernate)
   */
  public void delete(PITGrouperConfigHibernate pitGrouperConfigHibernate) {
    HibernateSession.byObjectStatic().delete(pitGrouperConfigHibernate);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {//and subjectSourceId != 'g:isa'
    hibernateSession.byHql().createQuery("delete from PITGrouperConfigHibernate where sourceId not in (select config.id from GrouperConfigHibernate as config) ").executeUpdate();
  }

  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findBySourceIdActive(java.lang.String, boolean, boolean)
   */
  public PITGrouperConfigHibernate findBySourceIdActive(String id, boolean createIfNotFound, boolean exceptionIfNotFound) {
    PITGrouperConfigHibernate pitGrouperConfigHibernate = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGrouperConfigHibernate from PITGrouperConfigHibernate as pitGrouperConfigHibernate where pitGrouperConfigHibernate.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITGrouperConfigHibernate.class);
    
    if (pitGrouperConfigHibernate == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITGrouperConfigHibernate with sourceId=" + id + " not found");
    }
    
    return pitGrouperConfigHibernate;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  @Override
  public PITGrouperConfigHibernate findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    return findBySourceIdActive(id, false, exceptionIfNotFound);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findBySourceIdsActive(Collection))
   */
  @Override
  public Set<PITGrouperConfigHibernate> findBySourceIdsActive(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Set<PITGrouperConfigHibernate> results = new HashSet<PITGrouperConfigHibernate>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, 100);
   
    //if there are more than 100, batch these up and return them
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive");

      StringBuilder sql = new StringBuilder("select pitGrouperConfigHibernate from PITGrouperConfigHibernate as pitGrouperConfigHibernate where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, 100, i);

      sql.append(" pitGrouperConfigHibernate.sourceId in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(") and activeDb = 'T'");
   
      Set<PITGrouperConfigHibernate> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITGrouperConfigHibernate.class);
      results.addAll(localResult);
    }

    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findByIds(Collection))
   */
  @Override
  public Set<PITGrouperConfigHibernate> findByIds(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Set<PITGrouperConfigHibernate> results = new HashSet<PITGrouperConfigHibernate>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, 100);
   
    //if there are more than 100, batch these up and return them
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindById");

      StringBuilder sql = new StringBuilder("select pitGrouperConfigHibernate from PITGrouperConfigHibernate as pitGrouperConfigHibernate where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, 100, i);

      sql.append(" pitGrouperConfigHibernate.id in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(")");
   
      Set<PITGrouperConfigHibernate> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITGrouperConfigHibernate.class);
      results.addAll(localResult);
    }

    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITGrouperConfigHibernate findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITGrouperConfigHibernate pitGrouperConfigHibernate = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGrouperConfigHibernate from PITGrouperConfigHibernate as pitGrouperConfigHibernate where pitGrouperConfigHibernate.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITGrouperConfigHibernate.class);
    
    if (pitGrouperConfigHibernate == null && exceptionIfNotFound) {
      throw new RuntimeException("PITGrouperConfigHibernate with sourceId=" + id + " not found");
    }
    
    return pitGrouperConfigHibernate;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITGrouperConfigHibernate> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITGrouperConfigHibernate> results = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGrouperConfigHibernate from PITGrouperConfigHibernate as pitGrouperConfigHibernate where pitGrouperConfigHibernate.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITGrouperConfigHibernate.class);
    
    if (results.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITGrouperConfigHibernate with sourceId=" + id + " not found");
    }
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findById(java.lang.String, boolean)
   */
  public PITGrouperConfigHibernate findById(String id, boolean exceptionIfNotFound) {
    PITGrouperConfigHibernate pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITGrouperConfigHibernate as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITGrouperConfigHibernate.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITGrouperConfigHibernate with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    
    return HibernateSession.byHqlStatic().createQuery(
        "select id from PITGrouperConfigHibernate where endTimeDb is not null and endTimeDb < :time")
        .setLong("time", time.getTime() * 1000)
        .deleteInBatches(String.class, "PITGrouperConfigHibernate", "id");

  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITGrouperConfigHibernate where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITGrouperConfigHibernate where id = :id")
      .setString("id", id)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findMissingActivePITConfigs()
   */
  @Override
  public Set<GrouperConfigHibernate> findMissingActivePITConfigs() {
    Set<GrouperConfigHibernate> configs = HibernateSession
        .byHqlStatic()
        .createQuery("select config from GrouperConfigHibernate config where " +
            "not exists (select 1 from PITGrouperConfigHibernate pit where config.id = pit.sourceId)")
        .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITConfigs")
        .listSet(GrouperConfigHibernate.class);
      
      return configs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITConfigDAO#findMissingInactivePITConfigs()
   */
  @Override
  public Set<PITGrouperConfigHibernate> findMissingInactivePITConfigs() {
    Set<PITGrouperConfigHibernate> pitConfigs = HibernateSession
        .byHqlStatic()
        .createQuery("select pit from PITGrouperConfigHibernate pit where activeDb = 'T' and " +
            "not exists (select 1 from GrouperConfigHibernate config where config.id = pit.sourceId)")
        .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITConfigs")
        .listSet(PITGrouperConfigHibernate.class);
      
      return pitConfigs;
  }
  
  

}


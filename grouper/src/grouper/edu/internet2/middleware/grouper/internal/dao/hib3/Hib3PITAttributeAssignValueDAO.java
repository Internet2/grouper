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
import java.util.Set;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignValueDAO extends Hib3DAO implements PITAttributeAssignValueDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignValueDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignValue)
   */
  public void saveOrUpdate(PITAttributeAssignValue pitAttributeAssignValue) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignValue);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeAssignValue> pitAttributeAssignValues) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignValues);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignValue)
   */
  public void delete(PITAttributeAssignValue pitAttributeAssignValue) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignValue);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignValue where sourceId not in (select v.id from AttributeAssignValue as v)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeAssignValue findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignValue pitAttributeAssignValue = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignValue.class);
    
    if (pitAttributeAssignValue == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeAssignValue with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignValue;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeAssignValue findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignValue pitAttributeAssignValue = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignValue.class);
    
    if (pitAttributeAssignValue == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignValue with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignValue;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeAssignValue findById(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignValue pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignValue as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignValue.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignValue with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#updatePITAttributeAssignId(java.lang.String, java.lang.String)
   */
  public void updatePITAttributeAssignId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssignValue set attributeAssignId = :newId where attributeAssignId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findActiveByPITAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssignValue> findActiveByPITAttributeAssignId(String id) {
    Set<PITAttributeAssignValue> values = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.attributeAssignId = :id and attrAssignValue.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByPITAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssignValue.class);
    
    return values;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    return HibernateSession.byHqlStatic().createQuery("select id from PITAttributeAssignValue where endTimeDb is not null and endTimeDb < :time")
        .setLong("time", time.getTime() * 1000)
        .deleteInBatches(String.class, "PITAttributeAssignValue", "id");
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findByPITAttributeAssignId(java.lang.String, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<PITAttributeAssignValue> findByPITAttributeAssignId(String attributeAssignId, QueryOptions queryOptions) {
    Set<PITAttributeAssignValue> attributeAssignValues = HibernateSession.byHqlStatic()
      .createQuery("from PITAttributeAssignValue as theAttributeAssignValue where " +
          "theAttributeAssignValue.attributeAssignId = :theAttributeAssignId")
      .options(queryOptions)
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByPITAttributeAssignId")
      .setString("theAttributeAssignId", attributeAssignId)
      .listSet(PITAttributeAssignValue.class);

    //return result
    return attributeAssignValues;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findMissingActivePITAttributeAssignValues()
   */
  public Set<AttributeAssignValue> findMissingActivePITAttributeAssignValues() {

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder hql = new StringBuilder("select value from AttributeAssignValue value where " +
        "not exists (select 1 from PITAttributeAssignValue pit where value.id = pit.sourceId) " +
        "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
        "    where temp.string01 = value.id " +
        "    and type.actionName='addAttributeAssignValue' and type.changeLogCategory='attributeAssignValue' and type.id=temp.changeLogTypeId)");
    
    Set<String> idsOfAttributeDefsToIgnore = GrouperConfig.retrieveConfig().attributeDefIdsToIgnoreChangeLogAndAudit();
    if (GrouperUtil.length(idsOfAttributeDefsToIgnore) > 0) {
      
      hql.append(" and not exists (select 1 from AttributeDefName adn, AttributeAssign aa where value.attributeAssignId = aa.id "
          + "and aa.attributeDefNameId = adn.id and adn.attributeDefId in (" 
          + HibUtils.convertToInClause(idsOfAttributeDefsToIgnore, byHqlStatic) + ") )");
      
    }
    
    Set<String> idsOfAttributeDefNamesToIgnore = GrouperConfig.retrieveConfig().attributeDefNameIdsToIgnoreChangeLogAndAudit();
    if (GrouperUtil.length(idsOfAttributeDefNamesToIgnore) > 0) {
      
      hql.append(" and not exists (select 1 from AttributeAssign aa where value.attributeAssignId = aa.id "
          + "and aa.attributeDefNameId in (" 
          + HibUtils.convertToInClause(idsOfAttributeDefNamesToIgnore, byHqlStatic) + ") )");
      
    }
    
    Set<AttributeAssignValue> values = byHqlStatic
      .createQuery(hql.toString())
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeAssignValues")
      .listSet(AttributeAssignValue.class);
    
    return values;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findMissingInactivePITAttributeAssignValues()
   */
  public Set<PITAttributeAssignValue> findMissingInactivePITAttributeAssignValues() {

    Set<PITAttributeAssignValue> values = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignValue pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeAssignValue value where value.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeAssignValue' and type.changeLogCategory='attributeAssignValue' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssignValues")
      .listSet(PITAttributeAssignValue.class);
    
    return values;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITAttributeAssignValue where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITAttributeAssignValue> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeAssignValue> pitAttributeAssignValue = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITAttributeAssignValue.class);
    
    if (pitAttributeAssignValue.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignValue with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignValue;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssignValue where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

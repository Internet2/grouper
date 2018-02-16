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

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefNameDAO extends Hib3DAO implements PITAttributeDefNameDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefNameDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDefName)
   */
  public void saveOrUpdate(PITAttributeDefName pitAttributeDefName) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefName);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeDefName> pitAttributeDefNames) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefNames);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDefName)
   */
  public void delete(PITAttributeDefName pitAttributeDefName) {
    HibernateSession.byObjectStatic().delete(pitAttributeDefName);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeDefName where sourceId not in (select attrDefName.id from AttributeDefName as attrDefName)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeDefName findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeDefName pitAttributeDefName = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefName from PITAttributeDefName as attrDefName where attrDefName.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeDefName.class);
    
    if (pitAttributeDefName == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeDefName with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefName;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeDefName findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeDefName pitAttributeDefName = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefName from PITAttributeDefName as attrDefName where attrDefName.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeDefName.class);
    
    if (pitAttributeDefName == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDefName with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefName;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITAttributeDefName> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITAttributeDefName> pitAttributeDefNames = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefName from PITAttributeDefName as attrDefName where attrDefName.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITAttributeDefName.class);
    
    if (pitAttributeDefNames.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDefName with sourceId=" + id + " not found");
    }
    
    return pitAttributeDefNames;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeDefName findById(String id, boolean exceptionIfNotFound) {
    PITAttributeDefName pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDefName as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeDefName.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeDefName with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    return HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDefName where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdateInt();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findByName(java.lang.String, boolean)
   */
  public Set<PITAttributeDefName> findByName(String name, boolean orderByStartTime) {
    String sql = "select pitAttributeDefName from PITAttributeDefName as pitAttributeDefName where pitAttributeDefName.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITAttributeDefName> pitAttributeDefNames = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", name)
      .listSet(PITAttributeDefName.class);
    
    return pitAttributeDefNames;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findByPITAttributeDefId(java.lang.String)
   */
  public Set<PITAttributeDefName> findByPITAttributeDefId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitAttributeDefName from PITAttributeDefName as pitAttributeDefName where pitAttributeDefName.attributeDefId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITAttributeDefId")
        .setString("id", id)
        .listSet(PITAttributeDefName.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findByPITStemId(java.lang.String)
   */
  public Set<PITAttributeDefName> findByPITStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitAttributeDefName from PITAttributeDefName as pitAttributeDefName where pitAttributeDefName.stemId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITStemId")
        .setString("id", id)
        .listSet(PITAttributeDefName.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findMissingActivePITAttributeDefNames()
   */
  public Set<AttributeDefName> findMissingActivePITAttributeDefNames() {

    Set<AttributeDefName> attrs = HibernateSession
      .byHqlStatic()
      .createQuery("select attr from AttributeDefName attr where " +
          "not exists (select 1 from PITAttributeDefName pit where attr.id = pit.sourceId and attr.nameDb = pit.nameDb) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = attr.id " +
          "    and type.actionName='addAttributeDefName' and type.changeLogCategory='attributeDefName' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = attr.id " +
          "    and type.actionName='updateAttributeDefName' and type.changeLogCategory='attributeDefName' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeDefNames")
      .listSet(AttributeDefName.class);
    
    return attrs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findMissingInactivePITAttributeDefNames()
   */
  public Set<PITAttributeDefName> findMissingInactivePITAttributeDefNames() {

    Set<PITAttributeDefName> attrs = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDefName pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeDefName attr where attr.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeDefName' and type.changeLogCategory='attributeDefName' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeDefNames")
      .listSet(PITAttributeDefName.class);
    
    return attrs;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITAttributeDefName where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDefName where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}
